package org.olf.rs.statemodel

import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.custprops.CustomProperties
import com.k_int.web.toolkit.refdata.CategoryId
import com.k_int.web.toolkit.refdata.Defaults

/**
 *
 */
class AvailableAction implements MultiTenant<AvailableAction> {

  String id
  StateModel model
  Status fromState
  String actionCode

  // [S]ystem / [M]anual
  String triggerType

  // [S]ervice / [C]losure / [N]one
  String actionType

  String actionBody

  static constraints = {
             model (nullable: false)
         fromState (nullable: false)
        actionCode (nullable: false, blank:false)
       triggerType (nullable: true, blank:false)
        actionType (nullable: true, blank:false)
        actionBody (nullable: true, blank:false)
  }

  static mapping = {
                     id column : 'aa_id', generator: 'uuid2', length:36
                version column : 'aa_version'
                  model column : 'aa_model'
              fromState column : 'aa_from_state'
             actionCode column : 'aa_action_code'
            triggerType column : 'aa_trigger_type'
             actionType column : 'aa_action_type'
             actionBody column : 'aa_action_body'
  }


  public static AvailableAction ensure(String model, String state, String action, String triggerType=null, String actionType=null, String actionBody=null) {

    AvailableAction result = null;

    StateModel sm = StateModel.findByShortcode(model);
    if ( sm ) {
      Status s = Status.findByOwnerAndCode(sm, state);
      if ( s ) {
        result = AvailableAction.findByModelAndFromStateAndActionCode(sm,s,action) ?: 
                      new AvailableAction(
                                          model:sm, 
                                          fromState:s, 
                                          actionCode:action,
                                          triggerType: triggerType,
                                          actionType: actionType,
                                          actionBody: actionBody).save(flush:true, failOnError:true);
      }
    }
    return result;

  }

  public String toString() {
    return "AvailableAction(${id}) ${actionCode} ${triggerType} ${actionType} ${actionBody?.take(40)}".toString()
  }
}


