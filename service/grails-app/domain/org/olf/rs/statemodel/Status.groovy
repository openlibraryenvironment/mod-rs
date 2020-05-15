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
  String presSeq
  Boolean visible
  Boolean needsAttention

  static constraints = {
               owner (nullable: false)
               code (nullable: false, blank:false)
            presSeq (nullable: true, blank:false)
            visible (nullable: true)
     needsAttention (nullable: false)
  }

  static mapping = {
                     id column : 'st_id', generator: 'uuid2', length:36
                version column : 'st_version'
                  owner column : 'st_owner'
                   code column : 'st_code'
                presSeq column : 'st_presentation_sequence'
                visible column : 'st_visible'
         needsAttention column : 'st_needs_attention'
  }

  public static Status lookupOrCreate(String model, String code, presSeq=null, visible=null, Boolean needsAttention=false) {
    StateModel sm = StateModel.findByShortcode(model) ?: new StateModel(shortcode: model).save(flush:true, failOnError:true)
    Status s = Status.findByOwnerAndCode(sm, code) ?: new Status(owner:sm, code:code, presSeq:presSeq, visible:visible, needsAttention:needsAttention).save(flush:true, failOnError:true)
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


