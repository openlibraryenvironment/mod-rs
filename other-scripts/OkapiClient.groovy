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
import com.k_int.goai.*;
import java.text.SimpleDateFormat
import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import groovyx.net.http.*
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import groovy.json.JsonOutput
import java.io.File;
import org.ini4j.*;


/**
 * An Okapi Client for use in the groovysh
 */
public class OkapiClient {

  private String config_id;
  private String url;
  private String tenant;
  private String password;
  private String username;

  private HTTPBuilder httpclient = null;



  private OkapiClient() {
  }

  public OkapiClient(String config) {
    Wini ini = new Wini(new File(System.getProperty("user.home")+'/.folio/credentials'));

    String config_id = 'kidemo'
    String url = ini.get(config_id, 'url', String.class);
    String tenant = ini.get(config_id, 'tenant', String.class);
    String password = ini.get(config_id, 'password', String.class);
    String username = ini.get(config_id, 'username', String.class);
  }


  public boolean connect() {
    println("Connected...");
    httpclient = new HTTPBuilder(url);
    return true;
  }

  public boolean createTenant(String tenant) {
    println("createTenant...");
    return true;
  }

  private login() {
    // Post to https://okapi-reshare.apps.k-int.com/bl-users/login?expandPermissions=true&fullPermissions=true
    // { username:x password:y }
  }
}
