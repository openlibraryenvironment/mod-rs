package org.olf.rs.hostlms;

import org.json.JSONArray;
import org.json.JSONObject;
import org.olf.rs.HostLMSItemLoanPolicy;
import org.olf.rs.HostLMSLocation;
import org.olf.rs.HostLMSLocationService;
import org.olf.rs.HostLMSShelvingLocation;
import org.olf.rs.HostLMSShelvingLocationService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ShelvingLocationSite;
import org.olf.rs.Z3950Service;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.lms.ConnectionDetailsNCIP;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.logging.DoNothingHoldingLogDetails;
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.logging.INcipLogDetails;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;

import grails.gorm.multitenancy.Tenants.CurrentTenant;
import groovy.json.StringEscapeUtils;
import groovy.util.slurpersupport.GPathResult;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public abstract class BaseHostLMSService implements HostLMSActions {

  private static final String CIRCULATION_NCIP = "ncip";

  private static IHoldingLogDetails defaultHoldingLogDetails = new DoNothingHoldingLogDetails();

  HostLMSLocationService hostLMSLocationService;
  HostLMSShelvingLocationService hostLMSShelvingLocationService;
  Z3950Service z3950Service;

  // http://www.loc.gov/z3950/agency/defns/bib1.html
  List getLookupStrategies() {
    return [
      [
        name:'Local_identifier_By_Z3950',
        precondition: { pr -> return ( pr.supplierUniqueRecordId != null ) },
        strategy: { pr, service, settings, holdingLogDetails -> return service.z3950ItemsByIdentifier(pr, settings, holdingLogDetails) },
        // We don't want to try other strategies if the precondition passes and available copies are not found
        final: true
      ],
      [
        name:'ISBN_identifier_By_Z3950',
        precondition: { pr -> return ( pr.isbn != null ) },
        strategy: { pr, service, settings, holdingLogDetails -> return service.z3950ItemsByPrefixQuery(pr,"@attr 1=7 \"${pr.isbn?.trim()}\"".toString(), settings, holdingLogDetails ) }
      ],
      [
        name:'Title_By_Z3950',
        precondition: { pr -> return ( pr.title != null ) },
        strategy: { pr, service, settings, holdingLogDetails -> return service.z3950ItemsByPrefixQuery(pr,"@attr 1=4 \"${pr.title?.trim()}\"".toString(), settings, holdingLogDetails ) }
      ],
    ]
  }

  void validatePatron(String patronIdentifier) {
  }

  public abstract CirculationClient getCirculationClient(ISettings settings, String address);

   //Method to inquire whether this LMS adapter speaks NCIP v2. Defaults to false, override if true
  public boolean isNCIP2() {
    return false;
  }

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
  ItemLocation determineBestLocation(
      ISettings settings,
      PatronRequest pr,
      IHoldingLogDetails holdingLogDetails = defaultHoldingLogDetails
  ) {

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
          def strategy_result = next_strategy.strategy(pr, this, settings, holdingLogDetails);
          holdingLogDetails.availableLocations(strategy_result);
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

        if (next_strategy?.final) {
          log.debug("Strategy ${next_strategy.name} is final, using result");
          break;
        }
      }
      else {
        log.debug("Strategy ${next_strategy.name} did not pass precondition");
      }
    }
    /*
      Allow for additional modifications to be made to the ItemLocation on a per-adapter basis.
      enrichItemLocation can be overriden as needed
    */

    log.debug("Calling enrichItemLocation");
    location = enrichItemLocation(settings, location);

    log.debug("determineBestLocation returns ${location}");
    holdingLogDetails.bestAvailableLocation(location);
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
      HostLMSLocation loc = hostLMSLocationService.ensureActive(o.location, o.location);

      HostLMSItemLoanPolicy ilp = null;
      HostLMSShelvingLocation sl = null;
      ShelvingLocationSite sls = null;

      // create/find HostLMSItemLoanPolicy
      if ( o?.itemLoanPolicy ) {
        List<HostLMSItemLoanPolicy> ilps = HostLMSItemLoanPolicy.executeQuery(POLICY_QRY, [ilp: o.itemLoanPolicy]);
        switch ( ilps.size() ) {
          case 0:
            log.debug("No HostLMSItemLoanPolicy found for ${o.itemLoanPolicy}, creating new entry");
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

      // find or create a HostLMSShelvingLocation in respect of shelvingLocation
      if ( o?.shelvingLocation != null ) {
        sl = hostLMSShelvingLocationService.ensureExists(o.shelvingLocation, o.shelvingLocation);
      }

      // if temporary location is present, use it in lieu of location for determining availability
      if ( o?.temporaryLocation != null ) {
        loc = hostLMSLocationService.ensureExists(o.temporaryLocation, o.temporaryLocation);
        // the shelving location does not apply to the temporary location but a temporaryShelvingLocation may be specified
        sl = null;
      }

      // if temporary shelving location is present, use it in lieu of shelving location for determining availability
      if ( o?.temporaryShelvingLocation != null ) {
        log.debug("Using temporaryShelvingLocation to calculate shelving location preference");
        sl = hostLMSShelvingLocationService.ensureExists(o.temporaryShelvingLocation, o.temporaryShelvingLocation);
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

      // Item Loan Policy (if present) overrides location preference when item is not lendable
      o.preference = (!ilp || ilp?.lendable) ? (loc?.supplyPreference ?: 0) : -1;
      log.debug("Setting preference for ${o} to ${o.preference} given HostLMSItemLoanPolicy ${ilp} and HostLMSLocation ${loc}");

      // Fall back to the preference for the shelving location when no sls preference is defined
      // ...can't just chain ?: here because we want an sls pref of 0 to take precedence
      log.debug("Option ${o} using shelving location of ${sl}");
      o.shelvingPreference = sls?.supplyPreference != null ? sls?.supplyPreference : (sl?.supplyPreference ?: 0);
      log.debug("Shelving preference for ${o} set to ${o.shelvingPreference}");
    }

    List<ItemLocation> sorted_options = options.findAll { it.preference >= 0 && it.shelvingPreference >= 0 }.sort {
      a,b -> a.preference <=> b.preference ?: a.shelvingPreference <=> b.shelvingPreference;
    }.reverse();

    if ( sorted_options.size() > 0 ) {
      log.debug("Preference order of locations: ${sorted_options}");
      result = sorted_options[0];
    } else {
      if ( options.size() > 0 ) {
        log.debug("Returning null for supply location because all holdings have either a location or shelving location (site) preference value < 0: ${options}");
      }
    }

    return result;
  }

  //default stub method
  public ItemLocation enrichItemLocation(ISettings settings, ItemLocation location) {
    return location;
  }

  // By default, ask for OPAC records - @override in implementation if you want different
  protected String getHoldingsQueryRecsyn() {
    return null;
  }

  // Override this method if our Host LMS Adapter needs a specific prefix for its templates
  protected String getNCIPTemplatePrefix() {
    return null;
  }

  /**
   * Record the details of the holdings for each of the records in the collection
   * @param records The records that have been found
   * @param holdingLogDetails Where we are recording the details
   */
  protected void logOpacHoldings(GPathResult records, IHoldingLogDetails holdingLogDetails) {
      // Have we been supplied any records
      if (records != null) {
          // We have so process them
          records.each { record ->
              holdingLogDetails.newRecord();
              holdingLogDetails.holdings(record?.recordData?.opacRecord?.holdings);
          }
      }
  }

  // Given the record syntax above, process response records as Opac recsyn. If you change the recsyn string above
  // you need to change the handler here. SIRSI for example needs to return us marcxml with a different location for the holdings
  protected List<ItemLocation> extractAvailableItemsFrom(z_response, String reason, IHoldingLogDetails holdingLogDetails) {
    List<ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.opacRecord != null ) {
      def withHoldings = z_response.records.record.findAll { it?.recordData?.opacRecord?.holdings?.holding?.size() > 0 };

      // Log the holdings
      logOpacHoldings(withHoldings, holdingLogDetails);

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
  public List<ItemLocation> z3950ItemsByIdentifier(PatronRequest pr, ISettings settings, IHoldingLogDetails holdingLogDetails) {

    List<ItemLocation> result = [];

    def prefix_query_string = "@attr 1=12 ${pr.supplierUniqueRecordId}";
    def z_response = z3950Service.query(settings, prefix_query_string, 1, getHoldingsQueryRecsyn(), holdingLogDetails);
    log.debug("Got Z3950 response: ${z_response}");

    if ( z_response?.numberOfRecords == 1 ) {
      // Got exactly 1 record
      List<ItemLocation> availability_summary = extractAvailableItemsFrom(z_response,"Match by @attr 1=12 ${pr.supplierUniqueRecordId}", holdingLogDetails);
      if ( availability_summary?.size() > 0 ) {
        result = availability_summary;
      }
      else {
        log.debug("CQL lookup(${prefix_query_string}) returned ${z_response?.numberOfRecords} matches. Unable to determine availability");
      }

      log.debug("At end, availability summary: ${availability_summary}");
    }

    return result;
  }

  public List<ItemLocation> z3950ItemsByPrefixQuery(PatronRequest pr, String prefix_query_string, ISettings settings, IHoldingLogDetails holdingLogDetails) {

    List<ItemLocation> result = [];

    // We need to fetch multiple records here as some sites may have separate records for electronic
    // and we'll also need a few results to determine if a title search was too broad to be useful eg.
    // we can't use title if there is more than exactly one record with holdings
    def z_response = z3950Service.query(settings, prefix_query_string, 3, getHoldingsQueryRecsyn(), holdingLogDetails);

    log.debug("Got Z3950 response: ${z_response}");

    if ( ((z_response?.numberOfRecords?.text() ?: -1) as int) > 0 ) {
      List<ItemLocation> availability_summary = extractAvailableItemsFrom(z_response, "Match by ${prefix_query_string}", holdingLogDetails);
      if ( availability_summary?.size() > 0 ) {
        result = availability_summary;
      }

      log.debug("At end, availability summary: ${availability_summary}, result=${result}");
    }
    else {
      log.debug("CQL lookup(${prefix_query_string}) returned ${z_response?.numberOfRecords} matches. Unable to determine availability");
    }

    return result;
  }

  public Map lookupPatron(ISettings settings, String patron_id, INcipLogDetails ncipLogDetails) {
    log.debug("lookupPatron(${patron_id})");
    Map result = [ result: true, status: 'OK', reason: 'spoofed' ];
    String borrowerCheckValue = settings.getSettingValue(SettingsData.SETTING_BORROWER_CHECK)
    if (borrowerCheckValue != null) {
      switch (borrowerCheckValue) {
        case CIRCULATION_NCIP:
          result = ncip2LookupPatron(settings, patron_id, ncipLogDetails)
          result.reason = 'ncip'
          break;

        default:
          log.debug("Borrower check - no action, config ${borrowerCheckValue}");
          // Borrower check is not configured, so return OK
          break;
      }
    } else {
      log.warn('borrower check not configured');
    }

    log.debug("BaseHostLMSService::lookupPatron(${patron_id}) returns ${result}");
    return result
  }

  private Map ncip2Lookup(ISettings settings, String keyValue, Boolean useUserId, INcipLogDetails ncipLogDetails) {
    Map result = [ status: 'FAIL' ];
    String key = null;
    if(useUserId) {
      key = "user_id";
    } else {
      key = "username";
    }

    log.debug("ncip2Lookup(keyValue:${keyValue}, useUserId:${useUserId})");

    if( (keyValue != null) && (keyValue.length() > 0)) {
      try {
        ConnectionDetailsNCIP ncipConnectionDetails = new ConnectionDetailsNCIP(settings);
        CirculationClient ncip_client = getCirculationClient(settings, ncipConnectionDetails.ncipServerAddress);
        if(!useUserId && !isNCIP2() ) {
          log.debug("Cannot look up by username for NCIP1 currently, skipping");
          result.result = false;
          result.problems = "Username lookup unsupported";
          return result;
        }
        log.debug("Requesting patron from ${ncipConnectionDetails.ncipServerAddress}");
        LookupUser lookupUser = null;
        if(useUserId) {
          lookupUser = new LookupUser().setUserId(keyValue);
        } else {
          lookupUser = new LookupUser().setUserName(keyValue);
        }
        lookupUser = lookupUser
          .includeUserAddressInformation()
          .includeUserPrivilege()
          .includeNameInformation()
          .setToAgency(ncipConnectionDetails.ncipToAgency)
          .setFromAgency(ncipConnectionDetails.ncipFromAgency)
          .setRegistryId(ncipConnectionDetails.registryId)
          .setApplicationProfileType(ncipConnectionDetails.ncipAppProfile);

        log.debug("[${CurrentTenant.get()}] NCIP2 lookupUser request ${lookupUser}");
        JSONObject response = ncip_client.send(lookupUser);
        log.debug("[${CurrentTenant.get()}] NCIP2 lookupUser response ${response}");

        processLookupUserResponse(result, response, ncipLogDetails);

      } catch(Exception e) {
        result.problems = "Unexpected problem in NCIP Call: ${e.message}";
        result.result = false;
      }
    } else {
      log.warn("Not calling NCIP lookup - No id value passed in");
      result.problems='No id supplied'
      result.result=false
    }

    return result;
  }

  private Map ncip2LookupById(ISettings settings, String user_id, INcipLogDetails ncipLogDetails) {
    return ncip2Lookup(settings, user_id, true, ncipLogDetails);
  }

  private Map ncip2LookupByUsername(ISettings settings, String username, INcipLogDetails ncipLogDetails) {
    return ncip2Lookup(settings, username, false, ncipLogDetails);
  }

  // {"firstName":"Stacey",
  //  "lastName":"Conrad",
  //  "privileges":[{"value":"ACTIVE","key":"STATUS"},{"value":"STA","key":"PROFILE"}],
  //  "electronicAddresses":[{"value":"Stacey.Conrad@millersville.edu","key":"mailto"},{"value":"7178715869","key":"tel"}],
  //  "userId":"M00069192"}
  protected void processLookupUserResponse(Map result, JSONObject response, INcipLogDetails ncipLogDetails) {
    if ( ( response ) && ( ! response.has('problems') ) ) {
      JSONArray priv = response.getJSONArray('privileges')
      // Return a status of BLOCKED if the user is blocked, else OK for now
      result.status=(priv.find { it.key.equalsIgnoreCase('STATUS') })?.value?.equalsIgnoreCase('BLOCKED') ? 'BLOCKED' : 'OK'
      result.userProfile=(priv.find { it.key.equalsIgnoreCase('PROFILE') })?.value
      result.result=true
      result.userid=response.opt('userId') ?: response.opt('userid')
      result.givenName=response.opt('firstName')
      result.surname=response.opt('lastName')
      protocolInformationToResult(response, ncipLogDetails);
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

  private void protocolInformationToResult(JSONObject response, INcipLogDetails ncipLogDetails) {
    try {
      ncipLogDetails.result(
              response.protocolInformation.request.endPoint,
              unescapeJson(response.protocolInformation.request.requestbody),
              response.protocolInformation.response.responseStatus,
              unescapeJson(response.protocolInformation.response.responseBody)
      );
    } catch (Exception e) {
      log.error("Unable to extract protocolInformation from NCIP response: ${e}");
    }
  }

  private String unescapeJson(String jsonString) {
      String unescaped = null;
      if (jsonString != null) {
          unescaped = StringEscapeUtils.unescapeJava(jsonString);
      }
      return(unescaped);
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
  private Map ncip2LookupPatron(ISettings settings, String patron_id, INcipLogDetails ncipLogDetails) {
    Map user_id_result = null;
    Map username_result = null;
    user_id_result = ncip2LookupById(settings, patron_id, ncipLogDetails);
    if(user_id_result.result == false) {
      log.debug("No result from userId patron lookup, attempting username");
      /*
      If the user_id lookup failed, try a lookup with the patron_id
      assigned to the username value instead, and return that result if
      and only if it is successful
      */
      username_result = ncip2LookupByUsername(settings, patron_id, ncipLogDetails);
      if(username_result.result != false) {
        return username_result;
      }
    }
    return user_id_result;
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

  public Map checkoutItem(
    ISettings settings,
    String requestId,
    String itemBarcode,
    String borrowerBarcode,
    INcipLogDetails ncipLogDetails
  ) {
    log.debug("checkoutItem(${requestId}. ${itemBarcode})");
    Map result = [
      result: true,
      reason: 'spoofed'
    ];

    String checkOutValue = settings.getSettingValue(SettingsData.SETTING_CHECK_OUT_ITEM);
    if (checkOutValue != null) {
      switch (checkOutValue) {
        case CIRCULATION_NCIP:
          result = ncip2CheckoutItem(settings, requestId, itemBarcode, borrowerBarcode, ncipLogDetails)
          break;

        default:
          log.debug("Check out - no action, config ${checkOutValue}");
          // Check in is not configured, so return true
          break;
      }
    }
    return result;
  }

  public Map ncip2CheckoutItem(
      ISettings settings,
      String requestId,
      String itemBarcode,
      String borrowerBarcode,
      INcipLogDetails ncipLogDetails
  ) {
    // set reason to ncip
    Map result = [reason: 'ncip'];

    // borrowerBarcode could be null or blank, error out if so
    if (borrowerBarcode != null && borrowerBarcode != '') {
      log.debug("ncip2CheckoutItem(${itemBarcode},${borrowerBarcode})");

      ConnectionDetailsNCIP ncipConnectionDetails = new ConnectionDetailsNCIP(settings);
      CirculationClient ncip_client = getCirculationClient(settings, ncipConnectionDetails.ncipServerAddress);
      CheckoutItem checkoutItem = new CheckoutItem()
                    .setUserId(borrowerBarcode)
                    .setItemId(itemBarcode)
                    .setRequestId(requestId)
                    .setToAgency(ncipConnectionDetails.ncipToAgency)
                    .setFromAgency(ncipConnectionDetails.ncipFromAgency)
                    .setRegistryId(ncipConnectionDetails.registryId)
                    .setApplicationProfileType(ncipConnectionDetails.ncipAppProfile);
                    //.setDesiredDueDate("2020-03-18");

      log.debug("[${CurrentTenant.get()}] NCIP2 checkoutItem request ${checkoutItem}");
      JSONObject response = ncip_client.send(checkoutItem);
      log.debug("[${CurrentTenant.get()}] NCIP2 checkoutItem response ${response}");
      protocolInformationToResult(response, ncipLogDetails);

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

  public Map acceptItem(
    ISettings settings,
    String item_id,
    String request_id,
    String user_id,
    String author,
    String title,
    String isbn,
    String call_number,
    String pickup_location,
    String requested_action,
    INcipLogDetails ncipLogDetails
  ) {
    log.debug("acceptItem(${request_id},${user_id})");
    Map result = [
      result: true,
      reason: 'spoofed'
    ]

    String acceptItemValue = settings.getSettingValue(SettingsData.SETTING_ACCEPT_ITEM);
    if (acceptItemValue != null) {
      switch (acceptItemValue) {
        case CIRCULATION_NCIP:
          // set reason block to ncip from 'spoofed'
          result.reason = 'ncip'

          ConnectionDetailsNCIP ncipConnectionDetails = new ConnectionDetailsNCIP(settings);
          CirculationClient ncip_client = getCirculationClient(settings, ncipConnectionDetails.ncipServerAddress);
          AcceptItem acceptItem = new AcceptItem()
                        .setItemId(item_id)
                        .setRequestId(request_id)
                        .setUserId(user_id)
                        .setAuthor(author)
                        .setTitle(title)
                        .setIsbn(isbn)
                        .setCallNumber(call_number)
                        .setPickupLocation(pickup_location)
                        .setToAgency(ncipConnectionDetails.ncipToAgency)
                        .setFromAgency(ncipConnectionDetails.ncipFromAgency)
                        .setRegistryId(ncipConnectionDetails.registryId)
                        .setRequestedActionTypeString(requested_action)
                        .setApplicationProfileType(ncipConnectionDetails.ncipAppProfile);

          if(getNCIPTemplatePrefix() != null) {
            log.debug("[${CurrentTenant.get()}] setting NCIP template prefix to ${getNCIPTemplatePrefix()}");
            acceptItem.setTemplatePrefix(getNCIPTemplatePrefix());
          }

          log.debug("[${CurrentTenant.get()}] NCIP acceptItem request ${acceptItem}");
          JSONObject response = ncip_client.send(acceptItem);
          log.debug("[${CurrentTenant.get()}] NCIP acceptItem response ${response}");
          protocolInformationToResult(response, ncipLogDetails);

          if ( response.has('problems') ) {
            result.result = false;
            result.problems = response.get('problems')
          }
          break;

        default:
          log.debug("Accept item - no action, config ${acceptItemValue}");
          // Check in is not configured, so return true
          break;
      }
    }
    return result;
  }


  public Map checkInItem(ISettings settings, String item_id, INcipLogDetails ncipLogDetails) {
    Map result = [
      result: true,
      reason: 'spoofed',
      already_checked_in: false
    ]

    String checkInValue = settings.getSettingValue(SettingsData.SETTING_CHECK_IN_ITEM);
    if (checkInValue != null) {
      switch (checkInValue) {
        case CIRCULATION_NCIP:
          // Set the reason from 'spoofed'
          result.reason = 'ncip'

          log.debug("checkInItem(${item_id})");
          ConnectionDetailsNCIP ncipConnectionDetails = new ConnectionDetailsNCIP(settings);
          CirculationClient ncip_client = getCirculationClient(settings, ncipConnectionDetails.ncipServerAddress);
          CheckinItem checkinItem = new CheckinItem()
                        .setItemId(item_id)
                        .setToAgency(ncipConnectionDetails.ncipToAgency)
                        .setFromAgency(ncipConnectionDetails.ncipFromAgency)
                        .setRegistryId(ncipConnectionDetails.registryId)
                        .includeBibliographicDescription()
                        .setApplicationProfileType(ncipConnectionDetails.ncipAppProfile);

          log.debug("[${CurrentTenant.get()}] NCIP checkinItem request ${checkinItem}");
          JSONObject response = ncip_client.send(checkinItem);
          log.debug("[${CurrentTenant.get()}] NCIP checkinItem response ${response}");
          protocolInformationToResult(response, ncipLogDetails);

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
          log.debug("Check In - no action, config ${checkInValue}");
          // Check in is not configured, so return true
          break;
      }
    }
    return result;
  }

  /**
   * Override this method if the server returns opac records but does something dumb like cram availability status into a public note
   */
  public List<ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    List<ItemLocation> availability_summary = [];

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("BaseHostLMSService holdings record:: ${hld}");
      hld.circulations?.circulation?.each { circ ->
        def loc = hld?.localLocation?.text()?.trim();
        if (loc && circ?.availableNow?.@value == '1') {
          log.debug("BASE extractAvailableItemsFromOpacRecord Available now");
          ItemLocation il = new ItemLocation(
                  reason: reason,
                  location: loc,
                  shelvingLocation: hld?.shelvingLocation?.text()?.trim() ?: null,
                  itemLoanPolicy: circ?.availableThru?.text()?.trim() ?: null,
                  itemId: circ?.itemId?.text()?.trim() ?: null,
                  callNumber: hld?.callNumber?.text()?.trim() ?: null)
          availability_summary << il;
        }
      }
    }

    return availability_summary;
  }

  /**
   * N.B. this method may be overriden in the LMS specific subclass - check there first - this is the default implementation
   */
  public List<ItemLocation> extractAvailableItemsFromMARCXMLRecord(record, String reason, IHoldingLogDetails holdingLogDetails) {
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
    List<ItemLocation> availability_summary = [];
    holdingLogDetails.newRecord();
    record.datafield.each { df ->
      if ( df.'@tag' == "926" ) {
        holdingLogDetails.holdings(df);
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
              availability_summary << new ItemLocation(
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
