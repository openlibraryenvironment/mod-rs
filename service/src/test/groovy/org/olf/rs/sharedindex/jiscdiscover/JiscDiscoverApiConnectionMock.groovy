package org.olf.rs.sharedindex.jiscdiscover

import groovyx.net.http.ApacheHttpBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.config.RequestConfig
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import static groovyx.net.http.ApacheHttpBuilder.configure

/**
 */
public class JiscDiscoverApiConnectionMock implements JiscDiscoverApiConnection {

  public Object getSru(Map description) {

    // https://discover.libraryhub.jisc.ac.uk/sru-api?operation=searchRetrieve&version=1.1&query=rec.id%3d%2231751908%22&maximumRecords=1
    HttpBuilder jiscDiscover = configure {
      request.uri = 'https://discover.libraryhub.jisc.ac.uk'
    }

    jiscDiscover.get {
      request.uri.path='/sru-api'
      request.accept='application/json'
      request.contentType='application/json'
      request.uri.query=[
        'operation':'searchRetrieve',
        'version':'1.1',
        'query':'rec.id=2231751908',
        'maximumRecords':1
      ]

      response.success { FromServer fs, Object body ->
        println("pull modules OK");
      }
      response.failure { FromServer fs, Object body ->
        println("Problem ${body} ${fs} ${fs.getStatusCode()}");
      }

    }

  }

}

