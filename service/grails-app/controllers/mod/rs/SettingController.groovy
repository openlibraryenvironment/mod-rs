package mod.rs

import org.olf.rs.SettingsService
import org.olf.rs.logging.ContextLogging;

import com.k_int.web.toolkit.settings.AppSetting;

import grails.gorm.transactions.Transactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses
import org.olf.rs.referenceData.SettingsData;

@Api(value = "/rs/settings/appSettings", tags = ["Settings (application) Controller"], description = "API for all things to do with application settings")
class SettingController extends OkapiTenantAwareSwaggerController<AppSetting> {

    static responseFormats = ['json', 'xml'];

    private static final String RESOURCE_APP_SETTING = AppSetting.getSimpleName();

    SettingController() {
        super(AppSetting);
    }

    @Override
    @Transactional
    @ApiOperation(
        value = "Search with the supplied criteria",
        nickname = "/",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "term",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "The term to be searched for",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "match",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The properties the match is to be applied to",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "filters",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The filters to be applied",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "sort",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The properties to sort the items by",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "max",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Maximum number of items to return",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "perPage",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Number of items per page",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "offset",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Offset from the becoming of the result set to start returning results",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "page",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "The page you wnat the results being returned from",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "stats",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Do we return statistics about the search",
            dataType = "boolean"
        )
    ])
    def index(Integer max) {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, RESOURCE_APP_SETTING);
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_SEARCH);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        def result = []

        AppSetting.withNewSession { session ->
            AppSetting.withNewTransaction { status ->
                // Use GORM criteria builder to add your custom filter....
                Closure gormFilterClosure = {
                    or {
                        isNull('hidden')
                        eq('hidden', false)
                    }
                }

                // Check if explicitly filtering on the hidden field
                if (params.filters != null && params.filters.toString().indexOf("hidden") > -1) {
                    // They are explicitly filtering on it, so use null to avoid altering
                    gormFilterClosure = null
                }

                result = doTheLookup(gormFilterClosure)
                result = filterRecordsByFeatureFlags(result)

                return result
            }
        }

        respond result

        // Record how long it took
        ContextLogging.duration()
        log.debug(ContextLogging.MESSAGE_EXITING)
    }

    def filterRecordsByFeatureFlags(result) {
        if (result.isEmpty()) {
            return []
        }

        // Map to cache whether each section is enabled
        def sectionEnabledMap = [:]

        // Iterate over the records to determine section-enabled states
        result.each { record ->
            def section = record.section

            // Only check section flag if it hasn't been checked before
            if (!sectionEnabledMap.containsKey(section)) {
                // Construct the section-wide feature flag key
                String sectionFeatFlagKey = section + ".feature_flag"
                String sectionFeatFlagValue = settingsService.getSettingValue(sectionFeatFlagKey)

                // Determine if the section is enabled
                boolean isSectionEnabled = !(sectionFeatFlagValue != null && sectionFeatFlagValue == "false")

                // Store section enabled state in the map
                sectionEnabledMap[section] = isSectionEnabled
            }
        }

        // Filter records based on both section and key-specific feature flags
        return result.findAll { record ->
            def section = record.section
            def key = record.key

            // Get the cached section enabled state
            boolean isSectionEnabled = sectionEnabledMap[section]

            if (!isSectionEnabled) {
                return false
            }

            // Construct the feature flag key for the specific record
            String featFlagKey = section + "." + key + ".feature_flag"
            String featFlagValue = settingsService.getSettingValue(featFlagKey)

            // Include the record if the specific feature flag is not "false"
            return !(featFlagValue != null && featFlagValue == "false")
        }
    }
}