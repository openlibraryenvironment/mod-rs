
import org.olf.rs.RabbitService;
import org.olf.rs.rabbit.Queue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

class ChasController {

	def TestXML() {
		String xml = null;

		JacksonXmlModule module = new JacksonXmlModule();
		
		// Noddy test of reading and writing xml
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, false);

		// Set the date format we want to use
		xmlMapper.configOverride(java.util.Date.class).setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
		
		try {
			// Test files generated from the xsd file
//			String messageXML = new File('D:/Source/Folio/ReShare/mod-rs/src/test/resources/ISO18626/request.xml').text;
//			String messageXML = new File('D:/Source/Folio/ReShare/mod-rs/src/test/resources/ISO18626/requestConfirmation.xml').text;
//			String messageXML = new File('D:/Source/Folio/ReShare/mod-rs/src/test/resources/ISO18626/requestingAgencyMessage.xml').text;
//			String messageXML = new File('D:/Source/Folio/ReShare/mod-rs/src/test/resources/ISO18626/requestingAgencyMessageConfirmation.xml').text;
//			String messageXML = new File('D:/Source/Folio/ReShare/mod-rs/src/test/resources/ISO18626/supplyingAgencyMessage.xml').text;
			String messageXML = new File('D:/Source/Folio/ReShare/mod-rs/src/test/resources/ISO18626/supplyingAgencyMessageConfirmation.xml').text;

			// from xml
			xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			xmlMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
//			com.k_int.folio.rs.models.ISO18626.Request.Message value = xmlMapper.readValue(messageXML, com.k_int.folio.rs.models.ISO18626.Request.Message.class);
//			com.k_int.folio.rs.models.ISO18626.Request.Confirmation value = xmlMapper.readValue(messageXML, com.k_int.folio.rs.models.ISO18626.Request.Confirmation.class);
//			com.k_int.folio.rs.models.ISO18626.RequestAgencyMessage.Message value = xmlMapper.readValue(messageXML, com.k_int.folio.rs.models.ISO18626.RequestAgencyMessage.Message.class);
//			com.k_int.folio.rs.models.ISO18626.RequestAgencyMessage.Confirmation value = xmlMapper.readValue(messageXML, com.k_int.folio.rs.models.ISO18626.RequestAgencyMessage.Confirmation.class);
//			com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage.Message value = xmlMapper.readValue(messageXML, com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage.Message.class);
			com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage.Confirmation value = xmlMapper.readValue(messageXML, com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage.Confirmation.class);

			// To xml
			xmlMapper.setSerializationInclusion(Include.NON_EMPTY);
			xml = xmlMapper.writeValueAsString(value);
		}
		catch (Exception e) {
			xml = "<error>" + e.getMessage() + "</error>"
			e.printStackTrace();
		}

		// Sump it out to the console
		System.out.println(xml);

		// Return the result as xml
		render(text: xml, contentType: "text/xml", encoding: "UTF-8");
	}

	RabbitService rabbitService;
	
	def CheckRabbit() {
		render(text: rabbitService.Running() ? "Rabbit is running" : "Rabbit is not running, is there a problem", contentType: "text/plain", encoding: "UTF-8");
	}

	def TestRabbit() {
		boolean successful = rabbitService.Send(Queue.RESHARE_ACTION, "Message-1", '{"field1":"contents of field1"}', Queue.PROCESSOR_RESPONSE);
		render(text: successful ? "Successfully sent a message to rabbit" : "Failed to send the message to rabbit, check the log file and retry later", contentType: "text/plain", encoding: "UTF-8");
	}
}
