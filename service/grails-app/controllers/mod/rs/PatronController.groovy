package mod.rs;

import org.olf.rs.ReshareActionService;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;

@Slf4j
@CurrentTenant
class PatronController extends OkapiTenantAwareController  {

    ReshareActionService reshareActionService;

    /**
     * Looks up the patron to see if the profile they belong to is allowed to make requests
     * @return a map containing the result of the call that can contain the following fields:
     *      callSuccess ... was the call a success or not
     *      patronDetails ... the details of the patron if the patron is a valid user
     *      patronValid ... can the patron create requests
     *      status ... the status of the patron
     */
    def canCreateRequest() {
        Map result = [ : ];

        // We do need to be supplied a patronId
        if (params?.patronIdentifier == null) {
            response.status = 400;
            result.message = 'No patron identifier supplied to perform the check';
        } else {
            // Lookup the patron
            result = reshareActionService.lookupPatron([ patronIdentifier : params.patronIdentifier ]);

            // Remove the patron details as they should not be passed back
            result.remove('patronDetails');
        }
        render result as JSON;
    }
}
