package mod.rs;

import org.olf.rs.Patron;
import org.olf.rs.ReshareActionService;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;

@Slf4j
@CurrentTenant
class PatronController extends OkapiTenantAwareController<Patron>  {

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
    def canCreateRequest() {
        Map result = [ : ];

        // We do need to be supplied a patronId
        if (params?.patronIdentifier == null) {
            response.status = 400;
            result.message = 'No patron identifier supplied to perform the check';
        } else {
            // Lookup the patron
            
            Patron.withTransaction { status ->
              result = reshareActionService.lookupPatron([ patronIdentifier : params.patronIdentifier ]);
            }

            // Remove the patron details and callSuccess as they should not be passed back
            result.remove('callSuccess');
            result.remove('patronDetails');
        }
        render result as JSON;
    }
}
