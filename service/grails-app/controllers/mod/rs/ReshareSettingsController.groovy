package mod.rs

import org.olf.rs.BackgroundTaskService;
import org.olf.rs.PatronRequest;
import org.olf.rs.shared.TenantSymbolMapping;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This controller is hit by a timer from OKAPI every 2 minutes/
 * To make use of the generic database functionality we need to pass it a domain,
 * Which gives this controller more functionality than it needs or requires but gives us a database connection to work with.
 *
 * @author Chas
 *
 */
@CurrentTenant
@Api(value = "/rs/settings", tags = ["ReShare Settings Controller"], description = "API for all things to do with reshare settings ...")
class ReshareSettingsController extends OkapiTenantAwareController<TenantSymbolMapping> {

  BackgroundTaskService backgroundTaskService;

  ReshareSettingsController() {
    super(TenantSymbolMapping);
  }

    @ApiOperation(
        value = "Triggers the background tasks for the instance",
        nickname = "worker",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
  def worker() {

    def result = [result:'OK'];
    String tenant = request.getHeader('X-OKAPI-TENANT')
    log.info("worker call start tenant: ${tenant}");
    try {
      PatronRequest.withTransaction {
        backgroundTaskService.performReshareTasks(tenant);
      }
    }
    catch ( Exception e ) {
      log.error("Problem in background task service",e);
    }
    finally {
      log.info("worker call completed tenant: ${tenant}");
    }
    render result as JSON
  }
}
