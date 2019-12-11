package mod.rs

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.GlobalConfigService
import org.olf.rs.BackgroundTaskService;
import org.olf.rs.ReshareApplicationEventHandlerService
import grails.gorm.multitenancy.Tenants
import java.text.SimpleDateFormat

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
      org.grails.databinding.xml.GPathResultMap messageGpathXml = new org.grails.databinding.xml.GPathResultMap(iso18626_msg.request);
      log.debug("MESSAGE: ${messageGpathXml}")
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

            def xmlHeader = iso18626_msg.request.header
            def supIdType = xmlHeader.supplyingAgencyId.agencyIdType
            def supId = xmlHeader.supplyingAgencyId.agencyIdValue
            def reqAgencyIdType = xmlHeader.requestingAgencyId.agencyIdType
            def reqAgencyId = xmlHeader.requestingAgencyId.agencyIdValue
            def reqId = xmlHeader.requestingAgencyRequestId
            def timeRec = xmlHeader.timestamp

            log.debug("result of req_request ${req_result}");
            render( contentType:"text/xml" ) {
              makeConfirmationMessage(delegate )
            }
          }
        } else {
          log.warn("Tenant not found.")
          // TODO send back error response.
          render( contentType:"text/xml" ) {
            vxml( version:'2.1' ) {
              param( name:'hi' ) {
                sub('error')
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


  void exec ( def del, Closure c ) {
    c.rehydrate(del, c.owner, c.thisObject)()
  } 


  def makeConfirmationMessage(def del, String supId, String supIdType, String reqAgencyId, String reqAgencyIdType, String reqId, String timeRec, String status) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    def currentTime = dateFormatter.format(new Date())
    return {
      exec(del) {
        ISO18626Message( 'ill:version':'1.0',
                        'xmlns':'http://illtransactions.org/2013/iso18626',
                        'xmlns:ill': 'http://illtransactions.org/2013/iso18626',
                        'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                        'xsi:schemaLocation': 'http://illtransactions.org/2013/iso18626 http://illtransactions.org/schemas/ISO-18626-v1_1.xsd' ) {
          request {
            header {
            supplyingAgencyId {
              agencyIdType(supIdType)
              agencyIdValue(supId)
            }
            requestingAgencyId {
              agencyIdType(reqAgencyIdType)
              agencyIdValue(reqAgencyId)
            }
            timestamp(currentTime)
            requestingAgencyRequestId(reqId)
            multipleItemRequestId(null)
            timestampReceived(timeRec)
            messageStatus(status)
            }
          }
        }
      }
    }
  }
}
