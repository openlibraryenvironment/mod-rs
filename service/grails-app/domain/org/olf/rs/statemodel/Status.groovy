package org.olf.rs.statemodel

import grails.gorm.MultiTenant
import com.k_int.web.toolkit.tags.Tag

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

  // Set up boolean to indicate whether a state is terminal or not.
  Boolean terminal

  static hasMany = [
        // Allow us to tag a state for use in analytical reporting - for example CURRENT_LOAN or CURRENT_BORROW
        tags: Tag
  ]


  static constraints = {
               owner (nullable: false)
               code (nullable: false, blank:false)
            presSeq (nullable: true, blank:false)
            visible (nullable: true)
     needsAttention (nullable: true)
           terminal (nullable: true)
  }

  static mapping = {
                     id column : 'st_id', generator: 'uuid2', length:36
                version column : 'st_version'
                  owner column : 'st_owner'
                   code column : 'st_code'
                presSeq column : 'st_presentation_sequence'
                visible column : 'st_visible'
         needsAttention column : 'st_needs_attention'
               terminal column : 'st_terminal'
                   tags cascade: 'save-update'
  }

  // Assert is a helper method that helps us ensure refdata is up to date - create any missing entries, update any ones
  // where the tags have changed and return the located value
  public static Status ensure(String model, String code, presSeq=null, visible=null, needsAttention=null, terminal=false, tags=null) {
    StateModel sm = StateModel.findByShortcode(model) ?: new StateModel(shortcode: model).save(flush:true, failOnError:true)
    Status s = Status.findByOwnerAndCode(sm, code)
    if ( s == null ) {
      s = new Status(owner:sm, code:code, presSeq:presSeq, visible:visible, needsAttention:needsAttention, terminal:terminal, tags: tags).save(flush:true, failOnError:true)
    }
    else {
      // We already know about this status code - just check if we need to install any new tags
      if ( tags != null ) {
        if ( s.tags == null )
          s.tags = []
        tags.each { tag ->
          if ( s.tags.find { it.value == tag } == null ) {
            def tag_obj = Tag.findByNormValue(Tag.normalizeValue(tag)) ?: new Tag(value:tag);
            s.tags.add(tag_obj);
            s.save(flush:true, failOnError:true)
          }
        }
      }
    }
    return s;
  }

  public static Status lookupOrCreate(String model, String code, presSeq=null, visible=null, needsAttention=null, terminal=false, tags=null) {
    StateModel sm = StateModel.findByShortcode(model) ?: new StateModel(shortcode: model).save(flush:true, failOnError:true)
    Status s = Status.findByOwnerAndCode(sm, code) 
    if ( s == null ) {
      s = new Status(owner:sm, code:code, presSeq:presSeq, visible:visible, needsAttention:needsAttention, terminal:terminal, tags: tags).save(flush:true, failOnError:true)
    }
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


