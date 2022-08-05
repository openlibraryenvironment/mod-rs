package org.olf.rs;

/**
 * Send a query to a Z39.50 system. Currently this abstraction likely only supports doing so if a proxy is configured.
 */
import com.k_int.web.toolkit.settings.AppSetting;
import groovyx.net.http.HttpBuilder
import org.olf.rs.referenceData.SettingsData;

class Z3950Service {
    def query(String query, int max = 3, String schema = null) {
        def proxySetting = AppSetting.findByKey(SettingsData.SETTING_Z3950_PROXY_ADDRESS);
        String z3950_proxy = proxySetting?.value ?: proxySetting.defValue;
        if (!z3950_proxy) throw new Exception('Unable to query Z39.50, no proxy configured');
        String z3950_server = AppSetting.findByKey(SettingsData.SETTING_Z3950_SERVER_ADDRESS)?.value;
        if (!z3950_server) throw new Exception('Unable to query Z39.50, no server configured');

        def z_response = HttpBuilder.configure {
            request.uri = z3950_proxy
        }.get {
            request.uri.path = '/'
            request.uri.query = ['x-target': z3950_server,
                                 'x-pquery': query,
                                 'maximumRecords': "$max" ]

            if (schema) {
                request.uri.query['recordSchema'] = schema;
            }

            log.debug("Querying z server with URL ${request.uri?.toURI().toString()}")
        }

        log.debug("Got Z3950 response: ${z_response}");
        return z_response;
    }
}
