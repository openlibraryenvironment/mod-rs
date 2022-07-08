package org.olf.rs.hostlms;

import org.json.JSONArray;
import org.json.JSONObject;
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.HostLMSItemLoanPolicy;
import org.olf.rs.HostLMSLocation;
import org.olf.rs.HostLMSLocationService;
import org.olf.rs.HostLMSShelvingLocation;
import org.olf.rs.PatronNoticeService;
import org.olf.rs.PatronRequest
import org.olf.rs.ShelvingLocationSite;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.lms.ItemLocation;

import com.k_int.web.toolkit.settings.AppSetting

import grails.gorm.multitenancy.Tenants.CurrentTenant
import groovyx.net.http.HttpBuilder

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public abstract class BaseHostLMSService implements HostLMSActions {

  HostLMSLocationService hostLMSLocationService;
  PatronNoticeService patronNoticeService;

  // http://www.loc.gov/z3950/agency/defns/bib1.html
  List getLookupStrategies() {
    return [
      [
        name:'Local_identifier_By_Z3950',
        precondition: { pr -> return ( pr.supplierUniqueRecordId != null ) },
        strategy: { pr, service -> return service.z3950ItemsByIdentifier(pr) }
      ],
      [
        name:'ISBN_identifier_By_Z3950',
        precondition: { pr -> return ( pr.isbn != null ) },
        strategy: { pr, service -> return service.z3950ItemsByPrefixQuery(pr,"@attr 1=7 \"${pr.isbn?.trim()}\"".toString() ) }
      ],
      [
        name:'Title_By_Z3950',
        precondition: { pr -> return ( pr.title != null ) },
        strategy: { pr, service -> return service.z3950ItemsByPrefixQuery(pr,"@attr 1=4 \"${pr.title?.trim()}\"".toString()) }
      ],
    ]
  }

  void validatePatron(String patronIdentifier) {
  }

  public abstract CirculationClient getCirculationClient(String address);


  /**
   *
   *
   */
  Map placeHold(String instanceIdentifier, String itemIdentifier) {
    def result=[:]
    // For NCIP2:: issue RequestItem()
    // RequestItem takes BibliographicId(A string, or name:value pair identifying an instance) or
    // ItemId(Item)(A String, or name:value pair identifying an item)
    log.debug("BaseHostLMSService::placeHold(${instanceIdentifier},${itemIdentifier}");
    result.status='HoldPlaced'
    result
  }


  /*
   * This method is called by the auto-responder on an incoming ILL request from a remote system (acting as a prospective borrower)
   * The method will use whatever strategies are available to try and find locations of copies inside THIS institution.
   * If available copies are located, the service MAY respond will-supply depending upon other configuration, if no available copies
   * are found the system MAY automatically respond not-supplied in order to rapidly move through rota entries until a possible supplier
   * is located.
   * Lookup strategies go from most specific to least.
   */
  ItemLocation determineBestLocation(PatronRequest pr) {

    log.debug("determineBestLocation(${pr})");

    ItemLocation location = null;
    def lookup_strategies = this.getLookupStrategies();
    Iterator i = lookup_strategies.iterator();

    while ( ( location==null ) && ( i.hasNext() ) ) {
      def next_strategy = i.next();
      log.debug("Next lookup strategy: ${next_strategy.name}");
      if ( next_strategy.precondition(pr) == true ) {
        log.debug("Strategy ${next_strategy.name} passed precondition");
        try {
          def strategy_result = next_strategy.strategy(pr, this);
          if ( strategy_result instanceof ItemLocation ) {
            log.debug("Legacy strategy - return top holding");
            location = strategy_result;
          }
          else if ( strategy_result instanceof List<ItemLocation> ) {
            log.debug("V2 strategy - rank supplying locations by cross referencing with hostLMSLocation");
            location = pickBestSupplyLocationFrom(strategy_result)
          }
        }
        catch ( Exception e ) {
          log.error("Problem attempting strategy ${next_strategy.name}",e);
        }
        finally {
          log.debug("Completed strategy ${next_strategy.name}, location = ${location}");
        }

      }
      else {
        log.debug("Strategy did not pass precondition");
      }
    }

    log.debug("determineBestLocation returns ${location}");
    return location;
  }


  /**
   * Cross reference the ItemLocation options returned from the local catalog with our internal information which
   * holds a preference order for supplying locations. Rank the locations according to our local info and return the
   * best option.
   */
  private ItemLocation pickBestSupplyLocationFrom(List<ItemLocation> options) {
    ItemLocation result = null;
    String POLICY_QRY = 'select ilp from HostLMSItemLoanPolicy as ilp where ilp.code=:ilp';
    String SHELVING_LOC_QRY = 'select sl from HostLMSShelvingLocation as sl where sl.code=:sl';
    String SLS_QRY = 'select sls from ShelvingLocationSite as sls where sls.location = :loc and sls.shelvingLocation=:sl';

    // Iterate through each option and see if we have a corresponding HostLMSlocation record for that location
    // If not, create one, as we may wish to record information about this location
    // Copy the location preference from the HostLMSLocation into the list of options so we can sort the list by the preference property.
    // higher preferences values == use in preference to lower values
    // Values < 0 are considered "DO NOT USE" - E.G. bindery
    options.each { o ->
      // See if we can find a HostLMSLocation for the given item - create one if not
      HostLMSLocation loc = hostLMSLocationService.EnsureActive(o.location, o.location);

      HostLMSItemLoanPolicy ilp = null;
      HostLMSShelvingLocation sl = null;
      ShelvingLocationSite sls = null;

      // create/find HostLMSItemLoanPolicy
      if ( o?.itemLoanPolicy?.trim() ) {
        List<HostLMSItemLoanPolicy> ilps = HostLMSItemLoanPolicy.executeQuery(POLICY_QRY, [ilp: o.itemLoanPolicy.trim()]);
        switch ( ilps.size() ) {
          case 0:
            ilp = new HostLMSItemLoanPolicy( code: o.itemLoanPolicy, name: o.itemLoanPolicy ).save(flush:true, failOnError:true);
            break;
          case 1:
            ilp = ilps.get(0);
            if (ilp.hidden) {
              ilp.hidden = false;
              ilp.save(flush : true, failOnError : true);
            }
            break;
          default:
            throw new RuntimeException("Multiple loan policies for ${o.itemLoanPolicy}");
            break;
        }
      }

      // create a HostLMSShelvingLocation in respect of shelvingLocation
      if ( o?.shelvingLocation != null ) {
        List<HostLMSShelvingLocation> shelving_loc_list = HostLMSShelvingLocation.executeQuery(SHELVING_LOC_QRY, [sl: o.shelvingLocation]);
        switch ( shelving_loc_list.size() ) {
          case 0:
            sl = new HostLMSShelvingLocation( code: o.shelvingLocation, name: o.shelvingLocation, supplyPreference: new Long(0)).save(flush:true, failOnError:true);
            patronNoticeService.triggerNotices(sl);
            break;

          case 1:
            sl = shelving_loc_list.get(0);

            // Is it hidden ?
            if (sl.hidden == true) {
                // we need to unhide it as it is active again
                sl.hidden = false;
                sl.save(flush : true, failOnError : true);
            }
            break;

          default:
            throw new RuntimeException("Multiple shelving locations match ${o.location}.${o.shelvingLocation}");
            break;
        }
      }

      // Create an instance of shelving location site to record the association
      if ( ( sl != null ) && ( loc != null ) ) {
        List<ShelvingLocationSite> slss = ShelvingLocationSite.executeQuery(SLS_QRY,[loc: loc, sl:sl]);
        switch ( slss.size() ) {
          case 0:
            sls = new ShelvingLocationSite( location:loc, shelvingLocation:sl).save(flush:true, failOnError:true);
            break;
          case 1:
            sls = slss.get(0);
            break;
          default:
            throw new RuntimeException("Multiple shelving location sites match ${loc}.${sl}");
            break;
        }
      }

      // Item Loan Policy overrides location preference when item is not lendable
      o.preference = ilp.lendable ? (loc?.supplyPreference ?: 0) : -1;

      // Fall back to the preference for the shelving location when no sls preference is defined
      // ...can't just chain ?: here because we want an sls pref of 0 to take precedence
      o.shelvingPreference = sls?.supplyPreference != null ? sls?.supplyPreference : (sl?.supplyPreference ?: 0);
    }

    List<ItemLocation> sorted_options = options.findAll { it.preference >= 0 && it.shelvingPreference >= 0 }.sort {
      a,b -> a.preference <=> b.preference ?: a.shelvingPreference <=> b.shelvingPreference;
    }.reverse();

    if ( sorted_options.size() > 0 ) {
      log.debug("Preference order of locations: ${sorted_options}");
      result = sorted_options[0];
    } else {
      if (options.size() > 0) {
        log.debug("Returning null for supply location because all holdings have either a location or shelving location (site) preference value < 0: ${options}");
      }
    }

    return result;
  }

  // By default, ask for OPAC records - @override in implementation if you want different
  protected String getHoldingsQueryRecsyn() {
    return null;
  }

  // Override this method if our Host LMS Adapter needs a specific prefix for its templates
  protected String getNCIPTemplatePrefix() {
    return null;
  }

  // Given the record syntax above, process response records as Opac recsyn. If you change the recsyn string above
  // you need to change the handler here. SIRSI for example needs to return us marcxml with a different location for the holdings
  protected Map<String, ItemLocation> extractAvailableItemsFrom(z_response, String reason=null) {
    Map<String, ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.opacRecord != null ) {
      def withHoldings = z_response.records.record.findAll { it?.recordData?.opacRecord?.holdings?.holding?.size() > 0 };
      if (withHoldings.size() < 1) {
        log.warn("BaseHostLMSService failed to find an OPAC record with holdings");
        return null;
      } else if (withHoldings.size() > 1) {
        log.warn("BaseHostLMSService found multiple OPAC records with holdings");
        return null;
      }
      log.debug("[BaseHostLMSService] Extract available items from OPAC record ${z_response}, reason: ${reason}");
      availability_summary = extractAvailableItemsFromOpacRecord(withHoldings?.first()?.recordData?.opacRecord, reason);
    }
    else {
      log.warn("BaseHostLMSService expected the response to contain an OPAC record, but none was found");
    }
    return availability_summary;
  }

  /**
   * The previous implementation z3950ItemByIdentifier returns the first available holding of an item, this is not ideal
   * when there are several locations holding an item and an institution wishes to express a preference as to
   * which locations are to be preferred for lending. This variant of the method returns all possible locations
   * it is the callers job to rank the response records.
   */
  public List<ItemLocation> z3950ItemsByIdentifier(PatronRequest pr) {

    List<ItemLocation> result = [];

    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://temple-psb.alma.exlibrisgroup.com:1921%2F01TULI_INST&x-pquery=water&maximumRecords=1%27
    // TNS: tcp:aleph.library.nyu.edu:9992/TNSEZB
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=water&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=@attr%201=4%20%22Head%20Cases:%20stories%20of%20brain%20injury%20and%20its%20aftermath%22&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=@attr%201=12%20000026460&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://temple-psb.alma.exlibrisgroup.com:1921%2F01TULI_INST&x-pquery=water&maximumRecords=1%27

    String z3950_proxy = 'http://reshare-mp.folio-dev.indexdata.com:9000';
    String z3950_server = getZ3950Server();

    if ( z3950_server != null ) {
      // log.debug("Sending system id query ${z3950_proxy}?x-target=http://temple-psb.alma.exlibrisgroup.com:1921/01TULI_INST&x-pquery=@attr 1=12 ${pr.supplierUniqueRecordId}");
      log.debug("Sending system id query ${z3950_proxy}?x-target=${z3950_server}&x-pquery=@attr 1=12 ${pr.supplierUniqueRecordId}");

      def z_response = HttpBuilder.configure {
        request.uri = z3950_proxy
      }.get {
          request.uri.path = '/'
          // request.uri.query = ['x-target': 'http://aleph.library.nyu.edu:9992/TNSEZB',
          request.uri.query = ['x-target': z3950_server,
                               'x-pquery': '@attr 1=12 '+pr.supplierUniqueRecordId,
                               'maximumRecords':'1' ]

          if ( getHoldingsQueryRecsyn() ) {
            request.uri.query['recordSchema'] = getHoldingsQueryRecsyn();
          }

          log.debug("Querying z server with URL ${request.uri?.toURI().toString()}")
      }

      log.debug("Got Z3950 response: ${z_response}");

      if ( z_response?.numberOfRecords == 1 ) {
        // Got exactly 1 record
        Map<String, ItemLocation> availability_summary = extractAvailableItemsFrom(z_response,"Match by @attr 1=12 ${pr.supplierUniqueRecordId}")
        if ( availability_summary?.size() > 0 ) {
          availability_summary.values().each { v ->
            result.add(v);
          }
        }

        log.debug("At end, availability summary: ${availability_summary}");
      }
    }

    return result;
  }

  public List<ItemLocation> z3950ItemsByPrefixQuery(PatronRequest pr, String prefix_query_string) {

    List<ItemLocation> result = [];

    String z3950_server = getZ3950Server();

    if ( z3950_server != null ) {
      def z_response = HttpBuilder.configure {
        request.uri = 'http://reshare-mp.folio-dev.indexdata.com:9000'
      }.get {
          request.uri.path = '/'
          request.uri.query = ['x-target': z3950_server,
                               'x-pquery': prefix_query_string,
                               'maximumRecords':'3' ]
          if ( getHoldingsQueryRecsyn() ) {
            request.uri.query['recordSchema'] = getHoldingsQueryRecsyn();
          }
          log.debug("Querying z server with URL ${request.uri?.toURI().toString()}")
      }

      log.debug("Got Z3950 response: ${z_response}");

      if ( (z_response?.numberOfRecords?.text() as int) > 0 ) {
        Map<String,ItemLocation> availability_summary = extractAvailableItemsFrom(z_response, "Match by ${prefix_query_string}");
        if ( availability_summary?.size() > 0 ) {
          availability_summary.values().each { v ->
            result.add(v)
          }
        }

        log.debug("At end, availability summary: ${availability_summary}, result=${result}");
      }
      else {
        log.debug("CQL lookup(${prefix_query_string}) returned ${z_response?.numberOfRecords} matches. Unable to determine availability");
      }
    }
    return result;
  }


  public Map lookupPatron(String patron_id) {
    log.debug("lookupPatron(${patron_id})");
    Map result = [ result: true, status: 'OK', reason: 'spoofed' ];
    AppSetting borrower_check_setting = AppSetting.findByKey('borrower_check')
    if ( ( borrower_check_setting != null ) && ( borrower_check_setting.value != null ) )  {
      switch ( borrower_check_setting.value ) {
        case 'ncip':
          result = ncip2LookupPatron(patron_id)
          result.reason = 'ncip'
          break;
        default:
          log.debug("Borrower check - no action, config ${borrower_check_setting?.value}");
          // Borrower check is not configured, so return OK
          break;
      }
    }
    else {
      log.warn('borrower check not configured');
    }

    log.debug("BaseHostLMSService::lookupPatron(${patron_id}) returns ${result}");
    return result
  }

  /**
   * @param patron_id - the patron to look up
   * @return A map with the following keys {
   *   status:'OK'|'FAIL'
   *   userid
   *   givenName
   *   surname
   *   email
   *   result: true|false
   * }
   */
  private Map ncip2LookupPatron(String patron_id) {
    Map result = [ status:'FAIL' ];
    log.debug("ncip2LookupPatron(${patron_id})");

    try {

      if ( ( patron_id != null ) && ( patron_id.length() > 0 ) ) {
        AppSetting ncip_server_address_setting = AppSetting.findByKey('ncip_server_address')
        AppSetting ncip_from_agency_setting = AppSetting.findByKey('ncip_from_agency')
        AppSetting ncip_to_agency_setting = AppSetting.findByKey('ncip_to_agency')
        AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')
        AppSetting wms_registry_id = AppSetting.findByKey('wms_registry_id')

        String ncip_server_address = ncip_server_address_setting?.value ?: ncip_server_address_setting?.defValue
        String ncip_from_agency = ncip_from_agency_setting?.value ?: ncip_from_agency_setting?.defValue
        String ncip_to_agency = ncip_to_agency_setting?.value ?: ncip_from_agency
        String ncip_app_profile = ncip_app_profile_setting?.value ?: ncip_app_profile_setting?.defValue
        // Will only be used by the client for WMS LMSs
        String registry_id = wms_registry_id?.value

        if ( ( ncip_server_address != null ) &&
             ( ncip_from_agency != null ) &&
             ( ncip_app_profile != null ) ) {

          log.debug("Request patron from ${ncip_server_address}");
          CirculationClient ncip_client = getCirculationClient(ncip_server_address);
          LookupUser lookupUser = new LookupUser()
                      .setUserId(patron_id)
                      .includeUserAddressInformation()
                      .includeUserPrivilege()
                      .includeNameInformation()
                      .setToAgency(ncip_to_agency)
                      .setFromAgency(ncip_from_agency)
                      .setRegistryId(registry_id)
                      .setApplicationProfileType(ncip_app_profile);

          log.debug("[${CurrentTenant.get()}] NCIP2 lookupUser request ${lookupUser}");
          JSONObject response = ncip_client.send(lookupUser);
          log.debug("[${CurrentTenant.get()}] NCIP2 lookupUser response ${response}");


          // {"firstName":"Stacey",
          //  "lastName":"Conrad",
          //  "privileges":[{"value":"ACTIVE","key":"STATUS"},{"value":"STA","key":"PROFILE"}],
          //  "electronicAddresses":[{"value":"Stacey.Conrad@millersville.edu","key":"mailto"},{"value":"7178715869","key":"tel"}],
          //  "userId":"M00069192"}
          if ( ( response ) && ( ! response.has('problems') ) ) {
            JSONArray priv = response.getJSONArray('privileges')
            // Return a status of BLOCKED if the user is blocked, else OK for now
            result.status=(priv.find { it.key.equalsIgnoreCase('STATUS') })?.value?.equalsIgnoreCase('BLOCKED') ? 'BLOCKED' : 'OK'
            result.userProfile=(priv.find { it.key.equalsIgnoreCase('PROFILE') })?.value
            result.result=true
            result.userid=response.opt('userId') ?: response.opt('userid')
            result.givenName=response.opt('firstName')
            result.surname=response.opt('lastName')
            if ( response.has('electronicAddresses') ) {
              JSONArray ea = response.getJSONArray('electronicAddresses')
              // We've had emails come from a key "emailAddress" AND "mailTo" in the past, check in emailAddress first and then mailTo as backup
              result.email=(ea.find { it.key=='emailAddress' })?.value ?: (ea.find { it.key=='mailTo' })?.value
              result.tel=(ea.find { it.key=='tel' })?.value
            }
          }
          else {
            result.problems=response.get('problems')
            result.result=false
          }
        }
      }
      else {
        log.warn("Not calling NCIP lookup - No patron ID passed in");
        result.problems='No patron id supplied'
        result.result=false
      }
    }
    catch ( Exception e ) {
      result.problems = "Unexpected problem in NCIP Call ${e.message}";
      result.result=false
    }

    return result;
  }

  def makeNCIPLookupUserRequest(String agency, String application_profile, String user_id) {
    return {
      NCIPMessage( 'version':'http://www.niso.org/schemas/ncip/v2_02/ncip_v2_02.xsd',
                       'xmlns':'http://www.niso.org/2008/ncip') {
        LookupUser {
          InitiationHeader {
            FromAgencyId {
              AgencyId(agency)
            }
            ToAgencyId {
              AgencyId(agency)
            }
            ApplicationProfileType(application_profile)
          }
          UserId {
            UserIdentifierValue(user_id)
          }
          UserElementType('User Address Information')
          UserElementType('Block Or Trap')
          UserElementType('Name Information')
          UserElementType('User Privilege')
          UserElementType('User ID')
        }
      }
    }
  }

  public Map checkoutItem(String requestId,
                          String itemBarcode,
                          String borrowerBarcode,
                          Symbol requesterDirectorySymbol) {

    log.debug("checkoutItem(${requestId}. ${itemBarcode},${borrowerBarcode},${requesterDirectorySymbol})");
    Map result = [
      result: true,
      reason: 'spoofed'
    ];

    AppSetting check_out_setting = AppSetting.findByKey('check_out_item')
    if ( ( check_out_setting != null ) && ( check_out_setting.value != null ) )  {
      switch ( check_out_setting.value ) {
        case 'ncip':
          result = ncip2CheckoutItem(requestId, itemBarcode, borrowerBarcode)
          break;
        default:
          log.debug("Check out - no action, config ${check_out_setting?.value}");
          // Check in is not configured, so return true
          break;
      }
    }
    return result;
  }

  public Map ncip2CheckoutItem(String requestId, String itemBarcode, String borrowerBarcode) {
    // set reason to ncip
    Map result = [reason: 'ncip'];

    // borrowerBarcode could be null or blank, error out if so
    if (borrowerBarcode != null && borrowerBarcode != '') {
      log.debug("ncip2CheckoutItem(${itemBarcode},${borrowerBarcode})");
      AppSetting ncip_server_address_setting = AppSetting.findByKey('ncip_server_address')
      AppSetting ncip_from_agency_setting = AppSetting.findByKey('ncip_from_agency')
      AppSetting ncip_to_agency_setting = AppSetting.findByKey('ncip_to_agency')
      AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')
      AppSetting wms_registry_id = AppSetting.findByKey('wms_registry_id')

      String ncip_server_address = ncip_server_address_setting?.value ?: ncip_server_address_setting?.defValue
      String ncip_from_agency = ncip_from_agency_setting?.value ?: ncip_from_agency_setting?.defValue
      String ncip_to_agency = ncip_to_agency_setting?.value ?: ncip_from_agency
      String ncip_app_profile = ncip_app_profile_setting?.value ?: ncip_app_profile_setting?.defValue
      // Will only be used by the client for WMS LMSs
      String registry_id = wms_registry_id?.value

      CirculationClient ncip_client = getCirculationClient(ncip_server_address);
      CheckoutItem checkoutItem = new CheckoutItem()
                    .setUserId(borrowerBarcode)
                    .setItemId(itemBarcode)
                    .setRequestId(requestId)
                    .setToAgency(ncip_to_agency)
                    .setFromAgency(ncip_from_agency)
                    .setRegistryId(registry_id)
                    .setApplicationProfileType(ncip_app_profile);
                    //.setDesiredDueDate("2020-03-18");

      log.debug("[${CurrentTenant.get()}] NCIP2 checkoutItem request ${checkoutItem}");
      JSONObject response = ncip_client.send(checkoutItem);
      log.debug("[${CurrentTenant.get()}] NCIP2 checkoutItem response ${response}");

      if ( response.has('problems') ) {
        result.result = false;
        result.problems = response.get('problems');
      }
      else {
        result.result = true;
        result.dueDate = response.opt('dueDate');
        result.userId = response.opt('userId')
        result.itemId = response.opt('itemId')
      }
    } else {
      result.problems = 'No institutional patron ID available'
    }
    return result;
  }

  protected String getZ3950Server() {
    return AppSetting.findByKey('z3950_server_address')?.value
  }

  public Map acceptItem(String item_id,
                        String request_id,
                        String user_id,
                        String author,
                        String title,
                        String isbn,
                        String call_number,
                        String pickup_location,
                        String requested_action) {

    log.debug("acceptItem(${request_id},${user_id})");
    Map result = [
      result: true,
      reason: 'spoofed'
    ]

    AppSetting accept_item_setting = AppSetting.findByKey('accept_item')
    if ( ( accept_item_setting != null ) && ( accept_item_setting.value != null ) )  {


      switch ( accept_item_setting.value ) {
        case 'ncip':
          // set reason block to ncip from 'spoofed'
          result.reason = 'ncip'

          AppSetting ncip_server_address_setting = AppSetting.findByKey('ncip_server_address')
          AppSetting ncip_from_agency_setting = AppSetting.findByKey('ncip_from_agency')
          AppSetting ncip_to_agency_setting = AppSetting.findByKey('ncip_to_agency')
          AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')
          AppSetting wms_registry_id = AppSetting.findByKey('wms_registry_id')

          String ncip_server_address = ncip_server_address_setting?.value
          String ncip_from_agency = ncip_from_agency_setting?.value
          String ncip_to_agency = ncip_to_agency_setting?.value ?: ncip_from_agency
          String ncip_app_profile = ncip_app_profile_setting?.value
          // Will only be used by the client for WMS LMSs
          String registry_id = wms_registry_id?.value

          CirculationClient ncip_client = getCirculationClient(ncip_server_address);
          AcceptItem acceptItem = new AcceptItem()
                        .setItemId(item_id)
                        .setRequestId(request_id)
                        .setUserId(user_id)
                        .setAuthor(author)
                        .setTitle(title)
                        .setIsbn(isbn)
                        .setCallNumber(call_number)
                        .setPickupLocation(pickup_location)
                        .setToAgency(ncip_to_agency)
                        .setFromAgency(ncip_from_agency)
                        .setRegistryId(registry_id)
                        .setRequestedActionTypeString(requested_action)
                        .setApplicationProfileType(ncip_app_profile);

          if(getNCIPTemplatePrefix() != null) {
            log.debug("[${CurrentTenant.get()}] setting NCIP template prefix to ${getNCIPTemplatePrefix()}");
            acceptItem.setTemplatePrefix(getNCIPTemplatePrefix());
          }

          log.debug("[${CurrentTenant.get()}] NCIP acceptItem request ${acceptItem}");
          JSONObject response = ncip_client.send(acceptItem);
          log.debug("[${CurrentTenant.get()}] NCIP acceptItem response ${response}");

          if ( response.has('problems') ) {
            result.result = false;
            result.problems = response.get('problems')
          }
          break;
        default:
          log.debug("Accept item - no action, config ${accept_item_setting?.value}");
          // Check in is not configured, so return true
          break;
      }
    }
    return result;
  }


  public Map checkInItem(String item_id) {
    Map result = [
      result: true,
      reason: 'spoofed',
      already_checked_in: false
    ]

    AppSetting check_in_setting = AppSetting.findByKey('check_in_item')
    if ( ( check_in_setting != null ) && ( check_in_setting.value != null ) )  {

      switch ( check_in_setting.value ) {
        case 'ncip':
          // Set the reason from 'spoofed'
          result.reason = 'ncip'

          log.debug("checkInItem(${item_id})");
          AppSetting ncip_server_address_setting = AppSetting.findByKey('ncip_server_address')
          AppSetting ncip_from_agency_setting = AppSetting.findByKey('ncip_from_agency')
          AppSetting ncip_to_agency_setting = AppSetting.findByKey('ncip_to_agency')
          AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')
          AppSetting wms_registry_id = AppSetting.findByKey('wms_registry_id')

          String ncip_server_address = ncip_server_address_setting?.value
          String ncip_from_agency = ncip_from_agency_setting?.value
          String ncip_to_agency = ncip_to_agency_setting?.value ?: ncip_from_agency
          String ncip_app_profile = ncip_app_profile_setting?.value
          // Will only be used by the client for WMS LMSs
          String registry_id = wms_registry_id?.value

          CirculationClient ncip_client = getCirculationClient(ncip_server_address);
          CheckinItem checkinItem = new CheckinItem()
                        .setItemId(item_id)
                        .setToAgency(ncip_to_agency)
                        .setFromAgency(ncip_from_agency)
                        .setRegistryId(registry_id)
                        .includeBibliographicDescription()
                        .setApplicationProfileType(ncip_app_profile);

          log.debug("[${CurrentTenant.get()}] NCIP checkinItem request ${checkinItem}");
          JSONObject response = ncip_client.send(checkinItem);
          log.debug("[${CurrentTenant.get()}] NCIP checkinItem response ${response}");


          log.debug(response?.toString());
          if ( response != null && response.has('problems') ) {
            // If there is a problem block, something went wrong, so change response to false.
            result.result = false;

            // If the problem block is just because the item is already checked in, then make response true
            try {
              JSONArray problemJsonArray = response.getJSONArray('problems');
              if(problemJsonArray.length() == 1) //Only if this is our ONLY problem
              {
                JSONObject problemJson = problemJsonArray.getJSONObject(0);
                if(problemJson.has("type") && problemJson.getString("type").equalsIgnoreCase("Item Not Checked Out")) {
                  result.result = true;
                  result.already_checked_in = true;
                  log.debug("[${CurrentTenant.get()}] NCIP checkinItem not needed: already checked in")
                  break;
                }
              }
            } catch(Exception e) {
              log.debug("[${CurrentTenant.get()}] Error getting problem type: ${e.getLocalizedMessage()}");
            }
            result.problems = response.get('problems')
          }
          break;
        default:
          log.debug("Check In - no action, config ${check_in_setting?.value}");
          // Check in is not configured, so return true
          break;
      }
    }
    return result;
  }

  /**
   * Override this method if the server returns opac records but does something dumb like cram availability status into a public note
   */
  public Map<String, ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    Map<String,ItemLocation> availability_summary = [:]

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("BaseHostLMSService holdings record:: ${hld}");
      hld.circulations?.circulation?.any { circ ->
        if (circ?.availableNow?.@value == '1') {
          log.debug("BASE extractAvailableItemsFromOpacRecord Available now");
          ItemLocation il = new ItemLocation(
                  reason: reason,
                  location: hld.localLocation,
                  shelvingLocation: hld.shelvingLocation,
                  itemLoanPolicy: circ?.availableThru,
                  callNumber: hld.callNumber)
          availability_summary[hld.localLocation] = il;
        }
      }
    }

    return availability_summary;
  }

  /**
   * N.B. this method may be overriden in the LMS specific subclass - check there first - this is the default implementation
   */
  public Map<String, ItemLocation> extractAvailableItemsFromMARCXMLRecord(record, String reason=null) {
    // <zs:searchRetrieveResponse>
    //   <zs:numberOfRecords>9421</zs:numberOfRecords>
    //   <zs:records>
    //     <zs:record>
    //       <zs:recordSchema>marcxml</zs:recordSchema>
    //       <zs:recordXMLEscaping>xml</zs:recordXMLEscaping>
    //       <zs:recordData>
    //         <record>
    //           <leader>02370cam a2200541Ii 4500</leader>
    //           <controlfield tag="008">140408r20141991nyua j 001 0 eng d</controlfield>
    //           <datafield tag="040" ind1=" " ind2=" ">
    //           </datafield>
    //           <datafield tag="926" ind1=" " ind2=" ">
    //             <subfield code="a">WEST</subfield>
    //             <subfield code="b">RESERVES</subfield>
    //             <subfield code="c">QL737 .C23 C58 2014</subfield>
    //             <subfield code="d">BOOK</subfield>
    //             <subfield code="f">2</subfield>
    //           </datafield>
    Map<String,ItemLocation> availability_summary = [:]
    record.datafield.each { df ->
      if ( df.'@tag' == "926" ) {
        Map<String,String> tag_data = [:]
        df.subfield.each { sf ->
          if ( sf.'@code' != null ) {
            tag_data[ sf.'@code'.toString().trim() ] = sf.text().trim()
          }
        }
        log.debug("Found holdings tag : ${df} ${tag_data}");
        try {
          if ( tag_data['b'] != null ){
            if ( [ 'RESERVES', 'CHECKEDOUT', 'MISSING', 'DISCARD'].contains(tag_data['b']) ) {
              // $b contains a string we think implies non-availability
            }
            else {
              log.debug("Assuming ${tag_data['b']} implies available - update extractAvailableItemsFromMARCXMLRecord if not the case");
              availability_summary[tag_data['a']] = new ItemLocation(
                                                            location: tag_data['a'],
                                                            shelvingLocation: tag_data['b'],
                                                            callNumber:tag_data['c'],
                                                            reason: reason )
            }
          }
          else {
            log.debug("No subfield b present - unable to determine number of copies available");
          }
        }
        catch ( Exception e ) {
          // All kind of odd strings like 'NONE' that mean there aren't any holdings available
          log.debug("Unable to parse number of copies: ${e.message}");
        }
      }
    }
    log.debug("MARCXML availability: ${availability_summary}");
    return availability_summary;
  }
}
