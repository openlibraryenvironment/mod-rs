package org.olf.rs.statemodel

import com.k_int.web.toolkit.tags.Tag;

import grails.gorm.MultiTenant;

class Status implements MultiTenant<Status> {

  public static final String PATRON_REQUEST_AWAITING_RETURN_SHIPPING    = "REQ_AWAITING_RETURN_SHIPPING";
  public static final String PATRON_REQUEST_BLANK_FORM_REVIEW           = "REQ_BLANK_FORM_REVIEW";
  public static final String PATRON_REQUEST_BORROWER_RETURNED           = "REQ_BORROWER_RETURNED";
  public static final String PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED  = "REQ_BORROWING_LIBRARY_RECEIVED";
  public static final String PATRON_REQUEST_CANCEL_PENDING              = "REQ_CANCEL_PENDING";
  public static final String PATRON_REQUEST_CANCELLED                   = "REQ_CANCELLED";
  public static final String PATRON_REQUEST_CANCELLED_WITH_SUPPLIER     = "REQ_CANCELLED_WITH_SUPPLIER";
  public static final String PATRON_REQUEST_CHECKED_IN                  = "REQ_CHECKED_IN";
  public static final String PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED = "REQ_CONDITIONAL_ANSWER_RECEIVED";
  public static final String PATRON_REQUEST_DUPLICATE_REVIEW            = "REQ_DUPLICATE_REVIEW";
  public static final String PATRON_REQUEST_END_OF_ROTA                 = "REQ_END_OF_ROTA";
  public static final String PATRON_REQUEST_END_OF_ROTA_REVIEWED        = "REQ_END_OF_ROTA_REVIEWED";
  public static final String PATRON_REQUEST_ERROR                       = "REQ_ERROR";
  public static final String PATRON_REQUEST_EXPECTS_TO_SUPPLY           = "REQ_EXPECTS_TO_SUPPLY";
  public static final String PATRON_REQUEST_FILLED_LOCALLY              = "REQ_FILLED_LOCALLY";
  public static final String PATRON_REQUEST_IDLE                        = "REQ_IDLE";
  public static final String PATRON_REQUEST_INVALID_PATRON              = "REQ_INVALID_PATRON";
  public static final String PATRON_REQUEST_LOCAL_REVIEW                = "REQ_LOCAL_REVIEW";
  public static final String PATRON_REQUEST_OVERDUE                     = "REQ_OVERDUE";
  public static final String PATRON_REQUEST_OVER_LIMIT                  = "REQ_OVER_LIMIT";
  public static final String PATRON_REQUEST_PENDING                     = "REQ_PENDING";
  public static final String PATRON_REQUEST_RECALLED                    = "REQ_RECALLED";
  public static final String PATRON_REQUEST_REQUEST_COMPLETE            = "REQ_REQUEST_COMPLETE";
  public static final String PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    = "REQ_REQUEST_SENT_TO_SUPPLIER";
  public static final String PATRON_REQUEST_SHIPPED                     = "REQ_SHIPPED";
  public static final String PATRON_REQUEST_SHIPPED_TO_SUPPLIER         = "REQ_SHIPPED_TO_SUPPLIER";
  public static final String PATRON_REQUEST_SOURCING_ITEM               = "REQ_SOURCING_ITEM";
  public static final String PATRON_REQUEST_SUPPLIER_IDENTIFIED         = "REQ_SUPPLIER_IDENTIFIED";
  public static final String PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER  = "REQ_UNABLE_TO_CONTACT_SUPPLIER";
  public static final String PATRON_REQUEST_UNFILLED                    = "REQ_UNFILLED";
  public static final String PATRON_REQUEST_VALIDATED                   = "REQ_VALIDATED";
  public static final String PATRON_REQUEST_WILL_SUPPLY                 = "REQ_WILL_SUPPLY";

  public static final String REQUESTER_LOANED_DIGITALLY                 = "REQ_LOANED_DIGITALLY";

  public static final String RESPONDER_AWAIT_PICKING              = "RES_AWAIT_PICKING";
  public static final String RESPONDER_AWAIT_SHIP                 = "RES_AWAIT_SHIP";
  public static final String RESPONDER_AWAITING_RETURN_SHIPPING   = "RES_AWAITING_RETURN_SHIPPING";
  public static final String RESPONDER_CANCEL_REQUEST_RECEIVED    = "RES_CANCEL_REQUEST_RECEIVED";
  public static final String RESPONDER_CANCELLED                  = "RES_CANCELLED";
  public static final String RESPONDER_COMPLETE                   = "RES_COMPLETE";
  public static final String RESPONDER_IDLE                       = "RES_IDLE";
  public static final String RESPONDER_ITEM_RETURNED              = "RES_ITEM_RETURNED";
  public static final String RESPONDER_ITEM_SHIPPED               = "RES_ITEM_SHIPPED";
  public static final String RESPONDER_NEW_AWAIT_PULL_SLIP        = "RES_NEW_AWAIT_PULL_SLIP";
  public static final String RESPONDER_NOT_SUPPLIED               = "RES_NOT_SUPPLIED";
  public static final String RESPONDER_OVERDUE                    = "RES_OVERDUE";
  public static final String RESPONDER_PENDING_CONDITIONAL_ANSWER = "RES_PENDING_CONDITIONAL_ANSWER";
  public static final String RESPONDER_UNFILLED                   = "RES_UNFILLED";

  public static final String RESPONDER_SEQUESTERED              = "RES_SEQUESTERED";
  public static final String RESPONDER_LOANED_DIGITALLY         = "RES_LOANED_DIGITALLY";
  public static final String RESPONDER_AWAIT_DESEQUESTRATION    = "RES_AWAIT_DESEQUESTRATION";

  // These 4 are no longer used but have left here in case they are to be used in the future,
  // as it will hopefully highlight that they may still be hangin arpind on an old system as we have not removed the references to them in the database
  public static final String RESPONDER_AWAIT_PROXY_BORROWER       = "RES_AWAIT_PROXY_BORROWER";
  public static final String RESPONDER_CHECKED_IN_TO_RESHARE      = "RES_CHECKED_IN_TO_RESHARE";
  public static final String RESPONDER_ERROR                      = "RES_ERROR";
  public static final String RESPONDER_HOLD_PLACED                = "RES_HOLD_PLACED";

  String id
  String code
  String presSeq
  // Used when retrieving termination status for closing the request manually, if it is not true, it will not be returned
  Boolean visible
  Boolean needsAttention

  // Set up boolean to indicate whether a state is terminal or not.
  Boolean terminal

  // The stage of the request that this status applies to
  StatusStage stage;

  /** The sequence to display the status for the close manual drop down */
  Integer terminalSequence;

  static hasMany = [
        // Allow us to tag a state for use in analytical reporting - for example CURRENT_LOAN or CURRENT_BORROW
        tags: Tag
  ]

  static constraints = {
                 code (nullable: false, blank: false, unique: true)
              presSeq (nullable: true, blank: false)
              visible (nullable: true)
       needsAttention (nullable: true)
             terminal (nullable: false)
                stage (nullable: true)
     terminalSequence (nullable: true)
  }

    static mapping = {
                       id column : 'st_id', generator: 'uuid2', length:36
                  version column : 'st_version'
                     code column : 'st_code'
                  presSeq column : 'st_presentation_sequence'
                  visible column : 'st_visible'
           needsAttention column : 'st_needs_attention'
                 terminal column : 'st_terminal'
                    stage column : 'st_stage'
         terminalSequence column : 'st_terminal_sequence'
                     tags cascade: 'save-update'
    }

    // A helper method that helps us ensure Status instances persisted in code are up to date
    // and returns the updated object
    public static Status ensure(
        String code,
        StatusStage stage,
        String presSeq = null,
        Boolean visible = null,
        Boolean needsAttention = null,
        Boolean terminal = false,
        Integer terminalSequence = null,
        List<String> tags=null) {
      Status s = Status.findByCode(code)
      if ( s == null ) {
          s = new Status(code:code, tags: tags);
      }

      // Update the the fields in case they have changed
      s.presSeq = presSeq;
      s.visible = visible;
      s.needsAttention = needsAttention;
      s.terminal = terminal;
      s.stage = stage;
      s.terminalSequence = terminalSequence;

      // Reflect new tag values, removing tags if none provided
      s.tags?.clear();
      if ( tags != null ) {
          tags.each { tag ->
              def tag_obj = Tag.findByNormValue(Tag.normalizeValue(tag)) ?: new Tag(value:tag);
              s.addToTags(tag_obj);
          }
      }

      // Save the status
	  s.save(flush:true, failOnError:true);

      // Return the status back to the caller
      return s;
  }

  public static Status lookup(String code) {
    Status result = null;
    if (code != null) {
        result = Status.findByCode(code);
    }
    return result;
  }

// Dosn't appear to be used
//  public static List<String> getTerminalStates(String stateModelCode) {
//    List<String> result = new ArrayList<String>();
//    StateModel stateModel = StateModel.findByShortcode(stateModelCode);
//    if ( stateModel ) {
//      Status[] states = Status.findAllByOwnerAndTerminal(stateModel, true);
//	  if (states) {
//		  states.each {  state ->
//			  result.add(state.code);
//		  }
//	  }
//    }
//    return(result);
//  }

  static public Status lookupStatusEvent(ActionEvent event) {
      Status eventStatus = null;

      if (event.code.startsWith(Events.STATUS_EVENT_PREFIX)) {
          // That is good it starts with the right prefix, so remove it
          String code = event.code.substring(Events.STATUS_EVENT_PREFIX.length());
          if (code.endsWith(Events.STATUS_EVENT_POSTFIX)) {
              // Event better it has the right postfix, so we need to move it
              code = code.substring(0, code.length() - Events.STATUS_EVENT_POSTFIX.length());

              // Now we can lookup the code and ensure it is for the right model
              eventStatus = findByCode(code);
          }
      }
      return(eventStatus);
  }
}
