package org.olf.rs

import groovy.util.logging.Slf4j
import org.xml.sax.SAXException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Schema
import javax.xml.validation.Validator

@Slf4j
class Iso18626MessageValidationService {

    void validateAgainstXSD(String xml) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            Schema schema = schemaFactory.newSchema(this.class.classLoader.getResource('xsd/ISO-18626-v1_2.xsd'))
            Validator validator = schema.newValidator()
            validator.validate(new StreamSource(new StringReader(xml)))
            log.debug("XSD validation successful")
        } catch (SAXException e) {
            log.error("XSD schema validation failed", e)
            throw e
        } catch (Exception e) {
            log.error("Error attempting to validate iso18626 message against schema  ${e.getLocalizedMessage()}")
            throw e
        }
    }
}