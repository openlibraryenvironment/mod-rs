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


/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class AlephHostLMSService implements HostLMSActions {

  def lookup_strategies = [
    [ 
      name:'Local_identifier_By_Z3950',
      precondition: { pr -> return ( pr.systemInstanceIdentifier != null ) },
      stragegy: { pr, service -> return service.z3950ItemByIdentifier(pr) }
    ],
    [ 
      name:'Local_identifier_By_Title',
      precondition: { pr -> return ( pr.title != null ) },
      stragegy: { pr, service -> return service.z3950ItemByTitle(pr) }
    ],

  ]

  void validatePatron(String patronIdentifier) {

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
    log.debug("AlephHostLMSService::placeHold(${instanceIdentifier},${itemIdentifier}");
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
      // log.debug("Sending system id query ${z3950_proxy}?x-target=http://temple-psb.alma.exlibrisgroup.com:1921/01TULI_INST&x-pquery=@attr 1=12 ${+pr.systemInstanceIdentifier}");
      log.debug("Sending system id query ${z3950_proxy}?x-target=${z3950_server}&x-pquery=@attr 1=12 ${+pr.systemInstanceIdentifier}");

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

  public Map lookupPatron(String patron_id) {
    log.debug("lookupPatron(${patron_id})");
    Map result = [ status: 'OK' ];
    AppSetting borrower_check_setting = AppSetting.findByKey('borrower_check')
    if ( ( borrower_check_setting != null ) && ( borrower_check_setting.value != null ) )  {
      switch ( borrower_check_setting.value ) {
        case 'ncip2':
          result = ncip2LookupPatron(patron_id)
          break;
        default:
          log.debug("Borrower check - no action, config ${borrower_check}");
          break;
      }
    }
    else {
      log.warn('borrower check not configured');
    }

    log.debug("AlephHostLMSService::lookupPatron(${patron_id}) returns ${result}");
    return result
  }

  private Map ncip2LookupPatron(String patron_id) {
    Map result = [ status:'FAIL' ];
    log.debug("ncip2LookupPatron(${patron_id})");
    AppSetting ncip_server_address_setting = AppSetting.findByKey('ncip_server_address')
    AppSetting ncip_from_agency_setting = AppSetting.findByKey('ncip_from_agency_config')
    AppSetting ncip_app_profile_setting = AppSetting.findByKey('ncip_app_profile')

    String ncip_server_address = ncip_server_address_setting?.value
    String ncip_from_agency = ncip_from_agency_setting?.value
    String ncip_app_profile = ncip_app_profile_setting?.value

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


  public Map checkoutItem(String itemBarcode, 
                          String borrowerBarcode, 
                          Symbol requesterDirectorySymbol) {
    log.debug("checkoutItem(${itemBarcode},${borrowerBarcode},${requesterDirectorySymbol})");
    return [
      result:ncip2CheckoutItem(itemBarcode, borrowerBarcode)
    ]
  }

  public boolean ncip2CheckoutItem(String itemBarcode, String borrowerBarcode) {
    log.debug("ncip2CheckoutItem(${itemBarcode},${borrowerBarcode})");
    return false;
  }

  private String getZ3950Server() {
    return AppSetting.findByKey('z3950_server_address')?.value
  }
}
