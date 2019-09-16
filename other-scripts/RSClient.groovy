import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import org.apache.http.*
import org.apache.http.protocol.*
import java.nio.charset.Charset
import static groovy.json.JsonOutput.*
import groovy.util.slurpersupport.GPathResult
import org.apache.log4j.*
import java.text.SimpleDateFormat
import groovyx.net.http.*
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import groovy.json.JsonOutput
import java.io.File;
import org.ini4j.*;

public class RSClient {

  OkapiClient okapi_client

  public RSClient(OkapiClient okapi_client) {
    println('RSClient::RSClient');
    this.okapi_client = okapi_client;
  }

}
