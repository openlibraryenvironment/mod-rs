import groovy.xml.StreamingMarkupBuilder 
import static groovyx.net.http.HttpBuilder.configure
import static groovyx.net.http.ContentTypes.XML
import groovyx.net.http.*

public class Iso18626Client {


  void sendNewRequest(Map args) {

    StringWriter sw = new StringWriter();
    sw << new StreamingMarkupBuilder().bind(makeRequest(args))
    String req_as_str = sw.toString();

    def iso18626_response = configure {
      request.uri = args.service
      request.contentType = XML[0]
    }.post {
      // request.body = xml ( makeRequest(args) )
      request.body = req_as_str
    }
  }
    
  void dumpRequest(Map args) {
    System.out << new StreamingMarkupBuilder().bind(makeRequest(args))
  }

  // Return a closure that describes the XML for a request
  def makeRequest(Map args) {
    String[] sup_info = args.supplier.split(':');
    String[] req_info = args.requester.split(':');

    return{
      ISO18626Message( 'ill:version':'1.0',
                       'xmlns':'http://illtransactions.org/2013/iso18626',
                       'xmlns:ill': 'http://illtransactions.org/2013/iso18626',
                       'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                       'xsi:schemaLocation': 'http://illtransactions.org/2013/iso18626 http://illtransactions.org/schemas/ISO-18626-v1_1.xsd' ) {
        request {
          header {
            supplyingAgencyId {
              agencyIdType(sup_info[0])
              agencyIdValue(sup_info[1])
            }
            requestingAgencyId {
              agencyIdType(req_info[0])
              agencyIdValue(req_info[1])
            }
            timestamp('2014-03-17T09:30:47.0Z')
            requestingAgencyRequestId('1234')
          }
          bibliographicInfo {
            supplierUniqueRecordId('1234')
            title(args.title)
          }
          serviceInfo {
            serviceType('Loan')
            serviceLevel('Loan')
            needBeforeDate('2014-05-01T00:00:00.0Z')
            anyEdition('Y')
          }
        }
      }
    }
  }
}

