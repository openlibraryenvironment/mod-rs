package org.olf.rs


import org.olf.rs.iso18626.*

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import static org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService.*

class ConfirmationMessageService {

  Iso18626MessageValidationService iso18626MessageValidationService
  JAXBContext context = JAXBContext.newInstance(ObjectFactory.class)

  def confirmationMessageReadable(def confirmationMessage) {
    StringWriter sw = new StringWriter()
    getMarshaller().marshal(confirmationMessage, sw)
    String message = sw.toString()
    iso18626MessageValidationService.validateAgainstXSD(message)
    return message
  }

  Marshaller getMarshaller() {
      Marshaller marshaller;

      marshaller = context.createMarshaller()
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, Iso18626Constants.SCHEMA_LOCATION)
      marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new IllNamespacePrefixMapper())

      return marshaller
  }

  // This method creates a confirmation message
  def makeConfirmationMessage(def req_result) {
    ISO18626Message iso18626Message = new ISO18626Message()
    iso18626Message.setVersion(Iso18626Constants.VERSION)
    switch (req_result.messageType) {
      case Iso18626Constants.REQUEST:
          RequestConfirmation confirmation = new RequestConfirmation()
          confirmation.setConfirmationHeader(makeConfirmationHeader(req_result))
          confirmation.setErrorData(makeErrorData(req_result))
          iso18626Message.setRequestConfirmation(confirmation)
        break
      case Iso18626Constants.SUPPLYING_AGENCY_MESSAGE:
          SupplyingAgencyMessageConfirmation confirmation = new SupplyingAgencyMessageConfirmation()
          confirmation.setConfirmationHeader(makeConfirmationHeader(req_result))
          if (req_result.reasonForMessage) {
            confirmation.setReasonForMessage(TypeReasonForMessage.fromValue(req_result.reasonForMessage))
          }
          confirmation.setErrorData(makeErrorData(req_result))
          iso18626Message.setSupplyingAgencyMessageConfirmation(confirmation)
        break
      case Iso18626Constants.REQUESTING_AGENCY_MESSAGE:
          RequestingAgencyMessageConfirmation confirmation = new RequestingAgencyMessageConfirmation()
          confirmation.setConfirmationHeader(makeConfirmationHeader(req_result))
          if (req_result.action) {
            confirmation.setAction(TypeAction.fromValue(req_result.action))
          }
          confirmation.setErrorData(makeErrorData(req_result))
          iso18626Message.setRequestingAgencyMessageConfirmation(confirmation)
        break
      default:
          log.error("UNHANDLED req_result.messageType : ${req_result.messageType}")
          throw new RuntimeException("UNHANDLED req_result.messageType : ${req_result.messageType}")
    }

    return iso18626Message
  }

  ConfirmationHeader makeConfirmationHeader(def req_result) {
    ConfirmationHeader confirmationHeader = new ConfirmationHeader()
    if(req_result.supIdType || req_result.supId) {
        TypeAgencyId supplyingAgencyId = new TypeAgencyId()
        supplyingAgencyId.setAgencyIdType(toTypeSchemeValuePair(req_result.supIdType))
        supplyingAgencyId.setAgencyIdValue(req_result.supId)
        confirmationHeader.setSupplyingAgencyId(supplyingAgencyId)
    }

    if(req_result.reqAgencyIdType || req_result.reqAgencyId) {
        TypeAgencyId requestingAgencyId = new TypeAgencyId()
        requestingAgencyId.setAgencyIdType(toTypeSchemeValuePair(req_result.reqAgencyIdType))
        requestingAgencyId.setAgencyIdValue(req_result.reqAgencyId)
        confirmationHeader.setRequestingAgencyId(requestingAgencyId)
    }

    confirmationHeader.setTimestamp(ZonedDateTime.now())
    confirmationHeader.setRequestingAgencyRequestId(req_result.reqId)
    confirmationHeader.setTimestampReceived(toZonedDateTime(req_result.timeRec))
    confirmationHeader.setMessageStatus(req_result.status == STATUS_OK ? TypeMessageStatus.OK : TypeMessageStatus.ERROR)
    return confirmationHeader
  }

  TypeSchemeValuePair toTypeSchemeValuePair(def text){
    TypeSchemeValuePair valuePair = new TypeSchemeValuePair()
    valuePair.setValue(text)
    return valuePair
  }

  ZonedDateTime toZonedDateTime(String dateString) {
    return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
  }

  ErrorData makeErrorData(def req_result) {
    ErrorData errorData = null
    if(req_result.errorType){
      errorData = new ErrorData()
      errorData.setErrorType(toErrorType(req_result.errorType))
      errorData.setErrorValue("$req_result.errorType: " + (req_result.errorValue ? req_result.errorValue : ""))
    }
    return errorData
  }

  TypeErrorType toErrorType(error) {
      switch (error) {
          case ERROR_TYPE_BADLY_FORMED_MESSAGE:
          case ERROR_TYPE_NO_XML_SUPPLIED:
          case ERROR_TYPE_REQUEST_ID_ALREADY_EXISTS:
              return TypeErrorType.BADLY_FORMED_MESSAGE
          case ERROR_TYPE_NO_ACTIVE_REQUEST:
          case ERROR_TYPE_NO_CANCEL_VALUE:
          case ERROR_TYPE_NO_ERROR:
          case ERROR_TYPE_INVALID_CANCEL_VALUE:
          case ERROR_TYPE_UNABLE_TO_FIND_REQUEST:
          case ERROR_TYPE_UNABLE_TO_PROCESS:
          case ERROR_TYPE_INVALID_PATRON_REQUEST:
              return TypeErrorType.UNRECOGNISED_DATA_VALUE
          case ERROR_TYPE_NO_ACTION:
          case ERROR_TYPE_NO_REASON_FOR_MESSAGE:
              return TypeErrorType.UNSUPPORTED_ACTION_TYPE
          case ERROR_TYPE_NO_CONFIRMATION_ELEMENT_IN_RESPONSE:
              return TypeErrorType.UNRECOGNISED_DATA_ELEMENT
          default: return TypeErrorType.UNRECOGNISED_DATA_VALUE
      }
  }

  // Clever bit of wizardry to allow us to inject the calling class into the builder
  void exec ( def del, Closure c ) {
    c.rehydrate(del, c.owner, c.thisObject)()
  }
}


