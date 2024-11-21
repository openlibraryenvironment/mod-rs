package org.olf.rs;

/**
 * Send a query to a Z39.50 system. Currently this abstraction likely only supports doing so if a proxy is configured.
 */
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;

import groovy.xml.XmlUtil;
import groovyx.net.http.HttpBuilder;

class Z3950Service {
    def query(ISettings settings, String query, int max, String schema, IHoldingLogDetails holdingLogDetails) {
        String z3950_proxy = settings.getSettingValue(SettingsData.SETTING_Z3950_PROXY_ADDRESS);
        if (!z3950_proxy) throw new Exception('Unable to query Z39.50, no proxy configured');
        String z3950_server = settings.getSettingValue(SettingsData.SETTING_Z3950_SERVER_ADDRESS);
        if (!z3950_server) throw new Exception('Unable to query Z39.50, no server configured');

        return executeQuery(z3950_server, z3950_proxy, query, max, schema, holdingLogDetails);
    }

    def executeQuery(String z3950_server, String z3950_proxy, String query, int max, String schema,
                     IHoldingLogDetails holdingLogDetails, String username=null, String password=null) {

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

            if (username) {
                request.uri.query['x-username'] = username;
            }

            if (password) {
                request.uri.query['x-password'] = password;
            }

            holdingLogDetails.newSearch(request.uri?.toURI().toString());
            log.debug("Querying z server with URL ${request.uri?.toURI().toString()}");
        }

        log.debug("Got Z3950 response: ${XmlUtil.serialize(z_response)}");
        holdingLogDetails.numberOfRecords(z_response?.numberOfRecords?.toLong());
        holdingLogDetails.searchRequest(z_response?.echoedSearchRetrieveRequest);
        return z_response;
    }
}
