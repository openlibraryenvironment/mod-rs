package mod.rs

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.GlobalConfigService
import org.olf.rs.BackgroundTaskService;
import org.olf.rs.ReshareApplicationEventHandlerService
import grails.gorm.multitenancy.Tenants

class iso18626Controller {

  GrailsApplication grailsApplication
  GlobalConfigService globalConfigService
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService

  def index() {
    def result=[status:'ok']
    log.debug("iso18626Controller::index(${params})");

    try {
      log.debug("XML: ${request.XML}");
      def iso18626_msg = request.XML;
      String recipient;
      String tenant;

      if ( iso18626_msg.request != null ) {
        log.debug("Process inbound request message");
        // Look in request.header.supplyingAgencyId for the intended recipient
        recipient = getSymbolFor(iso18626_msg.request.header.supplyingAgencyId);
        tenant = globalConfigService.getTenantForSymbol(recipient);
        if ( tenant ) {
          log.debug("incoming request for ${tenant}");
          Tenants.withId(tenant+'_mod_rs') {
            org.grails.databinding.xml.GPathResultMap mr = new org.grails.databinding.xml.GPathResultMap(iso18626_msg.request);
            def req_result = reshareApplicationEventHandlerService.handleRequestMessage(mr);

            log.debug("result of req_request ${req_result}");
            render( contentType:"text/xml" ) {
              vxml( version:'2.1' ) {
                param( name:'hi' ) {
                  sub('hello')
                }
              }
            }
          }
        }
      }
      else if ( iso18626_msg.supplyingAgencyMessage != null ) {
        log.debug("Process inbound supplyingAgencyMessage message");
        // Look in request.header.requestingAgencyId for the intended recipient
        recipient = getSymbolFor(iso18626_msg.request.header.requestingAgencyId);
        tenant = globalConfigService.getTenantForSymbol(recipient);
        if ( tenant ) {
          log.debug("incoming request for ${tenant}");
        }

        render( contentType:"text/xml" ) {
          vxml( version:'2.1' ) {
            var( name:'hi', expr:call.message )
          }
        }
      }
      else if ( iso10626_msg.requestingAgencyMessageConfirmation != null ) {
        log.debug("Process inbound requestingAgencyMessage message");
        // Look in request.header.supplyingAgencyId for the intended recipient
        recipient = getSymbolFor(iso18626_msg.request.header.supplyingAgencyId);
        tenant = globalConfigService.getTenantForSymbol(recipient);
        if ( tenant ) {
          log.debug("incoming request for ${tenant}");
        }

        render( contentType:"text/xml" ) {
          vxml( version:'2.1' ) {
            var( name:'hi', expr:call.message )
          }
        }
      }

    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

  }

  def symbol() {
    def result=[status:'ok']
    log.debug("iso18626Controller::symbol(${params})");
    render result as JSON
  }

  private String getSymbolFor(path) {
    String result = null;
    if ( path.agencyIdType != null && path.agencyIdValue != null ) {
      result = "${path.agencyIdType.text()}:${path.agencyIdValue.text()}".toString()
    }
    else {
      log.error("Missing agency id type or value");
    }
    log.debug("Returning symbol : ${result}");
    return result;
  }

  def requestConfirmation(String supIdType, 
                          String supId, 
                          String reqAgencyIdType, 
                          String reqAgencyId, 
                          String reqId, 
                          String status) {
    return [
      requestConfirmation:[
        confirmationHeader:confirmationHeader(supIdType,supId,reqAgencyIdType,reqAgencyId,reqId,status),
        errorData:[
        ]
      ]
    ]
  }

  def confirmationHeader(String supIdType, String supId, String reqAgencyIdType, String reqAgencyId, String reqId, String status) {
    // status OK | ERROR
    return [
      supplyingAgencyId:[
        agencyIdType:supIdType,
        agencyIdValue:supId
      ],
      requestingAgencyId:[
        agencyIdType:reqAgencyIdType,
        agencyIdValue:reqAgencyId
      ],
      timestamp:null,
      requestingAgencyRequestId:reqId,
      multipleItemRequestId:null,
      timestampReceived:null,
      messageStatus:status
    ]
  }
}
