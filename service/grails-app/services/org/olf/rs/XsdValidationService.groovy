package org.olf.rs

import groovy.util.logging.Slf4j
import org.xml.sax.SAXException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

@Slf4j
class XsdValidationService {

  Validator validator

  def validateXmlAgainstXsd(String xmlString) {
    try {
      if (validator == null) {
        def factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        def schema = factory.newSchema(this.class.classLoader.getResource("xsd/ISO-18626-v1_2.xsd"))
        validator = schema.newValidator()
      }
      validator.validate(new StreamSource(new StringReader(xmlString)))
      log.info("Validation successful")
      return true
    } catch (SAXException e) {
      log.error("Validation error: ${e.message}")
      return false
    }
  }

  // Clever bit of wizardry to allow us to inject the calling class into the builder
  void exec ( def del, Closure c ) {
    c.rehydrate(del, c.owner, c.thisObject)()
  }
}


