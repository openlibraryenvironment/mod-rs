@Grab('io.github.http-builder-ng:http-builder-ng-apache:1.0.4')


import groovyx.net.http.ApacheHttpBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.config.RequestConfig
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import groovyx.net.http.ChainedHttpConfig

import static groovyx.net.http.ApacheHttpBuilder.configure


def r = getSru([:]);
println("result title: ${r.records.record.recordData.mods.titleInfo.title}");
println("Availability");

// See https://www.bl.uk/bibliographic/pdfs/marc-codes-directory.pdf for location codes
r.records.record.recordData.mods.extension.modsCollection.mods.each { mc ->
  println("Processing a mc");

  mc.recordInfo.recordIdentifier.each { loc_specific_record_id ->
    println("    rec [${loc_specific_record_id.'@source'}] : ${loc_specific_record_id.text()}");
  }

  mc.location.each { loc ->
    String ukmac_code = loc.physicalLocation.find { it.'@authority'=='UkMaC' }
    println("  UkMac location code: ${ukmac_code}");

    loc.holdingSimple.copyInformation.each { ci ->
      // Have seen (Not Borrowable) in subLocation as an indication of policy
      println("    subloc: ${ci.subLocation}")
      println("    shelf: ${ci.shelfLocator}")
    }
  }
}

System.exit(0)

Object getSru(Map description) {
  // https://discover.libraryhub.jisc.ac.uk/sru-api?operation=searchRetrieve&version=1.1&query=rec.id%3d%2231751908%22&maximumRecords=1
  HttpBuilder jiscDiscover = configure {
    request.uri = 'https://discover.libraryhub.jisc.ac.uk'
  }

  def resp = jiscDiscover.get {
    request.uri.path='/sru-api'
    request.accept='text/xml'
    // request.contentType='application/json'
    request.uri.query=[
      'operation':'searchRetrieve',
      'version':'1.1',
      // 'query':'rec.id%3d2231751908',
      'query':'rec.id=31751908',
      'maximumRecords':1
    ]

    response.success { FromServer fs, Object body ->
      fs.headers.each { h ->
        println("SuccessHeader: ${h}");
      }
      return body
    }

    response.failure { FromServer fs, Object body ->
      println("Problem ${body} ${fs} ${fs.getStatusCode()}");
      return null
    }

    // Prevent parsing
    // response.parser('text/xml') { ChainedHttpConfig cfg, FromServer fs ->
    //   fs.inputStream.text
    // }

  }

  return resp;
}

