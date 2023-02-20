package mod.rs

import org.olf.rs.logging.ContextLogging;
import org.springframework.http.HttpStatus;

import grails.artefact.Artefact;
import grails.gorm.multitenancy.CurrentTenant;
import grails.gorm.transactions.Transactional;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CurrentTenant
@Artefact('Controller')
class HasHiddenRecordController<T> extends OkapiTenantAwareSwaggerController<T> {

    private final static String PROPERTY_HIDDEN = 'hidden';

    HasHiddenRecordController(Class<T> resource) {
        super(resource)
    }

    @Override
    def index(Integer max) {
        // Setup the variables we want to log
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, resource.getSimpleName());
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_SEARCH);
        ContextLogging.setValue(ContextLogging.FIELD_TERM, params.term);
        ContextLogging.setValue(ContextLogging.FIELD_FIELDS_TO_MATCH, params.match);
        ContextLogging.setValue(ContextLogging.FIELD_FILTERS, params.filters);
        ContextLogging.setValue(ContextLogging.FIELD_SORT, params.sort);
        ContextLogging.setValue(ContextLogging.FIELD_MAXIMUM_RESULTS, max);
        ContextLogging.setValue(ContextLogging.FIELD_NUMBER_PER_PAGE, params.perPage);
        ContextLogging.setValue(ContextLogging.FIELD_OFFSET, params.offset);
        ContextLogging.setValue(ContextLogging.FIELD_PAGE, params.page);
        ContextLogging.setValue(ContextLogging.FIELD_STATISTICS_REQUIRED, params.stats);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        // Closure definition can be found at https://gorm.grails.org/latest/hibernate/manual/#criteria
        respond doTheLookup(resource, {
            or {
                // How can I user PROPERTY_HIDDEN here, as it just tried to used PROPERTY_HIDDEN as the property name rether than its value
                eq('hidden', false)
                isNull('hidden')
            }
        });

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    @Transactional
    @Override
    // Not quite sure why it dosn't inherit the @ApiImplicitParams from the paren
    @ApiOperation(
        value = "Deletes the record with the supplied identifier",
        nickname = "{id}",
        httpMethod = "DELETE"
    )
    @ApiResponses([
        @ApiResponse(code = 204, message = "Deleted")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "id",
            paramType = "path",
            required = true,
            allowMultiple = false,
            value = "The id of the record to be deleted",
            dataType = "string"
        )
    ])
    def delete() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, resource.getSimpleName());
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_DELETE);
        ContextLogging.setValue(ContextLogging.FIELD_ID, params.id);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        T instance = queryForResource(params.id)

        // Not found.
        if (instance == null) {
            transactionStatus.setRollbackOnly();
            notFound();
        } else {
            // Return the relevant status if not allowed to delete.
            Map deletable = instance.metaClass.respondsTo(instance, 'canDelete') ? instance.canDelete() : [ deleteValid: true ];

            if (deletable.deleteValid) {
                // Delete the instance
                deleteResource instance
            } else {
                log.info('Marking ' + instance.class.name + ':' +params.getIdentifier() + ' as hidden because ' + deletable.error)
                instance.hidden = true;
                instance.save(flush:true, failOnError:true);
            }

            // It has either been deleted or marked as hidden, either way the user thinks it has been deleted
            render status : HttpStatus.NO_CONTENT;
        }

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }
}
