package mod.rs;

import org.olf.rs.Patron;
import org.olf.rs.ReshareActionService;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Slf4j
@CurrentTenant
@Api(value = "/rs/patron", tags = ["Patron Controller"], description = "API for all things to do with patrons")
class PatronController extends OkapiTenantAwareSwaggerController<Patron>  {

    PatronController() {
        super(Patron)
    }

    ReshareActionService reshareActionService;

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
    }
}
