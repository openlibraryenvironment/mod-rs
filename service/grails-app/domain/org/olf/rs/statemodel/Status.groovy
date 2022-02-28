package org.olf.rs.statemodel

import grails.gorm.MultiTenant
import com.k_int.web.toolkit.tags.Tag

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

class Status implements MultiTenant<Status> {

  public static String PATRON_REQUEST_AWAITING_RETURN_SHIPPING    = "REQ_AWAITING_RETURN_SHIPPING";
  public static String PATRON_REQUEST_BORROWER_RETURNED           = "REQ_BORROWER_RETURNED";
  public static String PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED  = "REQ_BORROWING_LIBRARY_RECEIVED";
  public static String PATRON_REQUEST_CANCEL_PENDING              = "REQ_CANCEL_PENDING";
  public static String PATRON_REQUEST_CANCELLED                   = "REQ_CANCELLED";
  public static String PATRON_REQUEST_CANCELLED_WITH_SUPPLIER     = "REQ_CANCELLED_WITH_SUPPLIER";
  public static String PATRON_REQUEST_CHECKED_IN                  = "REQ_CHECKED_IN";
  public static String PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED = "REQ_CONDITIONAL_ANSWER_RECEIVED";
  public static String PATRON_REQUEST_END_OF_ROTA                 = "REQ_END_OF_ROTA";
  public static String PATRON_REQUEST_ERROR                       = "REQ_ERROR";
  public static String PATRON_REQUEST_EXPECTS_TO_SUPPLY           = "REQ_EXPECTS_TO_SUPPLY";
  public static String PATRON_REQUEST_FILLED_LOCALLY              = "REQ_FILLED_LOCALLY";
  public static String PATRON_REQUEST_IDLE                        = "REQ_IDLE";
  public static String PATRON_REQUEST_INVALID_PATRON              = "REQ_INVALID_PATRON";
  public static String PATRON_REQUEST_LOCAL_REVIEW                = "REQ_LOCAL_REVIEW";
  public static String PATRON_REQUEST_OVERDUE                     = "REQ_OVERDUE";
  public static String PATRON_REQUEST_PENDING                     = "REQ_PENDING";
  public static String PATRON_REQUEST_RECALLED                    = "REQ_RECALLED";
  public static String PATRON_REQUEST_REQUEST_COMPLETE            = "REQ_REQUEST_COMPLETE";
  public static String PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    = "REQ_REQUEST_SENT_TO_SUPPLIER";
  public static String PATRON_REQUEST_SHIPPED                     = "REQ_SHIPPED";
  public static String PATRON_REQUEST_SHIPPED_TO_SUPPLIER         = "REQ_SHIPPED_TO_SUPPLIER";
  public static String PATRON_REQUEST_SOURCING_ITEM               = "REQ_SOURCING_ITEM";
  public static String PATRON_REQUEST_SUPPLIER_IDENTIFIED         = "REQ_SUPPLIER_IDENTIFIED";
  public static String PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER  = "REQ_UNABLE_TO_CONTACT_SUPPLIER";
  public static String PATRON_REQUEST_UNFILLED                    = "REQ_UNFILLED";
  public static String PATRON_REQUEST_VALIDATED                   = "REQ_VALIDATED";
  public static String PATRON_REQUEST_WILL_SUPPLY                 = "REQ_WILL_SUPPLY";

  public static String RESPONDER_AWAIT_PICKING              = "RES_AWAIT_PICKING";
  public static String RESPONDER_AWAIT_SHIP                 = "RES_AWAIT_SHIP";
  public static String RESPONDER_CANCEL_REQUEST_RECEIVED    = "RES_CANCEL_REQUEST_RECEIVED";
  public static String RESPONDER_CANCELLED                  = "RES_CANCELLED";
  public static String RESPONDER_CHECKED_IN_TO_RESHARE      = "RES_CHECKED_IN_TO_RESHARE";
  public static String RESPONDER_COMPLETE                   = "RES_COMPLETE";
  public static String RESPONDER_ERROR                      = "RES_ERROR";
  public static String RESPONDER_HOLD_PLACED                = "RES_HOLD_PLACED";
  public static String RESPONDER_IDLE                       = "RES_IDLE";
  public static String RESPONDER_ITEM_RETURNED              = "RES_ITEM_RETURNED";
  public static String RESPONDER_ITEM_SHIPPED               = "RES_ITEM_SHIPPED";
  public static String RESPONDER_NEW_AWAIT_PULL_SLIP        = "RES_NEW_AWAIT_PULL_SLIP";
  public static String RESPONDER_NOT_SUPPLIED               = "RES_NOT_SUPPLIED";
  public static String RESPONDER_OVERDUE                    = "RES_OVERDUE";
  public static String RESPONDER_PENDING_CONDITIONAL_ANSWER = "RES_PENDING_CONDITIONAL_ANSWER";
  public static String RESPONDER_UNFILLED                   = "RES_UNFILLED";

  // These 2 are no longer used but have left here in case they are to be used in the future,
  // as it will hopefully highlight that they may still be hangin arpind on an old system as we have not removed the references to them in the database
  //public static String RESPONDER_AWAIT_PROXY_BORROWER       = "RES_AWAIT_PROXY_BORROWER";
  //public static String RESPONDER_AWAITING_RETURN_SHIPPING   = "RES_AWAITING_RETURN_SHIPPING";
  
  String id
  StateModel owner
  String code
  String presSeq
  // Used when retrieving termination status for closing the request manually, if it is not true, it will not be returned
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
	boolean modified = false;
    StateModel sm = StateModel.findByShortcode(model) ?: new StateModel(shortcode: model).save(flush:true, failOnError:true)
    Status s = Status.findByOwnerAndCode(sm, code)
    if ( s == null ) {
      s = new Status(owner:sm, code:code, presSeq:presSeq, visible:visible, needsAttention:needsAttention, terminal:terminal, tags: tags);
	  modified = true;
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
			modified = true;
          }
        }
      }
	  
	  // Check if the visibility has changed
	  if (visible == null) {
		  if (s.visible != null) {
			  s.visible = visible;
			  modified = true;
		  }
	  } else if ((s.visible == null) || (s.visible != visible)) {
		  s.visible = visible;
		  modified = true;
	  }
    }

	// If we have modified the record then save it	
	if (modified == true) {
	  s.save(flush:true, failOnError:true);
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

  public static List<String> getTerminalStates(String stateModelCode) {
    List<String> result = new ArrayList<String>();
    StateModel stateModel = StateModel.findByShortcode(stateModelCode);
    if ( stateModel ) {
      Status[] states = Status.findAllByOwnerAndTerminal(stateModel, true);
	  if (states) {
		  states.each {  state ->
			  result.add(state.code);
		  }
	  }
    }
    return(result);
  }
}


