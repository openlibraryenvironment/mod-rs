package mod.rs

import org.olf.rs.BackgroundTaskService;
import org.olf.rs.shared.TenantSymbolMapping;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;

/**
 * This controller is hit by a timer from OKAPI every 2 minutes/
 * To make use of the generic database functionality we need to pass it a domain,
 * Which gives this controller more functionality than it needs or requires but gives us a database connection to work with.
 *
 * @author Chas
 *
 */
@CurrentTenant
class ReshareSettingsController extends OkapiTenantAwareController<TenantSymbolMapping> {

    BackgroundTaskService backgroundTaskService;

    ReshareSettingsController() {
        super(TenantSymbolMapping);
    }

    def worker() {
        def result = [result:'OK'];
        backgroundTaskService.performReshareTasks();
        render result as JSON
    }
}
