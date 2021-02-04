package mod.rs;

import groovy.util.logging.Slf4j;
import grails.gorm.multitenancy.CurrentTenant;
import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.rs.FolioSharedIndexService;
import groovyx.net.http.FromServer;

@Slf4j
@CurrentTenant
class SharedIndexQueryController {

  FolioSharedIndexService folioSharedIndexService

  def query() {
    def stream;
    def status;
    folioSharedIndexService.queryPassthrough(request).get() {
      def parser = { Object cfg, FromServer fs ->
        stream = fs.inputStream;
        status = fs.getStatusCode();
      }
      // Doesn't seem to be a clear way to just disable the parser irrespective of
      // content-type, will probably ultimately want to eschew HttpBuilder entirely
      // for this interface
      response.parser('application/json', parser);
      response.parser('text/plain', parser);
      response.success { FromServer fs ->
        log.debug("Success response from shared index query passthrough: ${fs.getStatusCode()}");
      }
      response.failure { FromServer fs ->
        log.debug("Failure response from shared index query passthrough: ${status}");
      }
    };

    response.setStatus(status);
    response << stream;
  }
}
