package org.olf.rs.sharedindex.jiscdiscover

import groovyx.net.http.ApacheHttpBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.config.RequestConfig
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import static groovyx.net.http.ApacheHttpBuilder.configure
import groovy.util.slurpersupport.GPathResult;


/**
 */
public class JiscDiscoverApiConnectionImpl implements JiscDiscoverApiConnection {

  public GPathResult getSru(Map description) {

    // https://discover.libraryhub.jisc.ac.uk/sru-api?operation=searchRetrieve&version=1.1&query=rec.id%3d%2231751908%22&maximumRecords=1
    HttpBuilder jiscDiscover = configure {
      request.uri = 'https://discover.libraryhub.jisc.ac.uk'
    }

    GPathResult result = (GPathResult) jiscDiscover.get {
      request.uri.path='/sru-api'
      request.accept='text/xml'
      request.uri.query=[
        'operation':'searchRetrieve',
        'version':'1.1',
        'query':'rec.id=31751908',
        'maximumRecords':1
      ]

      response.success { FromServer fs, Object body ->
        return body
      }
      response.failure { FromServer fs, Object body ->
        println("Problem ${body} ${fs} ${fs.getStatusCode()}");
        return null
      }

    }

    return result;
  }

}

