package org.olf.rs

import grails.gorm.MultiTenant;

import com.k_int.web.toolkit.refdata.CategoryId;
import com.k_int.web.toolkit.refdata.RefdataValue;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.referenceData.RefdataValueData;

class PatronRequestLoanCondition implements MultiTenant<PatronRequestLoanCondition> {

  // Internal id of the message
  String id

  static belongsTo = [patronRequest : PatronRequest]

  // Default date metadata maintained by the db
  Date dateCreated
  Date lastUpdated

  /*
   * Right now, when a loan condition is rejected, this results in the request being cancelled with the supplier.
   * Hence we can safely assume that false OR null == pending and true == accepted.
   * If this behaviour changes in future we will be able to separate out null == pending and false == rejected
   */
  Boolean accepted = false;

  /* The actual code sent along the ISO18626 message, such as "LibraryUseOnly", "SpecCollSupervReq" or "Other"
   * THIS MIGHT NOT BE HUMAN READABLE, and we might not have refdata corresponding to it in the receiver's system,
   * so a translation lookup has to be done when displaying on the request. In cases where we know the refdata exists
   * we can lookup that instead and use the label from there.
   */
  String code

  BigDecimal cost

  @CategoryId(RefdataValueData.VOCABULARY_CURRENCY_CODES)
  RefdataValue costCurrency

  // Note passed along with the code, human readable content for explanation of user defined codes or extension of what the code says
  String note

  Symbol relevantSupplier
  String supplyingInstitutionSymbol


  static constraints = {
    accepted (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronRequest (nullable: true)
    code( nullable: true)
    cost( nullable: true)
    costCurrency( nullable: true)
    note( nullable: true)
    relevantSupplier (nullable: true)
    supplyingInstitutionSymbol (nullable: true)
  }

  static mapping = {
                  id column : 'prlc_id', generator: 'uuid2', length:36
             version column : 'prlc_version'
         dateCreated column : 'prlc_date_created'
         lastUpdated column : 'prlc_last_updated'
                code column : 'prlc_code'
                cost column : 'prlc_cost'
        costCurrency column : 'prlc_cost_currency_fk'
                note column : 'prlc_note'
       patronRequest column : 'prlc_patron_request_fk'
    relevantSupplier column : 'prlc_relevant_supplier_fk'
            accepted column : 'prlc_accepted'
    supplyingInstitutionSymbol column : 'prlc_sup_inst_symbol'
  }
}