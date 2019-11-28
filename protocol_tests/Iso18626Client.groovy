import groovy.xml.StreamingMarkupBuilder 

public class Iso18626Client {
  void sendNewRequest(Map args) {
    String[] sup_info = args.supplier.split(':');
    String[] req_info = args.requester.split(':');
  
    System.out << new StreamingMarkupBuilder().bind {
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

