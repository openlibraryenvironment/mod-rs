package org.olf.rs.statemodel

/**
 * Defines the various qualifiers that influence the status
 */
public class ActionEventResultQualifier {
    static public final String QUALIFIER_CLOSE_CANCELLED      = 'REQ_CANCELLED';
    static public final String QUALIFIER_CLOSE_COMPLETE       = 'REQ_REQUEST_COMPLETE';
    static public final String QUALIFIER_CLOSE_END_OF_ROTA    = 'REQ_END_OF_ROTA';
    static public final String QUALIFIER_CLOSE_FILLED_LOCALLY = 'REQ_FILLED_LOCALLY';
    static public final String QUALIFIER_CLOSE_NOT_SUPPLIED   = 'RES_NOT_SUPPLIED';
    static public final String QUALIFIER_CLOSE_RESP_CANCELLED = 'RES_CANCELLED';
    static public final String QUALIFIER_CLOSE_RESP_COMPLETE  = 'RES_COMPLETE';
    static public final String QUALIFIER_CLOSE_UNFILLED       = 'RES_UNFILLED';
    static public final String QUALIFIER_CONDITIONS_AGREED    = 'conditionsAgreed';
    static public final String QUALIFIER_HOLDING              = 'holding';
    static public final String QUALIFIER_NO                   = 'no';

}
