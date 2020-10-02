package org.olf.rs;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder
import org.olf.rs.ItemLocation;
import org.olf.rs.statemodel.Status;
import com.k_int.web.toolkit.settings.AppSetting
import groovy.xml.StreamingMarkupBuilder
import static groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.FromServer;
import com.k_int.web.toolkit.refdata.RefdataValue
import static groovyx.net.http.ContentTypes.XML
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.lms.HostLMSActions;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.NCIP2Client;
import org.olf.rs.circ.client.CirculationClient;

import org.json.JSONObject;
import org.json.JSONArray;


/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public abstract class BaseHostLMSService implements HostLMSActions {

  // http://www.loc.gov/z3950/agency/defns/bib1.html
  def lookup_strategies = [
    [ 
      name:'Local_identifier_By_Z3950',
      precondition: { pr -> return ( pr.systemInstanceIdentifier != null ) },
      stragegy: { pr, service -> return service.z3950ItemByIdentifier(pr) }
    ],
    [ 
      name:'ISBN_identifier_By_Z3950',
      precondition: { pr -> return ( pr.isbn != null ) },
      stragegy: { pr, service -> return service.z3950ItemByCQL(pr,"@attr 1=7 \"${pr.isbn?.trim()}\"".toString() ) }
    ],
    [ 
      name:'Local_identifier_By_Title',
      precondition: { pr -> return ( pr.title != null ) },
      stragegy: { pr, service -> return service.z3950ItemByCQL(pr,"@attr 1=4 \"${pr.title?.trim()}\"".toString()) }
    ],

  ]

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

  ItemLocation determineBestLocation(PatronRequest pr) {

    log.debug("determineBestLocation(${pr})");

    ItemLocation location = null;
    Iterator i = lookup_strategies.iterator();
    
    while ( ( location==null ) && ( i.hasNext() ) ) {
      def next_strategy = i.next();
      log.debug("Next lookup strategy: ${next_strategy.name}");
      if ( next_strategy.precondition(pr) == true ) {
        log.debug("Strategy ${next_strategy.name} passed precondition");
        try {
          location = next_strategy.stragegy(pr, this);
        }
        catch ( Exception e ) {
          log.error("Problem attempting strategy ${next_strategy.name}",e);
        }
        finally {
          log.debug("Completed strategt ${next_strategy.name}");
        }
     
      }
      else {
        log.debug("Strategy did not pass precondition");
      }
    }
    
    log.debug("determineBestLocation returns ${location}");
    return location;
  }
  
  public ItemLocation z3950ItemByIdentifier(PatronRequest pr) {

    ItemLocation result = null;

    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://temple-psb.alma.exlibrisgroup.com:1921%2F01TULI_INST&x-pquery=water&maximumRecords=1%27
    // TNS: tcp:aleph.library.nyu.edu:9992/TNSEZB
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=water&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=@attr%201=4%20%22Head%20Cases:%20stories%20of%20brain%20injury%20and%20its%20aftermath%22&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=@attr%201=12%20000026460&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://temple-psb.alma.exlibrisgroup.com:1921%2F01TULI_INST&x-pquery=water&maximumRecords=1%27

    String z3950_proxy = 'http://reshare-mp.folio-dev.indexdata.com:9000';
    String z3950_server = getZ3950Server();

    if ( z3950_server != null ) {
      // log.debug("Sending system id query ${z3950_proxy}?x-target=http://temple-psb.alma.exlibrisgroup.com:1921/01TULI_INST&x-pquery=@attr 1=12 ${pr.systemInstanceIdentifier}");
      log.debug("Sending system id query ${z3950_proxy}?x-target=${z3950_server}&x-pquery=@attr 1=12 ${pr.systemInstanceIdentifier}");

      def z_response = HttpBuilder.configure {
        request.uri = z3950_proxy
      }.get {
          request.uri.path = '/'
          // request.uri.query = ['x-target': 'http://aleph.library.nyu.edu:9992/TNSEZB',
          request.uri.query = ['x-target': z3950_server,
                               'x-pquery': '@attr 1=12 '+pr.systemInstanceIdentifier,
                               'maximumRecords':'1' ]
      }

      log.debug("Got Z3950 response: ${z_response}");

      if ( z_response?.numberOfRecords == 1 ) {
        // Got exactly 1 record
        Map availability_summary = [:]
        z_response?.records?.record?.recordData?.opacRecord?.holdings?.holding?.each { hld ->
          log.debug("${hld}");
          log.debug("${hld.circulations?.circulation?.availableNow}");
          log.debug("${hld.circulations?.circulation?.availableNow?.@value}");
          if ( hld.circulations?.circulation?.availableNow?.@value=='1' ) {
            log.debug("Available now");
            ItemLocation il = new ItemLocation( location: hld.localLocation, shelvingLocation:hld.shelvingLocation, callNumber:hld.callNumber )
  
            if ( result == null ) 
              result = il;
  
            availability_summary[hld.localLocation] = il;
          }
        }
  
        log.debug("At end, availability summary: ${availability_summary}");
      }
    }

    return result;
  }

  public ItemLocation z3950ItemByTitle(PatronRequest pr) {

    ItemLocation result = null;

    String z3950_server = getZ3950Server();

    if ( z3950_server != null ) {
      def z_response = HttpBuilder.configure {
        request.uri = 'http://reshare-mp.folio-dev.indexdata.com:9000'
      }.get {
          request.uri.path = '/'
          // request.uri.query = ['x-target': 'http://aleph.library.nyu.edu:9992/TNSEZB',
          request.uri.query = ['x-target': z3950_server,
                               'x-pquery': '@attr 1=4 "'+pr.title?.trim()+'"',
                               'maximumRecords':'3' ]
      }
  
      log.debug("Got Z3950 response: ${z_response}");
  
      if ( z_response?.numberOfRecords == 1 ) {
        // Got exactly 1 record
        Map availability_summary = [:]
        z_response?.records?.record?.recordData?.opacRecord?.holdings?.holding?.each { hld ->
          log.debug("${hld}");
          log.debug("${hld.circulations?.circulation?.availableNow}");
          log.debug("${hld.circulations?.circulation?.availableNow?.@value}");
          if ( hld.circulations?.circulation?.availableNow?.@value=='1' ) {
            log.debug("Available now");
            ItemLocation il = new ItemLocation( location: hld.localLocation, shelvingLocation:hld.shelvingLocation, callNumber:hld.callNumber )
  
            if ( result == null ) 
              result = il;
  
            availability_summary[hld.localLocation] = il;
          }
        }
  
        log.debug("At end, availability summary: ${availability_summary}");
      }
      else {
        log.debug("Title lookup returned ${z_response?.numberOfRecords} matches. Unable to determin availability");
      }
    }
    return result;
  }

  public ItemLocation z3950ItemByCQL(PatronRequest pr, String cql) {

    ItemLocation result = null;

    String z3950_server = getZ3950Server();

    if ( z3950_server != null ) {
      def z_response = HttpBuilder.configure {
        request.uri = 'http://reshare-mp.folio-dev.indexdata.com:9000'
      }.get {
          request.uri.path = '/'
          request.uri.query = ['x-target': z3950_server,
                               'x-pquery': cql,
                               'maximumRecords':'3' ]
      }
  
      log.debug("Got Z3950 response: ${z_response}");
  
      if ( z_response?.numberOfRecords == 1 ) {
        // Got exactly 1 record
        Map availability_summary = [:]
        z_response?.records?.record?.recordData?.opacRecord?.holdings?.holding?.each { hld ->
          log.debug("${hld}");
          log.debug("${hld.circulations?.circulation?.availableNow}");
          log.debug("${hld.circulations?.circulation?.availableNow?.@value}");
          if ( hld.circulations?.circulation?.availableNow?.@value=='1' ) {
            log.debug("Available now");
            ItemLocation il = new ItemLocation( location: hld.localLocation, shelvingLocation:hld.shelvingLocation, callNumber:hld.callNumber )
  
            if ( result == null ) 
              result = il;
  
            availability_summary[hld.localLocation] = il;
          }
        }
  
        log.debug("At end, availability summary: ${availability_summary}");
      }
      else {
        log.debug("CQL lookup(${cql}) returned ${z_response?.numberOfRecords} matches. Unable to determine availability");
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
        AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')
    
        String ncip_server_address = ncip_server_address_setting?.value ?: ncip_server_address_setting?.defValue
        String ncip_from_agency = ncip_from_agency_setting?.value ?: ncip_from_agency_setting?.defValue
        String ncip_app_profile = ncip_app_profile_setting?.value ?: ncip_app_profile_setting?.defValue
    
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
                      .setToAgency(ncip_from_agency)
                      .setFromAgency(ncip_from_agency)
                      .setApplicationProfileType(ncip_app_profile);
          JSONObject response = ncip_client.send(lookupUser);
    
          log.debug("Lookup user response: ${response}");
    
  
          // {"firstName":"Stacey",
          //  "lastName":"Conrad",
          //  "privileges":[{"value":"ACTIVE","key":"STATUS"},{"value":"STA","key":"PROFILE"}],
          //  "electronicAddresses":[{"value":"Stacey.Conrad@millersville.edu","key":"mailto"},{"value":"7178715869","key":"tel"}],
          //  "userId":"M00069192"}
          if ( ( response ) && ( ! response.has('problems') ) ) {
            JSONArray priv = response.getJSONArray('privileges')
            // Return a status of BLOCKED if the user is blocked, else OK for now
            result.status=(priv.find { it.key=='STATUS' })?.value.equalsIgnoreCase('BLOCKED') ? 'BLOCKED' : 'OK'
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

  /**
   * @param patron_id - the patron to look up
   * @return A map with the following keys {
   *   status:'OK'|'FAIL'
   *   userid
   *   givenName
   *   surname
   *   email
   * }
   */
  private Map old_ncip2LookupPatron(String patron_id) {
    Map result = [ status:'FAIL' ];
    log.debug("ncip2LookupPatron(${patron_id})");
    AppSetting ncip_server_address_setting = AppSetting.findByKey('ncip_server_address')
    AppSetting ncip_from_agency_setting = AppSetting.findByKey('ncip_from_agency')
    AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')

    String ncip_server_address = ncip_server_address_setting?.value ?: ncip_server_address_setting?.defValue
    String ncip_from_agency = ncip_from_agency_setting?.value ?: ncip_from_agency_setting?.defValue
    String ncip_app_profile = ncip_app_profile_setting?.value ?: ncip_app_profile_setting?.defValue

    if ( ( ncip_server_address != null ) &&
         ( ncip_from_agency != null ) &&
         ( ncip_app_profile != null ) ) {
      log.debug("Request patron from ${ncip_server_address}");

      StringWriter sw = new StringWriter();
      // sw << new StreamingMarkupBuilder().bind (makeNCIPLookupUserRequest('01TULI_INST','EZBORROW',patron_id))
      sw << new StreamingMarkupBuilder().bind (makeNCIPLookupUserRequest(ncip_from_agency, ncip_app_profile, patron_id))
      String message = sw.toString();

      log.debug("NCIP Request: ${message}");

      HttpBuilder.configure {
        request.uri = ncip_server_address
        request.contentType = XML[0]
        request.headers['accept'] = 'application/xml'
      }.post {
        request.body = message

        response.success { FromServer fs, Object body ->
            org.grails.databinding.xml.GPathResultMap mr = new org.grails.databinding.xml.GPathResultMap(body);
            log.debug("NCIP Response: ${mr}");
            result=[
              userid: mr.LookupUserResponse?.UserId?.UserIdentifierValue,
              givenName: mr.LookupUserResponse?.UserOptionalFields?.NameInformation?.PersonalNameInformation?.StructuredPersonalUserName?.GivenName,
              surname: mr.LookupUserResponse?.UserOptionalFields?.NameInformation?.PersonalNameInformation?.StructuredPersonalUserName?.Surname,
              status: 'OK'
            ]
            
            mr.LookupUserResponse?.UserOptionalFields?.UserAddressInformation.each { uai ->
              if ( ( uai.ElectronicAddress ) && ( uai.ElectronicAddress?.ElectronicAddressType == 'mailto' ) ) {
                result.email = uai.ElectronicAddress.ElectronicAddressData
              }
            }

            log.debug("Result of user lookup: ${result}");
            // result = JsonOutput.toJson(body);
        }
        response.failure { FromServer fs ->
          log.debug("Failure response from shared index - Lookup borrower info: ${fs.getStatusCode()} ${patron_id}");
        }
      }
    }
    else {
      log.error("MISSING CONFIGURATION FOR NCIP. Unable to perform patron lookup ${patron_id}/addr=${ncip_server_address}/from=${ncip_from_agency}/profile=${ncip_app_profile}");
    }

    return result
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
    // borrowerBarcode could be null or blank, error out if so
    if (borrowerBarcode != null && borrowerBarcode != '') {
        // set reason to ncip
      Map result = [reason: 'ncip'];

      log.debug("ncip2CheckoutItem(${itemBarcode},${borrowerBarcode})");
      AppSetting ncip_server_address_setting = AppSetting.findByKey('ncip_server_address')
      AppSetting ncip_from_agency_setting = AppSetting.findByKey('ncip_from_agency')
      AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')

      String ncip_server_address = ncip_server_address_setting?.value ?: ncip_server_address_setting?.defValue
      String ncip_from_agency = ncip_from_agency_setting?.value ?: ncip_from_agency_setting?.defValue
      String ncip_app_profile = ncip_app_profile_setting?.value ?: ncip_app_profile_setting?.defValue

      CirculationClient ncip_client = getCirculationClient(ncip_server_address);
      CheckoutItem checkoutItem = new CheckoutItem()
                    .setUserId(borrowerBarcode)
                    .setItemId(itemBarcode)
                    .setRequestId(requestId)
                    .setToAgency(ncip_from_agency)
                    .setFromAgency(ncip_from_agency)
                    .setApplicationProfileType(ncip_app_profile);
                    //.setDesiredDueDate("2020-03-18");

      JSONObject response = ncip_client.send(checkoutItem);
      log.debug("NCIP2 checkoutItem responseL ${response}");
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

  private String getZ3950Server() {
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
          AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')

          String ncip_server_address = ncip_server_address_setting?.value
          String ncip_from_agency = ncip_from_agency_setting?.value
          String ncip_app_profile = ncip_app_profile_setting?.value

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
                        .setToAgency(ncip_from_agency)
                        .setFromAgency(ncip_from_agency)
                        .setRequestedActionTypeString(requested_action)
                        .setApplicationProfileType(ncip_app_profile);
          JSONObject response = ncip_client.send(acceptItem);
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
      reason: 'spoofed'
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
          AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')

          String ncip_server_address = ncip_server_address_setting?.value
          String ncip_from_agency = ncip_from_agency_setting?.value
          String ncip_app_profile = ncip_app_profile_setting?.value

          CirculationClient ncip_client = getCirculationClient(ncip_server_address);
          CheckinItem checkinItem = new CheckinItem()
                        .setItemId(item_id)
                        .setToAgency(ncip_from_agency)
                        .setFromAgency(ncip_from_agency)
                        .includeBibliographicDescription()
                        .setApplicationProfileType(ncip_app_profile);
          JSONObject response = ncip_client.send(checkinItem);
          log.debug(response?.toString());
          if ( response.has('problems') ) {
            // If there is a problem block, something went wrong, so change response to false.
            result.result = false;
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
  
}
