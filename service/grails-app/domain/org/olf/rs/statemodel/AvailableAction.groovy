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

  static constraints = {
             model (nullable: false, blank:false)
         fromState (nullable: false, blank:false)
        actionCode (nullable: false, blank:false)
  }

  static mapping = {
                     id column : 'aa_id', generator: 'uuid2', length:36
                version column : 'aa_version'
                  model column : 'aa_model'
              fromState column : 'aa_from_state'
             actionCode column : 'aa_action_code'
  }


  public static AvailableAction ensure(String model, String state, String action) {

    AvailableAction result = null;

    StateModel sm = StateModel.findByShortcode(model);
    if ( sm ) {
      Status s = Status.findByOwnerAndCode(sm, state);
      if ( s ) {
        result = AvailableAction.findByModelAndFromStateAndActionCode(sm,s,action) ?: 
                      new AvailableAction(
                                          model:sm, 
                                          fromState:s, 
                                          actionCode:action).save(flush:true, failOnError:true);
      }
    }
    return result;

  }
}


