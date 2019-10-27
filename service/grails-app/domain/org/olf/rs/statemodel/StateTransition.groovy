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
  Status toState

  static constraints = {
             model (nullable: false, blank:false)
         fromState (nullable: false, blank:false)
        actionCode (nullable: false, blank:false)
           toState (nullable: false, blank:false)
  }

  static mapping = {
                     id column : 'st_id', generator: 'uuid2', length:36
                version column : 'st_version'
                  model column : 'st_model'
              fromState column : 'st_fromState'
             actionCode column : 'st_actionCode'
                toState column : 'st_toState'
  }

}


