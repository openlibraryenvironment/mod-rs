package org.olf.rs.hostlms

import org.olf.rs.circ.client.CirculationClient
import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.lms.ItemLocation
import org.olf.rs.logging.IHoldingLogDetails
import org.olf.rs.settings.ISettings

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class PolarisHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [protocol: "NCIP1", useNamespace: false]).circulationClient;
  }

  // Given the record syntax above, process response records as Opac recsyn. If you change the recsyn string above
  // you need to change the handler here. SIRSI for example needs to return us marcxml with a different location for the holdings
  @Override
  protected List<ItemLocation> extractAvailableItemsFrom(z_response, String reason, IHoldingLogDetails holdingLogDetails) {
    log.debug("Extract holdings from Polaris marcxml record ${z_response}");
    if ( z_response?.numberOfRecords != 1 ) {
      log.warn("Multiple records seen in response from Polaris Z39.50 server, unable to extract available items. Record: ${z_response}");
      return null;
    }

    List<ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.record != null ) {
      availability_summary = extractAvailableItemsFromMARCXMLRecord(z_response?.records?.record?.recordData?.record, reason, holdingLogDetails);
    }
    return availability_summary;

  }

  @Override
  protected String getHoldingsQueryRecsyn() {
    return 'marcxml';
  }

  /**
   * N.B. this method may be overriden in the LMS specific subclass - check there first - this is the default implementation
   */
  public List<ItemLocation> extractAvailableItemsFromMARCXMLRecord(record, String reason, IHoldingLogDetails holdingLogDetails) {
//    <zs:searchRetrieveResponse>
//        <zs:numberOfRecords>1</zs:numberOfRecords>
//        <zs:records>
//          <zs:record>
//            <zs:recordSchema>marcxml</zs:recordSchema>
//            <zs:recordXMLEscaping>xml</zs:recordXMLEscaping>
//            <zs:recordData>
//             <record>
//                 <leader>02370cam a2200541Ii 4500</leader>
//                 <controlfield tag="008">140408r20141991nyua j 001 0 eng d</controlfield>
//                 <datafield tag="852" ind1=" " ind2=" ">
//                    <subfield code="a">Main Library</subfield>
//                    <subfield code="b">Juvenile nonfiction shelves</subfield>
//                    <subfield code="h">j574.92 Sill</subfield>
//                    <subfield code="o">3</subfield>
//                    <subfield code="p">31256015853493</subfield>
//                    <subfield code="r">In</subfield>
//                    <subfield code="w">Hardcover</subfield>
//                    <subfield code="y">1</subfield>
//                    <subfield code="1">Aug 24 2023 </subfield>
//                    <subfield code="7">True</subfield>
//                    <subfield code="9">0</subfield>
//                  </datafield>
//                <datafield tag="852" ind1=" " ind2=" "> (using 1=12 775848) <subfield code="a">Main Library</subfield>
//                    <subfield code="b">Juvenile nonfiction shelves</subfield>
//                    <subfield code="h">j394.268 Tate</subfield>
//                    <subfield code="o">3</subfield>
//                    <subfield code="p">31256016306970</subfield>
//                    <subfield code="r">Out</subfield>
//                    <subfield code="u">Dec 15 2023 </subfield>
//                    <subfield code="w">Hardcover</subfield>
//                    <subfield code="y">1</subfield>
//                    <subfield code="1">Nov 17 2023 </subfield>
//                    <subfield code="7">True</subfield>
//                     <subfield code="9">0</subfield>
//                </datafield>
//                <datafield tag="852" ind1=" " ind2=" "> (using 1=12 814873) <subfield code="a">Main Library</subfield>
//                    <subfield code="b">Adult nonfiction</subfield>
//                    <subfield code="h">394.268 Sa56l</subfield>
//                    <subfield code="o">3</subfield>
//                    <subfield code="p">31256016640014</subfield>
//                    <subfield code="r">Held</subfield>
//                    <subfield code="w">Hardcover</subfield>
//                    <subfield code="y">1</subfield>
//                    <subfield code="1">Dec 4 2023 </subfield>
//                    <subfield code="7">True</subfield>
//                    <subfield code="9">0</subfield>
//                </datafield>

    log.debug("PolarisHostLMSService extracting available items from record ${record}");
    List<ItemLocation> availability_summary = []
    holdingLogDetails.newRecord();
    record.datafield.each { df ->
      if ( df.'@tag' == "852" ) {
        holdingLogDetails.holdings(df);
        Map<String,String> tag_data = [:]
        df.subfield.each { sf ->
          if ( sf.'@code' != null ) {
            tag_data[ sf.'@code'.toString().trim() ] = sf.text().trim()
          }
        }

        log.debug("Found holdings tag: ${df} ${tag_data}");
        try {
          if ( ![ 'In' ].contains(tag_data['r']) ) {
            // $r contains item status which is not requestable
            log.debug("Item status not requestable - ${tag_data['r']}");
          }
          else {
            log.debug("Assuming ${tag_data['r']} is requestable status to update extractAvailableItemsFromMARCXMLRecord if not the case");
            availability_summary << new ItemLocation(
                    location: tag_data['b'],
                    shelvingLocation: tag_data['a'],
                    callNumber: tag_data['h'],
                    itemId: tag_data['p'] )
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
