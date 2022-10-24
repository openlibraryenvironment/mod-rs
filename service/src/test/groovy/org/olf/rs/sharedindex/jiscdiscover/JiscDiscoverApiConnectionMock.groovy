package org.olf.rs.sharedindex.jiscdiscover

import groovyx.net.http.ApacheHttpBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.config.RequestConfig
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import static groovyx.net.http.ApacheHttpBuilder.configure
import groovy.util.slurpersupport.GPathResult;
import groovy.util.logging.Slf4j

/**
 * HttpBuilderNG returns groovy.util.slurpersupport.GPathResult from parsed XML response records
 */

@Slf4j
public class JiscDiscoverApiConnectionMock implements JiscDiscoverApiConnection {

  public GPathResult getSru(Map description) {

    log.debug("JiscDiscoverApiConnectionMock::getSru(${description})");

    GPathResult result = null;

    if ( description?.systemInstanceIdentifier == '2231751908' ) {
      InputStream is = this.getClass().getResourceAsStream("/sharedindex/jiscdiscover/jd_rec_id_2231751908.xml");
      result = new XmlSlurper().parse(is)
    }
    else {
      log.debug("No matching mock systemInstanceIdentifier - return null");
    }

    return result;
  }

}

