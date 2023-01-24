package mod.rs;

import org.olf.rs.Batch;

import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;

@Slf4j
@CurrentTenant
@Api(value = "/rs/batch", tags = ["Batch Controller"], description = "API for all things to do with batches")
class BatchController extends OkapiTenantAwareSwaggerController<Batch>  {

	BatchController() {
		super(Batch)
	}
}
