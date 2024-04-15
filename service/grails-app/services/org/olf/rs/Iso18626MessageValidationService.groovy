package org.olf.rs

import groovy.util.logging.Slf4j
import org.xml.sax.SAXException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Schema

@Slf4j
class Iso18626MessageValidationService {

    def validator = null

    void validateAgainstXSD(String xml) {
        try {
            if (validator == null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                Schema schema = schemaFactory.newSchema(this.class.classLoader.getResource('xsd/ISO-18626-v1_2.xsd'))
                validator = schema.newValidator()
            }
            validator.validate(new StreamSource(new StringReader(xml)))
            log.debug("XSD validation successful")
        } catch (SAXException e) {
            log.error("XSD schema validation failed", e)
            throw e
        }
    }
}