package org.olf.rs

import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import spock.lang.Specification
import groovy.util.XmlSlurper

@Slf4j
class ModmetastorageSharedIndexServiceSpec extends Specification implements ServiceUnitTest<ModmetastorageSharedIndexService> {
  def sampleData = '''<?xml version="1.0" encoding="UTF-8"?>
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
  <responseDate>2022-05-10T11:56:33.337500Z</responseDate>
  <request verb="GetRecord">http://dev-okapi.folio-dev.indexdata.com/meta-storage/oai?verb=GetRecord&amp;identifier=00007130-1b19-474d-b913-00c1afb3ca6f</request>
  <GetRecord>
    <record>
      <header>
        <identifier>oai:00007130-1b19-474d-b913-00c1afb3ca6f</identifier>
        <datestamp>2022-05-06T09:35:38Z</datestamp>
        <setSpec>title</setSpec>
      </header>
    <metadata>
<record xmlns="http://www.loc.gov/MARC21/slim">
  <leader>01083cam a2200289   4500</leader>
  <controlfield tag="001">a902097</controlfield>
  <controlfield tag="003">SIRSI</controlfield>
  <controlfield tag="005">20211009050004.0</controlfield>
  <controlfield tag="008">760922|||||||||ru            ||| | rusx </controlfield>
  <datafield tag="010" ind1=" " ind2=" ">
    <subfield code="a">   50046293</subfield>
  </datafield>
  <datafield tag="035" ind1=" " ind2=" ">
    <subfield code="a">(CStRLIN)CSUG3044823-B</subfield>
  </datafield>
  <datafield tag="035" ind1=" " ind2=" ">
    <subfield code="a">(OCoLC-M)7288257</subfield>
  </datafield>
  <datafield tag="035" ind1=" " ind2=" ">
    <subfield code="a">(OCoLC-I)272866596</subfield>
  </datafield>
  <datafield tag="040" ind1=" " ind2=" ">
    <subfield code="c">CSt  </subfield>
    <subfield code="d">CSt</subfield>
    <subfield code="d">OrLoB</subfield>
  </datafield>
  <datafield tag="050" ind1=" " ind2=" ">
    <subfield code="a">G700 1933.D5</subfield>
  </datafield>
  <datafield tag="241" ind1="0" ind2="0">
    <subfield code="a">Dnevniki Cheliuskintsev.</subfield>
  </datafield>
  <datafield tag="245" ind1="0" ind2="0">
    <subfield code="a">Dnevniki Cheliuskintsev.</subfield>
    <subfield code="c">[Po dnevnikam, zapisiam i vospominaniiam uchastnikov geroicheskoi ekspeditsii sostavili M. A. D'iakonov i E. B. Rubinchik]</subfield>
  </datafield>
  <datafield tag="260" ind1=" " ind2=" ">
    <subfield code="a">Leningrad, Khudozh. lit-ra,</subfield>
    <subfield code="c">1935.</subfield>
  </datafield>
  <datafield tag="300" ind1=" " ind2=" ">
    <subfield code="a">567 p.</subfield>
    <subfield code="c">24 cm.</subfield>
  </datafield>
  <datafield tag="596" ind1=" " ind2=" ">
    <subfield code="a">31</subfield>
  </datafield>
  <datafield tag="611" ind1=" " ind2="0">
    <subfield code="a">Cheli͡uskin Expedition</subfield>
    <subfield code="d">(1933-1934)</subfield>
    <subfield code="=">^A2809431</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="a">Rubinchik, E. B.</subfield>
    <subfield code="=">^A2865567</subfield>
  </datafield>
  <datafield tag="700" ind1=" " ind2=" ">
    <subfield code="a">D'iakonov, Mikhail Alekseevich.</subfield>
    <subfield code="?">UNAUTHORIZED</subfield>
  </datafield>
  <datafield tag="916" ind1=" " ind2=" ">
    <subfield code="a">DATE CATALOGED</subfield>
    <subfield code="b">19910824</subfield>
  </datafield>
  <datafield tag="999" ind1="1" ind2="1">
    <subfield code="i">00007130-1b19-474d-b913-00c1afb3ca6f</subfield>
    <subfield code="m">Dnevniki Cheliuskintsev</subfield>
    <subfield code="l">a902097</subfield>
    <subfield code="s">48f88baf-306b-4f65-9437-544965ae8a4f</subfield>
  </datafield>

  
  <datafield tag="999" ind1="1" ind2="1">
    <subfield code="i">123456</subfield>
    <subfield code="s">SYM-BOL</subfield>
    <subfield code="p">Will not lend</subfield>
  </datafield>
  <datafield tag="999" ind1="1" ind2="1">
    <subfield code="i">987654</subfield>
    <subfield code="s">SYM-BOL</subfield>
    <subfield code="p">Will lend</subfield>
  </datafield>
  <datafield tag="999" ind1="1" ind2="1">
    <subfield code="i">987654</subfield>
    <subfield code="s">OTH-ER</subfield>
  </datafield>
  <datafield tag="999" ind1="1" ind2="1">
    <subfield code="i">123456</subfield>
    <subfield code="s">THI-RD</subfield>
    <subfield code="p">Will not lend</subfield>
  </datafield>
 
 
  
  <datafield tag="999" ind1=" " ind2=" ">
    <subfield code="a">G700 1933 .D5</subfield>
    <subfield code="w">LC</subfield>
    <subfield code="c">1</subfield>
    <subfield code="i">36105036566896</subfield>
    <subfield code="d">12/21/2006</subfield>
    <subfield code="e">12/14/2006</subfield>
    <subfield code="l">STACKS</subfield>
    <subfield code="m">SAL3</subfield>
    <subfield code="r">Y</subfield>
    <subfield code="s">Y</subfield>
    <subfield code="t">STKS-MONO</subfield>
    <subfield code="u">9/22/1976</subfield>
    <subfield code="z">DIGI-SCAN</subfield>
  </datafield>
  <datafield tag="900" ind1=" " ind2=" ">
    <subfield code="5">POD</subfield>
    <subfield code="b">CSt</subfield>
  </datafield>
  <datafield tag="998" ind1=" " ind2=" ">
    <subfield code="5">POD</subfield>
    <subfield code="8">999.0\\p</subfield>
    <subfield code="a">G700 1933 .D5</subfield>
    <subfield code="i">36105036566896</subfield>
    <subfield code="m">SAL3</subfield>
  </datafield>
</record>
    </metadata>
    </record>
  </GetRecord>
</OAI-PMH>
''';

  def 'test '() {
    setup:
    def parsedSample = new XmlSlurper().parseText(sampleData);
    service.metaClass.fetchCluster = { String id -> parsedSample };

    when: 'We parse a date'
    def result = service.sharedIndexHoldings('123');

    then:
    log.info("parsed as ${result}");
    result.find { it.symbol == 'SYM-BOL' }.illPolicy == service.LENDABLE;
    result.find { it.symbol == 'OTH-ER' }.illPolicy == service.LENDABLE;
    result.find { it.symbol == 'THI-RD' }.illPolicy == 'Will not lend';
  }
}