package mod.rs;

import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.logging.ContextLogging;
import org.olf.rs.logging.HoldingLogDetails;
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.logging.ProtocolAuditService;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.MapSettings;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Slf4j
@Api(value = "/rs/testHostLMS", tags = ["Test Host LMS Controller"], description = "Test Host LMS Api")
class TestHostLMSController extends OkapiTenantAwareController<PatronRequest> {

    private static final String CIRC_TYPE_NCIP = "ncip";

    private static final String HOST_LMS_ADAPTERS = "aleph,alma,default,folio,horizon,koha,manual,millennium,ncsu,sierra,symphony,tlc,voyager,wms,wms2";

    HostLMSService hostLMSService;
    ProtocolAuditService protocolAuditService;

	TestHostLMSController() {
        super(PatronRequest);
	}

    /**
     * Validates the borrower id against the circulation system
     * @return The result from the validate call
     */
    @ApiOperation(
        value = "Validates the user against the specified Host LMS",
        nickname = "validate",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "lms",
            paramType = "query",
            allowMultiple = false,
            allowableValues = HOST_LMS_ADAPTERS,
            required = true,
            value = "The type of the host LMS",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipServerAddress",
            paramType = "query",
            required = true,
            value = "The address for the ncip server",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgency",
            paramType = "query",
            required = true,
            value = "The ncip from agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipToAgency",
            paramType = "query",
            required = false,
            value = "The ncip to agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipAppProfile",
            paramType = "query",
            required = true,
            value = "The ncip app profile",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgencyAuthentication",
            paramType = "query",
            required = false,
            value = "The from agency authentication",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiKey",
            paramType = "query",
            required = false,
            value = "The wms api key",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiSecret",
            paramType = "query",
            required = false,
            value = "The wms api secret",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsLookupPatronEndpoint",
            paramType = "query",
            required = false,
            value = "The wms lookup patron endpoint",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsRegistryId",
            paramType = "query",
            required = false,
            value = "The WMS registry id",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "patronId",
            paramType = "query",
            required = true,
            value = "The patron id to be validated",
            dataType = "string"
        )
    ])
	def validate() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_TEST_NCIP_VALIDATE);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        Map result = [ : ];

        // Must have been supplied the LMS
        if (params.lms) {
            HostLMSActions hostLMSActions = hostLMSService.getHostLMSActionsFor(params.lms);

            if (hostLMSActions == null) {
                result.error = "unable to determine service for lms: " + params.lms;
            } else  {
                // Must be supplied a patronId
                if (params.patronId) {
                    // Now we need to create our settings that will be used
                    MapSettings settings = createAuthSettings();

                    // Settings need to know we are using ncip for validate
                    settings.add(SettingsData.SETTING_BORROWER_CHECK, CIRC_TYPE_NCIP);

                    // Now we can make the call
                    result = hostLMSActions.lookupPatron(settings, params.patronId);
                } else {
                    result.error = "no patronId supplied";
                }
            }
        } else {
            result.error = "lms not supplied";
        }

		render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     * Checks out the item from the local LMS
     * @return The result from the check out call
     */
    @ApiOperation(
        value = "Check out an item",
        nickname = "checkOut",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "lms",
            paramType = "query",
            allowMultiple = false,
            allowableValues = HOST_LMS_ADAPTERS,
            required = true,
            value = "The type of the host LMS",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipServerAddress",
            paramType = "query",
            required = true,
            value = "The address for the ncip server",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgency",
            paramType = "query",
            required = true,
            value = "The ncip from agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipToAgency",
            paramType = "query",
            required = false,
            value = "The ncip to agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipAppProfile",
            paramType = "query",
            required = true,
            value = "The ncip app profile",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgencyAuthentication",
            paramType = "query",
            required = false,
            value = "The from agency authentication",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiKey",
            paramType = "query",
            required = false,
            value = "The wms api key",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiSecret",
            paramType = "query",
            required = false,
            value = "The wms api secret",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsLookupPatronEndpoint",
            paramType = "query",
            required = false,
            value = "The wms lookup patron endpoint",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsRegistryId",
            paramType = "query",
            required = false,
            value = "The WMS registry id",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "requestId",
            paramType = "query",
            required = true,
            value = "The request id",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "itemBarcode",
            paramType = "query",
            required = true,
            value = "The item barcode",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "borrowerBarcode",
            paramType = "query",
            required = true,
            value = "The borrower barcode to whom we check it out to",
            dataType = "string"
        )
    ])
    def checkOut() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_TEST_NCIP_CHECK_OUT);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        Map result = [ : ];

        // Must have been supplied the LMS
        if (params.lms) {
            HostLMSActions hostLMSActions = hostLMSService.getHostLMSActionsFor(params.lms);

            if (hostLMSActions == null) {
                result.error = "unable to determine service for lms: " + params.lms;
            } else  {
                // Must be supplied a an item barcode, borrower barcode and a request id
                if (params.requestId && params.itemBarcode && params.borrowerBarcode) {
                    // Now we need to create our settings that will be used
                    MapSettings settings = createAuthSettings();

                    // Settings need to know we are using ncip for check out
                    settings.add(SettingsData.SETTING_CHECK_OUT_ITEM, CIRC_TYPE_NCIP);

                    // Now we can make the call
                    result = hostLMSActions.checkoutItem(settings, params.requestId, params.itemBarcode, params.borrowerBarcode);
                } else {
                    result.error = "Need to supply the requestId, itemBarcode and borrowerBarcodeno parameters to test check in";
                }
            }
        } else {
            result.error = "lms not supplied";
        }

        render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     * Checks in an item to the local LMS
     * @return The result from the check in call
     */
    @ApiOperation(
        value = "Check in an item",
        nickname = "checkIn",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "lms",
            paramType = "query",
            allowMultiple = false,
            allowableValues = HOST_LMS_ADAPTERS,
            required = true,
            value = "The type of the host LMS",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipServerAddress",
            paramType = "query",
            required = true,
            value = "The address for the ncip server",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgency",
            paramType = "query",
            required = true,
            value = "The ncip from agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipToAgency",
            paramType = "query",
            required = false,
            value = "The ncip to agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipAppProfile",
            paramType = "query",
            required = true,
            value = "The ncip app profile",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgencyAuthentication",
            paramType = "query",
            required = false,
            value = "The from agency authentication",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiKey",
            paramType = "query",
            required = false,
            value = "The wms api key",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiSecret",
            paramType = "query",
            required = false,
            value = "The wms api secret",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsLookupPatronEndpoint",
            paramType = "query",
            required = false,
            value = "The wms lookup patron endpoint",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsRegistryId",
            paramType = "query",
            required = false,
            value = "The WMS registry id",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "itemBarcode",
            paramType = "query",
            required = true,
            value = "The item barcode",
            dataType = "string"
        )
    ])
    def checkIn() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_TEST_NCIP_CHECK_IN);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        Map result = [ : ];

        // Must have been supplied the LMS
        if (params.lms) {
            HostLMSActions hostLMSActions = hostLMSService.getHostLMSActionsFor(params.lms);

            if (hostLMSActions == null) {
                result.error = "unable to determine service for lms: " + params.lms;
            } else  {
                // Must be supplied an item barcode
                if (params.itemBarcode) {
                    // Now we need to create our settings that will be used
                    MapSettings settings = createAuthSettings();

                    // Settings need to know we are using ncip for check out
                    settings.add(SettingsData.SETTING_CHECK_IN_ITEM, CIRC_TYPE_NCIP);

                    // Now we can make the call
                    result = hostLMSActions.checkInItem(settings, params.itemBarcode);
                } else {
                    result.error = "Need to supply the itemBarcode to check the item in";
                }
            }
        } else {
            result.error = "lms not supplied";
        }

        render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     * Accept item, checks the item into the local LMS
     * @return The result from the accept item call
     */
    @ApiOperation(
        value = "Accept Item",
        nickname = "acceptItem",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "lms",
            paramType = "query",
            allowMultiple = false,
            allowableValues = HOST_LMS_ADAPTERS,
            required = true,
            value = "The type of the host LMS",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipServerAddress",
            paramType = "query",
            required = true,
            value = "The address for the ncip server",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgency",
            paramType = "query",
            required = true,
            value = "The ncip from agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipToAgency",
            paramType = "query",
            required = false,
            value = "The ncip to agency",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipAppProfile",
            paramType = "query",
            required = true,
            value = "The ncip app profile",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipFromAgencyAuthentication",
            paramType = "query",
            required = false,
            value = "The from agency authentication",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiKey",
            paramType = "query",
            required = false,
            value = "The wms api key",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiSecret",
            paramType = "query",
            required = false,
            value = "The wms api secret",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsLookupPatronEndpoint",
            paramType = "query",
            required = false,
            value = "The wms lookup patron endpoint",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsRegistryId",
            paramType = "query",
            required = false,
            value = "The WMS registry id",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "itemBarcode",
            paramType = "query",
            required = true,
            value = "The item barcode",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "requestId",
            paramType = "query",
            required = true,
            value = "The request identifier",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "userId",
            paramType = "query",
            required = true,
            value = "The user identifier barcode",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "author",
            paramType = "query",
            required = true,
            value = "The author",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "title",
            paramType = "query",
            required = true,
            value = "The title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "isbn",
            paramType = "query",
            required = false,
            value = "The ISBN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "callNumber",
            paramType = "query",
            required = false,
            value = "The call number",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "pickupLocation",
            paramType = "query",
            required = true,
            value = "The pickup location",
            dataType = "string"
        )
    ])
    def acceptItem() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_TEST_NCIP_ACCEPT_ITEM);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        Map result = [ : ];

        // Must have been supplied the LMS
        if (params.lms) {
            HostLMSActions hostLMSActions = hostLMSService.getHostLMSActionsFor(params.lms);

            if (hostLMSActions == null) {
                result.error = "unable to determine service for lms: " + params.lms;
            } else  {
                // Must have been supplied various fields
                if (params.itemBarcode &&
                    params.requestId &&
                    params.userId &&
                    params.author &&
                    params.title &&
                    params.pickupLocation) {
                    // Now we need to create our settings that will be used
                    MapSettings settings = createAuthSettings();

                    // Settings need to know we are using ncip for check out
                    settings.add(SettingsData.SETTING_ACCEPT_ITEM, CIRC_TYPE_NCIP);

                    // Now we can make the call
                    result = hostLMSActions.acceptItem(
                        settings,
                        params.itemBarcode,
                        params.requestId,
                        params.userId,
                        params.author,
                        params.title,
                        params.isbn,
                        params.callNumber,
                        params.pickupLocation,
                        null);
                } else {
                    result.error = "Need to supply all the following fields itemBarcode, requestId, userId, author, title and pickupLocation to accept the item";
                }
            }
        } else {
            result.error = "lms not supplied";
        }

        render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     * Determines the best location
     * @return The result from the determineBestLocation call
     */
    @ApiOperation(
        value = "Determine best location",
        nickname = "determineBestLocation",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "lms",
            paramType = "query",
            allowMultiple = false,
            allowableValues = HOST_LMS_ADAPTERS,
            required = true,
            value = "The type of the host LMS",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "z3950ServerAddress",
            paramType = "query",
            required = true,
            value = "The address for the z3950 server",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "z3950ProxyAddress",
            paramType = "query",
            required = true,
            value = "The address for the z3950 proxy",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ncipServerAddress",
            paramType = "query",
            required = false,
            value = "The address for the ncip server (required for voyager) lms",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "voyagerItemApiAddress",
            paramType = "query",
            required = false,
            value = "The voyager item API address",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsConnectorAddress",
            paramType = "query",
            required = false,
            value = "The wms connector address",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsUsername",
            paramType = "query",
            required = false,
            value = "The wms username",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsPassword",
            paramType = "query",
            required = false,
            value = "The wms password",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiKey",
            paramType = "query",
            required = false,
            value = "The wms api key",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsApiSecret",
            paramType = "query",
            required = false,
            value = "The wms api secret",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "wmsRegistryId",
            paramType = "query",
            required = false,
            value = "The WMS registry id",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "supplierUniqueRecordIdentifier",
            paramType = "query",
            required = false,
            value = "The supplier uniqie record identifier",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "isbn",
            paramType = "query",
            required = false,
            value = "The ISBN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "title",
            paramType = "query",
            required = false,
            value = "The title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "oclcNumber",
            paramType = "query",
            required = false,
            value = "The OCLC number",
            dataType = "string"
        )
    ])
    def determineBestLocation() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_TEST_NCIP_ACCEPT_ITEM);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        Map result = [ : ];

        // Must have been supplied the LMS
        if (params.lms) {
            HostLMSActions hostLMSActions = hostLMSService.getHostLMSActionsFor(params.lms);

            if (hostLMSActions == null) {
                result.error = "unable to determine service for lms: " + params.lms;
            } else  {
                // Must have been supplied various fields
                if (params.supplierUniqueRecordIdentifier ||
                    params.isbn ||
                    params.title ||
                    params.oclcNumber) {
                    // Now we need to create our settings that will be used
                    MapSettings settings = createAuthSettings();

                    // We need to create a request, which will get thrown away
                    PatronRequest patronRequest = new PatronRequest();
                    patronRequest.title = params.title;
                    patronRequest.isbn = params.isbn;
                    patronRequest.oclcNumber = params.oclcNumber;
                    patronRequest.supplierUniqueRecordId = params.supplierUniqueRecordIdentifier;

                    // determineBestLocation requires a transaction
                    PatronRequest.withTransaction { tstatus ->
                        try {
                            // Now we can make the call
                            IHoldingLogDetails holdingLogDetails = new HoldingLogDetails(ProtocolType.Z3950_RESPONDER, ProtocolMethod.GET);
                            result.itemLocation = hostLMSActions.determineBestLocation(settings, patronRequest, holdingLogDetails);
                            result.logging = holdingLogDetails.toMap();
                        } catch (Exception e) {
                            log.error("Exception thrown, while determining best location", e);
                            result.error = "Exception: " + e.detailMessage;
                        }
                    }
                } else {
                    result.error = "Need to supply one of the following fields supplierUniqueRecordIdentifier, isbn, title or oclc number to determine the best location";
                }
            }
        } else {
            result.error = "lms not supplied";
        }

        render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     * Sets up the connection details in a MapSettings object bases on what has been passed in through the params
     * @return The MapSettings instance containing the connection settings
     */
    private MapSettings createAuthSettings() {
        MapSettings settings = new MapSettings();
        settings.add(SettingsData.SETTING_NCIP_SERVER_ADDRESS, params.ncipServerAddress);
        settings.add(SettingsData.SETTING_NCIP_FROM_AGENCY, params.ncipFromAgency);
        settings.add(SettingsData.SETTING_NCIP_TO_AGENCY, params.ncipToAgency);
        settings.add(SettingsData.SETTING_NCIP_APP_PROFILE, params.ncipAppProfile);
        settings.add(SettingsData.SETTING_WMS_REGISTRY_ID, params.wmsRegistryId);
        settings.add(SettingsData.SETTING_NCIP_FROM_AGENCY_AUTHENTICATION, params.ncipFromAgencyAuthentication);
        settings.add(SettingsData.SETTING_WMS_API_KEY, params.wmsApiKey);
        settings.add(SettingsData.SETTING_WMS_API_SECRET, params.wmsApiSecret);
        settings.add(SettingsData.SETTING_WMS_LOOKUP_PATRON_ENDPOINT, params.wmsLookupPatronEndpoint);

        // Additional settings ones used for z3950 / item lookup
        settings.add(SettingsData.SETTING_Z3950_SERVER_ADDRESS, params.z3950ServerAddress);
        settings.add(SettingsData.SETTING_Z3950_PROXY_ADDRESS, params.z3950ProxyAddress);
        settings.add(SettingsData.SETTING_VOYAGER_ITEM_API_ADDRESS, params.voyagerItemApiAddress);
        settings.add(SettingsData.SETTING_WMS_CONNECTOR_ADDRESS, params.wmsConnectorAddress);
        settings.add(SettingsData.SETTING_WMS_CONNECTOR_USERNAME, params.wmsUsername);
        settings.add(SettingsData.SETTING_WMS_CONNECTOR_PASSWORD, params.wmsPassword);

        return(settings);
    }
}
