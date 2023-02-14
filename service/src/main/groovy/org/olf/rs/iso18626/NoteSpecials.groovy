package org.olf.rs.iso18626;

/**
 * Holds the definitions of all the specials that we have incorporated into the note field
 * Once extensions are added this can be removed
 * @author Chas
 *
 */
public class NoteSpecials {

    // The different separators we use
    public static final String SPECIAL_FIELD_SEPARATOR = '-';
    public static final String SPECIAL_SEPARATOR       = ':';
    public static final String SPECIAL_WRAPPER         = '#';

    public static final String ADD_LOAN_CONDITION           = SPECIAL_WRAPPER + 'ReShareAddLoanCondition' + SPECIAL_WRAPPER;
    public static final String AGREE_LOAN_CONDITION         = SPECIAL_WRAPPER + 'ReShareLoanConditionAgreeResponse' + SPECIAL_WRAPPER;
    public static final String AWAITING_CONDITION_CONFIRMED = SPECIAL_WRAPPER + 'ReShareSupplierAwaitingConditionConfirmation' + SPECIAL_WRAPPER;
    public static final String CONDITIONS_ASSUMED_AGREED    = SPECIAL_WRAPPER + 'ReShareSupplierConditionsAssumedAgreed' + SPECIAL_WRAPPER;
    public static final String LAST_SEQUENCE                = SPECIAL_WRAPPER + 'lastSeq';
    public static final String SEQUENCE                     = SPECIAL_WRAPPER + 'seq';
    public static final String UPDATE_FIELD                 = SPECIAL_WRAPPER +'ReShareUpdatedField';

    public static final String FIELD_PICKUP_LOCATION = 'PickupLocation';

    public static final String LAST_SEQUENCE_PREFIX = LAST_SEQUENCE + SPECIAL_SEPARATOR;
    public static final String SEQUENCE_PREFIX      = SEQUENCE + SPECIAL_SEPARATOR;

    public static final String UPDATED_FIELD_PICKUP_LOCATION_PREFIX = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_PICKUP_LOCATION + SPECIAL_SEPARATOR;
}
