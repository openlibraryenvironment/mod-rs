package mod.rs

import org.springframework.http.HttpStatus;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.artefact.Artefact;
import grails.gorm.multitenancy.CurrentTenant;
import grails.gorm.transactions.Transactional;

@CurrentTenant
@Artefact('Controller')
class HasHiddenRecordController<T> extends OkapiTenantAwareController<T> {

    private final static String PROPERTY_HIDDEN = 'hidden';

    HasHiddenRecordController(Class<T> resource) {
        super(resource)
    }

    def index() {
        // Closure definition can be found at https://gorm.grails.org/latest/hibernate/manual/#criteria
        respond doTheLookup(resource, {
            or {
                // How can I user PROPERTY_HIDDEN here, as it just tried to used PROPERTY_HIDDEN as the property name rether than its value
                eq('hidden', false)
                isNull('hidden')
            }
        });
    }

    @Transactional
    def delete() {
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
    }
}
