package org.olf.rs.statemodel

/**
 * Defines the various qualifiers that influence the status
 */
public class ActionEventResultQualifier {
    static public final String QUALIFIER_BLANK_FORM_REVIEW     = 'blankFormReview';
    static public final String QUALIFIER_CANCELLED             = 'Cancelled';
    static public final String QUALIFIER_CHECKED_IN            = 'checkedIn';
    static public final String QUALIFIER_CLOSE_CANCELLED       = 'REQ_CANCELLED';
    static public final String QUALIFIER_CLOSE_COMPLETE        = 'REQ_REQUEST_COMPLETE';
    static public final String QUALIFIER_CLOSE_END_OF_ROTA     = 'REQ_END_OF_ROTA';
    static public final String QUALIFIER_CLOSE_FILLED_LOCALLY  = 'REQ_FILLED_LOCALLY';
    static public final String QUALIFIER_CLOSE_NOT_SUPPLIED    = 'RES_NOT_SUPPLIED';
    static public final String QUALIFIER_CLOSE_RESP_CANCELLED  = 'RES_CANCELLED';
    static public final String QUALIFIER_CLOSE_RESP_COMPLETE   = 'RES_COMPLETE';
    static public final String QUALIFIER_CLOSE_UNFILLED        = 'RES_UNFILLED';
    static public final String QUALIFIER_CONDITIONAL           = 'Conditional';
    static public final String QUALIFIER_CONDITIONS_AGREED     = 'conditionsAgreed';
    static public final String QUALIFIER_CONTINUE              = 'continue';
    static public final String QUALIFIER_DUPLICATE_REVIEW      = 'duplicateReview';
    static public final String QUALIFIER_END_OF_ROTA           = 'endOfRota';
    static public final String QUALIFIER_EXPECT_TO_SUPPLY      = 'ExpectToSupply';
    static public final String QUALIFIER_HOLDING               = 'holding';
    static public final String QUALIFIER_HOST_LMS_CALL_FAILED  = 'hostLMSCallFailed';
    static public final String QUALIFIER_INVALID_PATRON        = 'invalidPatron';
    static public final String QUALIFIER_LOCAL_REVIEW          = 'localReview';
    static public final String QUALIFIER_LOCATED               = 'located';
    static public final String QUALIFIER_LOCATED_REQUEST_ITEM  = 'locatedRequestItem';
    static public final String QUALIFIER_NO                    = 'no';
    static public final String QUALIFIER_NO_INSTITUTION_SYMBOL = 'noInstitutionSymbol';
    static public final String QUALIFIER_NO_SUPPLIER           = 'noSupplier';
    static public final String QUALIFIER_OVER_LIMIT            = 'overLimit';
    static public final String QUALIFIER_SHIP_ITEM             = 'shipItem';
    static public final String QUALIFIER_SHIPPED               = 'shipped';
    static public final String QUALIFIER_SOURCING              = 'sourcing';
    static public final String QUALIFIER_UNFILLED              = 'unfilled';
    static public final String QUALIFIER_LOANED                = 'Loaned';
    static public final String QUALIFIER_ABORTED               = 'ABORT';
    static public final String QUALIFIER_DOCUMENT_SUPPLIED     = 'DocumentSupplied';
    static public final String QUALIFIER_DOCUMENT_AVAILABLE    = 'DocumentAvailable';
    static public final String QUALIFIER_SENT_TO_SUPPLIER      = 'SentToSupplier';
    static public final String QUALIFIER_WILL_SUPPLY           = 'WillSupply';
    static public final String QUALIFIER_COPY_COMPLETED        = 'CopyCompleted';
}
