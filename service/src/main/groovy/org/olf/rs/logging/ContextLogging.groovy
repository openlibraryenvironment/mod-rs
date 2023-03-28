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
    static public final String FIELD_EVENT               = "event";
    static public final String FIELD_FIELDS_TO_MATCH     = "fieldsToMatch";
    static public final String FIELD_FILTERS             = "filters";
    static public final String FIELD_HRID                = "hrid";
    static public final String FIELD_ID                  = "id";
    static public final String FIELD_JSON                = "json";
    static public final String FIELD_JVM_UPTIME          = "jvmUptime";
    static public final String FIELD_MAXIMUM_RESULTS     = "maximumResults";
    static public final String FIELD_MEMORY_ALLOCATED    = "memoryAllocated";
    static public final String FIELD_MEMORY_FREE         = "memoryFree";
    static public final String FIELD_MEMORY_MAX          = "memoryMax";
    static public final String FIELD_MEMORY_TOTAL_FREE   = "memoryTotalFree";
    static public final String FIELD_NUMBER_PER_PAGE     = "numberPerPage";
    static public final String FIELD_OFFSET              =  "offset";
    static public final String FIELD_PAGE                = "page";
    static public final String FIELD_REQUEST_ACTION      = "requestAction";
    static public final String FIELD_RESOURCE            = "resource";
    static public final String FIELD_SLUG                = "slug";
    static public final String FIELD_SORT                = "sort";
    static public final String FIELD_START_TIME          = "startTime";
    static public final String FIELD_STATISTICS_REQUIRED = "statisticsRequired";
    static public final String FIELD_TENANT              = "tenant";
    static public final String FIELD_TERM                = "term";
    static public final String FIELD_TIMER               = "timer";
    static public final String FIELD_XML                 = "xml";

    static public final String ACTION_BULK_ACTION              = "bulkAction";
    static public final String ACTION_CAN_CREATE_REQUEST       = "canCreateRequest";
    static public final String ACTION_CREATE                   = "create";
    static public final String ACTION_CREATE_GRAPH             = "createGraph";
    static public final String ACTION_CREATE_UPDATE            = "CreateUpdate";
    static public final String ACTION_DELETE                   = "delete";
    static public final String ACTION_EXECUTE                  = "execute";
    static public final String ACTION_EXPORT                   = "export";
    static public final String ACTION_FETCH                    = "fetch";
    static public final String ACTION_FILE_DOWNLOAD            = "fileDownload";
    static public final String ACTION_FILE_UPLOAD              = "fileUpload";
    static public final String ACTION_FROM_STATES              = "fromStates";
    static public final String ACTION_GENERATE_PICK_LIST       = "generatePickList";
    static public final String ACTION_GENERATE_PICK_LIST_BATCH = "generatePickListBatch";
    static public final String ACTION_HANDLE_APPLICATION_EVENT = "handleApplicationEvent";
    static public final String ACTION_HANDLE_REQUEST_MESSAGE   = "handleRequestMessage";
    static public final String ACTION_IMPORT                   = "import";
    static public final String ACTION_INDEX                    = "index";
    static public final String ACTION_ISO18626                 = "iso18626";
    static public final String ACTION_MANUAL_CLOSE_STATES      = "manualCloseStates";
    static public final String ACTION_MARK_BATCH_AS_PRINTED    = "markBatchAsPrinted";
    static public final String ACTION_OPEN_URL                 = "openUrl";
    static public final String ACTION_PERFORM_ACTION           = "performAction";
    static public final String ACTION_PROCESS_DIRECTORY_UPDATE = "processDirectoryUpdate";
    static public final String ACTION_RAML                     = "raml";
    static public final String ACTION_SEARCH                   = "search";
    static public final String ACTION_STATISTICS               = "statistics";
    static public final String ACTION_STATUS_REPORT            = "statusReport";
    static public final String ACTION_TEST_DETERMINE_BEST_LOC  = "testDetermineBestLocation";
    static public final String ACTION_TEST_NCIP_ACCEPT_ITEM    = "testNCIPAcceptItem";
    static public final String ACTION_TEST_NCIP_CHECK_IN       = "testNCIPCheckIn";
    static public final String ACTION_TEST_NCIP_CHECK_OUT      = "testNCIPCheckOut";
    static public final String ACTION_TEST_NCIP_VALIDATE       = "testNCIPValidate";
    static public final String ACTION_TO_STATES                = "toStates";
    static public final String ACTION_UPDATE                   = "update";
    static public final String ACTION_VALID_ACTIONS            = "validActions";
    static public final String ACTION_WORKER                   = "worker";

    static public final String MESSAGE_ENTERING = "Entering";
    static public final String MESSAGE_EXITING  = "Exiting";

    /**
     * Adds a field to the logging MDC if its value is not null
     * @param field The field name that the value will be mapped against
     * @param value The value to be output for this field
     */
    static public void setValue(String field, Object value) {
        // Only add it to the MDC if the value is not null
        if (value == null) {
            // Remove the existing value if there is one
            remove(field);
        } else {
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
