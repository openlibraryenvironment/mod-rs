package mod.rs

import org.olf.rs.BackgroundTaskService;
import org.olf.rs.shared.TenantSymbolMapping;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import org.olf.rs.PatronRequest;
import groovy.transform.CompileStatic;

/**
 * This controller is hit by a timer from OKAPI every 2 minutes/
 * To make use of the generic database functionality we need to pass it a domain,
 * Which gives this controller more functionality than it needs or requires but gives us a database connection to work with.
 *
 * @author Chas
 *
 */
@CurrentTenant
@CompileStatic
class ReshareSettingsController extends OkapiTenantAwareController<TenantSymbolMapping> {

    BackgroundTaskService backgroundTaskService;

    private static int worker_count = 0;

    ReshareSettingsController() {
        super(TenantSymbolMapping);
    }

  def worker() {
    Map result = [result:'OK'];
    String tenant_header = request.getHeader('X-OKAPI-TENANT')
    log.info("worker call start tenant: ${tenant_header} ${worker_count++}");

    backgroundTaskService.logSystemInfo();

    try {
      PatronRequest.withTransaction {
        backgroundTaskService.performReshareTasks();
      }
    }
    catch ( Exception e ) {
      log.error("Problem in background task service",e);
    }
    finally {
      log.info("worker call completed tenant: ${tenant_header}");
    }
    render result as JSON
  }
}
