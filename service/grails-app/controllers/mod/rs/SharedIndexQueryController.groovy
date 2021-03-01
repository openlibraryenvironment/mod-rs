package mod.rs;

import groovy.util.logging.Slf4j;
import grails.gorm.multitenancy.CurrentTenant;
import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.rs.FolioSharedIndexService;
import org.olf.rs.SharedIndexService;
import groovyx.net.http.FromServer;

@Slf4j
@CurrentTenant
class SharedIndexQueryController {

  SharedIndexService sharedIndexService

  def query() {
    def stream;
    def status;
    def headers;
    def si = sharedIndexService.getSharedIndexActions();

    // right now only FOLIO SI has such a method, eventually (TODO) we should
    // have an interface for the passthrough but it probably won't be based on
    // HttpBuilder for reasons below
    if (!(si instanceof FolioSharedIndexService)) {
      def msg = "Query passthrough accessed on a shared index that does not implement it";
      log.warn(msg);
      response.sendError(422, msg);
      return;
    }

    si.queryPassthrough(request).get() {
      def parser = { Object cfg, FromServer fs ->
        stream = fs.inputStream;
        status = fs.getStatusCode();
        headers = fs.getHeaders();
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

    def passHeaders = ['Content-Type'];
    passHeaders.each {
      response.setHeader(it, headers.find { h-> h.key == it }.getValue());
    }

    response.setStatus(status);
    response << stream;
  }
}
