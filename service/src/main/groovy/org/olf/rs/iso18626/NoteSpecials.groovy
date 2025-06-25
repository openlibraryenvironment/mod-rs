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

    public static final String FIELD_AUTHOR                     = 'Author';
    public static final String FIELD_AUTHOR_OF_COMPONENT        = 'AuthorOfComponent';
    public static final String FIELD_EDITION                    = 'Edition';
    public static final String FIELD_ISBN                       = 'ISBN';
    public static final String FIELD_ISSN                       = 'ISSN';
    public static final String FIELD_ISSUE                      = 'Issue';
    public static final String FIELD_NEEDED_BY                  = 'Neededby';
    public static final String FIELD_OCLC_NUMBER                = 'OCLCNumber';
    public static final String FIELD_PAGES_REQUESTED            = 'PagesRequested';
    public static final String FIELD_PATRON_NOTE                = 'PatronNote';
    public static final String FIELD_PICKUP_LOCATION            = 'PickupLocation';
    public static final String FIELD_PLACE_OF_PUBLICATION       = 'PlaceOfPublication';
    public static final String FIELD_PUBLICATION_DATE           = 'PublicationDate';
    public static final String FIELD_PUBLISHER                  = 'Publisher';
    public static final String FIELD_SERVICE_LEVEL              = 'ServiceLevel';
    public static final String FIELD_SUBTITLE                   = 'Subtitle';
    public static final String FIELD_SYSTEM_INSTANCE_IDENTIFIER = 'SystemInstanceIdentifier';
    public static final String FIELD_TITLE                      = 'Title';
    public static final String FIELD_TITLE_OF_COMPONENT         = 'TitleOfComponent';
    public static final String FIELD_VOLUME                     = 'Volume';
    public static final String FIELD_COPYRIGHT_TYPE             = 'CopyrightType';
    public static final String FIELD_MAX_COSTS_VALUE            = 'MaximumCostsMonetaryValue';
    public static final String FIELD_MAX_COSTS_CODE             = 'MaximumCostsCurrencyCode';

    public static final String LAST_SEQUENCE_PREFIX = LAST_SEQUENCE + SPECIAL_SEPARATOR;
    public static final String SEQUENCE_PREFIX      = SEQUENCE + SPECIAL_SEPARATOR;

    public static final String UPDATED_FIELD_AUTHOR_PREFIX                     = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_AUTHOR + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_AUTHOR_OF_COMPONENT_PREFIX        = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_AUTHOR_OF_COMPONENT + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_EDITION_PREFIX                    = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_EDITION + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_ISBN_PREFIX                       = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_ISBN + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_ISSN_PREFIX                       = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_ISSN + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_ISSUE_PREFIX                      = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_ISSUE + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_NEEDED_BY_PREFIX                  = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_NEEDED_BY + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_OCLC_NUMBER_PREFIX                = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_OCLC_NUMBER + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_PAGES_REQUESTED_PREFIX            = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_PAGES_REQUESTED + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_PATRON_NOTE_PREFIX                = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_PATRON_NOTE + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_PICKUP_LOCATION_PREFIX            = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_PICKUP_LOCATION + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_PLACE_OF_PUBLICATION_PREFIX       = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_PLACE_OF_PUBLICATION + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_PUBLICATION_DATE_PREFIX           = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_PUBLICATION_DATE + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_PUBLISHER_PREFIX                  = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_PUBLISHER + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_SUBTITLE_PREFIX                   = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_SUBTITLE + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_SYSTEM_INSTANCE_IDENTIFIER_PREFIX = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_SYSTEM_INSTANCE_IDENTIFIER + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_SERVICE_LEVEL_PREFIX              = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_SERVICE_LEVEL + SPECIAL_FIELD_SEPARATOR;
    public static final String UPDATED_FIELD_TITLE_PREFIX                      = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_TITLE + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_TITLE_OF_COMPONENT_PREFIX         = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_TITLE_OF_COMPONENT + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_VOLUME_PREFIX                     = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_VOLUME + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_COPYRIGHT_TYPE                    = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_COPYRIGHT_TYPE + SPECIAL_SEPARATOR;
    public static final String UPDATED_FIELD_MAX_COSTS_VALUE                   = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_MAX_COSTS_VALUE + SPECIAL_FIELD_SEPARATOR;
    public static final String UPDATED_FIELD_MAX_COSTS_CODE                    = UPDATE_FIELD + SPECIAL_FIELD_SEPARATOR + FIELD_MAX_COSTS_CODE + SPECIAL_FIELD_SEPARATOR;
}
