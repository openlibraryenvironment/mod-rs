package org.olf.rs.statemodel

import grails.gorm.MultiTenant

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner.
 */

class StateModel implements MultiTenant<StateModel> {

  public static final String MODEL_REQUESTER = "PatronRequest";
  public static final String MODEL_RESPONDER = "Responder";

  String id
  String shortcode
  String name

  static constraints = {
               shortcode (nullable: false, blank:false)
               name (nullable: true, blank:false)
  }

  static mapping = {
                     id column : 'sm_id', generator: 'uuid2', length:36
                version column : 'sm_version'
              shortcode column : 'sm_shortcode'
                   name column : 'sm_name'
  }

  static public StateModel getStateModel(boolean isRequester) {
	  return(StateModel.findByShortcode(isRequester ? MODEL_REQUESTER : MODEL_RESPONDER));
  }

  static public StateModel lookup(String code) {
      return(StateModel.findByShortcode(code));
  }
}
