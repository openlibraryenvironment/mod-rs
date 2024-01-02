package mod.rs;

import groovy.xml.XmlUtil;
import org.olf.rs.HostLMSService;
import org.olf.rs.Patron;
import org.olf.rs.ReshareActionService;
import org.olf.rs.SettingsService;
import org.olf.rs.logging.ContextLogging;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.olf.rs.logging.INcipLogDetails;
import org.olf.rs.logging.NcipLogDetails;

@Slf4j
@CurrentTenant
@Api(value = "/rs/patron", tags = ["Patron Controller"], description = "API for all things to do with patrons")
class PatronController extends OkapiTenantAwareSwaggerController<Patron>  {

    private static final String RESOURCE_PATRON = Patron.getSimpleName();

    PatronController() {
        super(Patron)
    }

    HostLMSService hostLMSService;
    ReshareActionService reshareActionService;
    SettingsService settingsService;

    /**
     * Looks up the patron to see if the profile they belong to is allowed to make requests
     * @return a map containing the result of the call that can contain the following fields:
     *      patronValid ... can the patron create requests
     *      problems ... An array of reasons that explains either a FAIL or the patron is not valid
     *      status ... the status of the patron (FAIL or OK)
     */
    @ApiOperation(
        value = "Is the user allowed to make requests",
        nickname = "{patronIdentifier}/canCreateRequest",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "patronIdentifier",
            paramType = "path",
            required = true,
            allowMultiple = false,
            value = "The identifier of the patron",
            dataType = "string"
        )
    ])
    def canCreateRequest() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, RESOURCE_PATRON);
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_CAN_CREATE_REQUEST);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        Map result = [ : ];
        Patron.withTransaction { status ->
          // We do need to be supplied a patronId
          if (params?.patronIdentifier == null) {
            response.status = 400;
            result.message = 'No patron identifier supplied to perform the check';
          } else {
            // Lookup the patron
            result = reshareActionService.lookupPatron([ patronIdentifier : params.patronIdentifier ]);

            // Remove the patron details and callSuccess as they should not be passed back
            result.remove('callSuccess');
            result.remove('patronDetails');
          }
        }
        render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    @ApiOperation(
        value = "Validate barcode/pin and determine authorization",
        nickname = "validate",
        produces = "application/json",
        httpMethod = "POST"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 403, message = "User not authorized to place requests"),
        @ApiResponse(code = 401, message = "Invalid barcode/pin"),
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            paramType = "body",
            required = true,
            allowMultiple = false,
            value = 'JSON with properties "barcode" and "pin"',
            defaultValue = "{}",
            dataType = "string"
        )
    ])
    def validate() {
        String barcode = request.JSON?.barcode;
        String pin = request.JSON?.pin
        if (!barcode || !pin) {
            response.status = 400;
            render ([message: 'Missing required barcode and pin']) as JSON;
            return;
        }
        String escapedBarcode = XmlUtil.escapeXml(XmlUtil.escapeControlCharacters(request.JSON.barcode));
        String escapedPin = XmlUtil.escapeXml(XmlUtil.escapeControlCharacters(request.JSON.pin));
        if (!(escapedBarcode == barcode) || !(escapedPin == pin)) {
            response.status = 400;
            render ([message: 'Unsupported characters']) as JSON;
            return;
        }
        def lmsActions = hostLMSService.getHostLMSActions();
        INcipLogDetails ncipLogDetails = new NcipLogDetails();
        def ncipResult = lmsActions.lookupPatronByBarcodePin(settingsService, request.JSON.barcode, request.JSON.pin, ncipLogDetails);
        if (ncipResult.status == 'OK' && ncipResult.userid) {
            render (['userid':ncipResult.userid]) as JSON;
        } else if (ncipResult.status == 'BLOCKED') {
            response.status = 403;
        } else {
            response.status = 401;
        }
    }
}
