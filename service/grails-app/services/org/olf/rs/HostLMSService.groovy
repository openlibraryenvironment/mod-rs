package org.olf.rs;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder


/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class HostLMSService {

  def lookup_strategies = [
    [ 
      name:'Local_identifier_By_Z3950',
      precondition: { pr -> return ( pr.systemInstanceIdentifier != null ) },
      stragegy: { pr, service -> return service.z3950ItemByIdentifier(pr) }
    ]
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
    log.debug("HostLMSService::placeHold(${instanceIdentifier},${itemIdentifier}");
    result.status='HoldPlaced'
    result
  }

  String determineBestLocation(PatronRequest pr) {
    log.debug("determineBestLocation(${pr})");

    String location = null;
    Iterator i = lookup_strategies.iterator();
    
    while ( ( location==null ) && ( i.hasNext() ) ) {
      def next_strategy = i.next();
      log.debug("Next lookup strategy: ${next_strategy.name}");
      if ( next_strategy.precondition(pr) == true ) {
        log.debug("Strategy passed precondition");
        location = next_strategy.stragegy(pr, this);
      }
      else {
        log.debug("Strategy did not pass precondition");
      }
    }
    
    return location;
  }
  
  public String z3950ItemByIdentifier(PatronRequest pr) {

    String result = null;

    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://temple-psb.alma.exlibrisgroup.com:1921%2F01TULI_INST&x-pquery=water&maximumRecords=1%27
    // TNS: tcp:aleph.library.nyu.edu:9992/TNSEZB
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=water&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=@attr%201=4%20%22Head%20Cases:%20stories%20of%20brain%20injury%20and%20its%20aftermath%22&maximumRecords=1%27
    // http://reshare-mp.folio-dev.indexdata.com:9000/?x-target=http://aleph.library.nyu.edu:9992%2FTNSEZB&x-pquery=@attr%201=12%20000026460&maximumRecords=1%27

    def z_response = HttpBuilder.configure {
      request.uri = 'http://reshare-mp.folio-dev.indexdata.com:9000'
    }.get {
        request.uri.path = '/'
        request.uri.query = ['x-target': 'http://aleph.library.nyu.edu:9992/TNSEZB',
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
          result = hld.localLocation
          availability_summary[hld.localLocation] = ( availability_summary[hld.localLocation] ?: 0 ) + 1;
        }
      }

      log.debug("At end, availability summary: ${availability_summary}");
    }

    return result;
  }

}

