package mod.rs;

import org.olf.rs.Batch;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;

@Slf4j
@CurrentTenant
@Api(value = "/rs/batch", tags = ["Batch Controller"], description = "API for all things to do with batches")
class BatchController extends OkapiTenantAwareController<Batch>  {

	BatchController() {
		super(Batch)
	}
}
