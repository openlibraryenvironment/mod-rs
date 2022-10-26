package org.olf.rs.sharedindex.jiscdiscover

import groovyx.net.http.ApacheHttpBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.config.RequestConfig
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import static groovyx.net.http.ApacheHttpBuilder.configure


/**
 */
public class JiscDiscoverApiConnectionImpl implements JiscDiscoverApiConnection {

  public Object getSru(Map description) {

    // https://discover.libraryhub.jisc.ac.uk/sru-api?operation=searchRetrieve&version=1.1&query=rec.id%3d%2231751908%22&maximumRecords=1
    HttpBuilder jiscDiscover = configure {
      request.uri = 'https://discover.libraryhub.jisc.ac.uk'
    }

    Object result = jiscDiscover.get {
      request.uri.path='/search' // ?id=3568439&rn=1&format=json
      request.accept='text/xml'
      request.uri.query=[
        'id':description.systemInstanceIdentifier,
        'format':'json'
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

