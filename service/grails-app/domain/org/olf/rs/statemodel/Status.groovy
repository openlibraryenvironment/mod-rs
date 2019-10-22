package org.olf.rs.statemodel

import grails.gorm.MultiTenant

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

class Status implements MultiTenant<Status> {

  String id
  StateModel owner
  String code

  static constraints = {
               owner (nullable: false)
               code (nullable: false, blank:false)
  }

  static mapping = {
                     id column : 'st_id', generator: 'uuid2', length:36
                version column : 'st_version'
                  owner column : 'st_owner'
                   code column : 'st_code'
  }

  public static Status lookupOrCreate(String model, String code) {
    StateModel sm = StateModel.findByShortcode(model) ?: new StateModel(shortcode: model).save(flush:true, failOnError:true)
    Status s = Status.findByOwnerAndCode(sm, code) ?: new Status(owner:sm, code:code).save(flush:true, failOnError:true)
    return s;
  }

  public static Status lookup(String model, String code) {
    Status result = null;
    StateModel sm = StateModel.findByShortcode(model);
    if ( sm ) {
      result = Status.findByOwnerAndCode(sm, code);
    }
    return result;
  }
}


