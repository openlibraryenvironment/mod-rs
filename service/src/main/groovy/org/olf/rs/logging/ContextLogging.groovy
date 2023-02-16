package org.olf.rs.logging;

import org.slf4j.MDC;

/**
 * Extends the MDC to allow us to add methods that manipulates the context of the data being logged
 * @author Chas
 *
 */
public class ContextLogging extends MDC {

    static public final String FIELD_ACTION              = "action";
    static public final String FIELD_DURATION            = "duration";
    static public final String FIELD_FIELDS_TO_MATCH     = "fieldsToMatch";
    static public final String FIELD_FILTERS             = "filters";
    static public final String FIELD_ID                  = "id";
    static public final String FIELD_JSON                = "json";
    static public final String FIELD_MAXIMUM_RESULTS     = "maximumResults";
    static public final String FIELD_NUMBER_PER_PAGE     = "numberPerPage";
    static public final String FIELD_OFFSET              =  "offset";
    static public final String FIELD_PAGE                = "page";
    static public final String FIELD_RESOURCE            = "resource";
    static public final String FIELD_SORT                = "sort";
    static private final String FIELD_START_TIME         = "startTime";
    static public final String FIELD_STATISTICS_REQUIRED = "statisticsRequired";
    static public final String FIELD_TERM                = "term";


    /**
     * Adds a field to the logging MDC if its value is not null
     * @param field The field name that the value will be mapped against
     * @param value The value to be output for this field
     */
    static public void setValue(String field, Object value) {
        // Only add it to the MDC if the value is not null
        if (value != null) {
            // Add it to the MDC
            put(field, value.toString());
        }
    }

    /**
     * Adds the start time to the context
     */
    static public void startTime() {
        setValue(FIELD_START_TIME, System.currentTimeMillis());
    }

    /**
     * Adds the duration to the context  if the start time was set
     */
    static public void duration() {
        String startTime = get(FIELD_START_TIME);
        if (startTime != null) {
            setValue(FIELD_DURATION, System.currentTimeMillis() - Long.valueOf(startTime));
        }
    }
}
