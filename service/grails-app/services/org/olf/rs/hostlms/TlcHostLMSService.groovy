
public class TlcHostLMSService extends BaseHostLMSService {


@Override
  protected Map<String, ItemLocation> extractAvailableItemsFrom(z_response) {
    log.debug("Extract holdings from marcxml record ${z_response}");

    Map<String, ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.record != null ) {
      availability_summary = extractAvailableItemsFromMARCXMLRecord(z_response?.records?.record?.recordData?.record);
    }
    return availability_summary;

  }



  
  @Override
  public Map<String, ItemLocation> extractAvailableItemsFromMARCXMLRecord(record) {
    //<zs:searchRetrieveResponse xmlns:zs="http://docs.oasis-open.org/ns/search-ws/sruResponse">
    //  <zs:numberOfRecords>1359</zs:numberOfRecords>
    //  <zs:records>
    //    <zs:record>
    //      <zs:recordSchema>marcxml</zs:recordSchema>
    //      <zs:recordXMLEscaping>xml</zs:recordXMLEscaping>
    //      <zs:recordData>
    //        <record xmlns="http://www.loc.gov/MARC21/slim">
    //        <leader>02304nam a22005051i 4500</leader>
    //        <controlfield tag="001">ebc1823214</controlfield>
    //        <controlfield tag="003">NhCcYBP</controlfield>
    //        <controlfield tag="005">20170628225136.0</controlfield>
    //        <controlfield tag="006">m |o d | </controlfield>
    //        <controlfield tag="007">cr |n|||||||||</controlfield>
    //        <controlfield tag="008">170604s2014 ilua ob 001 0 eng d</controlfield>
    //        <datafield tag="982" ind1=" " ind2=" ">
    //          <subfield code="1">y</subfield>
    //          <subfield code="a">PDA</subfield>
    //          <subfield code="b">Snowden Library</subfield>
    //          <subfield code="c">E-BOOK (DDA)</subfield>
    //          <subfield code="i">ebc1823214</subfield>
    //          <subfield code="m"/>
    //          <subfield code="s">A</subfield>
    //        </datafield>
    Map<String, ItemLocation> availability_summary = [:];
    record.datafield.each { df ->
      if( df.'@tag' == "982") {
        Map<String,String> tag_data = [:];
        df.subfield.each { sf ->
          if( sf.@code != null ) {
            tag_data[ sf.'@code'.toString() ] = sf.toString();
          }
        }
        log.debug("Found holdings (982) tag: ${df} ${tag_data}");
        try {
          if( tag_data['1'] == 'y') {
            log.debug("Available now");
            def location = tag_data['b'];
            def shelvingLocation = tag_data['c'];
            ItemLocation il = new ItemLocation( location: location, shelvingLocation: shelvingLocation, null );
            availability_summary[location] = il;
          }
        } catch(Exception e) {
          log.debug("Unable to parse holdings (982): ${e.message}");
        }
        
      }
    }
    log.debug("Tlc Host availability: ${availability_summary}");
    return availability_summary;

  }

}