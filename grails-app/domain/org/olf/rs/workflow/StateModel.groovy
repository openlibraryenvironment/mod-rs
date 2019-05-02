package org.olf.rs.workflow

import grails.gorm.MultiTenant

class StateModel implements MultiTenant<StateModel> {

  /** We have the id as a uuid */
  String id;

  String name

  static mapping = {
    table      'wf_state_model'
    id         column : 'sm_id', generator: 'uuid2', length:36
    name       column : 'sm_name'
  }

  static constraints = {
    name     nullable : false
  }

}
