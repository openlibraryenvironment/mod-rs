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
class StateTransition implements MultiTenant<StateTransition> {

  String id
  StateModel model
  Status fromState
  String actionCode

  static constraints = {
             model (nullable: false, blank:false)
         fromState (nullable: false, blank:false)
        actionCode (nullable: false, blank:false)
  }

  static mapping = {
                     id column : 'str_id', generator: 'uuid2', length:36
                version column : 'str_version'
                  model column : 'str_model'
              fromState column : 'str_from_state'
             actionCode column : 'str_action_code'
  }


  public static StateTransition ensure(String model, String state, String action) {

    StateTransition result = null;

    StateModel sm = StateModel.findByShortcode(model);
    if ( sm ) {
      Status s = Status.findByOwnerAndCode(sm, state);
      if ( s ) {
        result = StateTransition.findByModelAndFromStateAndActionCode(sm,s,action) ?: 
                      new StateTransition(model:sm, fromState:s, actionCode:action).save(flush:true, failOnError:true);
      }
    }
    return result;

  }
}


