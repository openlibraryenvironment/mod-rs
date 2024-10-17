package org.olf.rs.sharedindex

import org.olf.rs.AvailabilityStatement
import org.olf.rs.SharedIndexActions

public class MockSharedIndexService implements SharedIndexActions {
    @Override
    List<AvailabilityStatement> findAppropriateCopies(Map description) {
        return null
    }

    @Override
    List<String> fetchSharedIndexRecords(Map description) {
        String xmlString = """<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
  <responseDate>2024-09-19T20:08:54.468130932Z</responseDate>
  <request verb="GetRecord"></request>
  <GetRecord>
    <record>
      <header>
        <identifier></identifier>
        <datestamp>2024-04-24T21:44:47Z</datestamp>
        <setSpec>goldrush</setSpec>
      </header>
    <metadata>

<record xmlns="http://www.loc.gov/MARC21/slim">
  <leader>05095cam a2200829 i 4500</leader>
  <controlfield tag="001">3aa73fef-09fd-4f71-9246-ec0e50d35ef4</controlfield>
  <controlfield tag="005">20240919000000.0</controlfield>
  <controlfield tag="008">130328t20142014caua     b    001 0 eng  </controlfield>
  <datafield tag="010" ind1=" " ind2=" ">
    <subfield code="a">  2013008876</subfield>
  </datafield>
  <datafield tag="015" ind1=" " ind2=" ">
    <subfield code="a">GBB339317</subfield>
    <subfield code="2">bnb</subfield>
  </datafield>
  <datafield tag="016" ind1="7" ind2=" ">
    <subfield code="a">016321337</subfield>
    <subfield code="2">Uk</subfield>
  </datafield>
  <datafield tag="019" ind1=" " ind2=" ">
    <subfield code="a">870525282</subfield>
    <subfield code="a">907471440</subfield>
    <subfield code="a">1165420346</subfield>
    <subfield code="a">1170959138</subfield>
  </datafield>
  <datafield tag="020" ind1=" " ind2=" ">
    <subfield code="a">1452242569</subfield>
    <subfield code="q">(pbk. ;</subfield>
    <subfield code="q">alk. paper)</subfield>
  </datafield>

  <datafield tag="100" ind1="1" ind2=" ">
    <subfield code="a">Yin, Robert K.</subfield>
    <subfield code="e">author.</subfield>
  </datafield>
  <datafield tag="245" ind1="1" ind2="0">
    <subfield code="a">Case study research :</subfield>
    <subfield code="b">design and methods /</subfield>
    <subfield code="c">Robert K. Yin, COSMOS Corporation.</subfield>
  </datafield>
  <datafield tag="250" ind1=" " ind2=" ">
    <subfield code="a">5 edition.</subfield>
  </datafield>

</record>
    </metadata>
    </record>
  </GetRecord>
</OAI-PMH>""";
        return [ xmlString ];
    }
}
