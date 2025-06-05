package org.olf.rs

import com.k_int.okapi.OkapiHeaders
import com.k_int.okapi.OkapiTenantResolver
import grails.gorm.multitenancy.Tenants
import groovy.util.logging.Slf4j
import groovyx.net.http.ChainedHttpConfig
import groovyx.net.http.HttpBuilder
import groovyx.net.http.UriBuilder
import groovyx.net.http.FromServer
import groovyx.net.http.HttpConfig
import groovyx.net.http.NativeHandlers
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpServletRequest

import static groovyx.net.http.HttpBuilder.configure
import static groovyx.net.http.ContentTypes.JSON


@Slf4j
class SimpleOkapiService {
    private static final List<String> EXTRA_JSON_TYPES = [
            'application/vnd.api+json'
    ]

    // Collect this for a full list of the supplied types plus our own. So it can be easily referenced elsewhere.
    public static final List<String> JSON_TYPES = JSON.collect { "${it}" } + EXTRA_JSON_TYPES

    private final HttpServletRequest getRequestObject() {
        GrailsWebRequest gwr = (GrailsWebRequest)RequestContextHolder.requestAttributes;
        if (!gwr) {
            log.debug "No request present.";
        }

        return gwr?.currentRequest;
    }

    public def get(final String uri, final Map params = null) {
        String tenantId = OkapiTenantResolver.schemaNameToTenantId(Tenants.currentId().toString());
        HttpServletRequest requestObject = getRequestObject();
        String okapiURL = requestObject?.getHeader(OkapiHeaders.URL);
        String token = requestObject?.getHeader(OkapiHeaders.TOKEN);

        HttpBuilder client = configure {
            request.uri = uri; //Path to Okapi module
            if (okapiURL) {
                final UriBuilder builder = UriBuilder.root().setFull(okapiURL);
                request.uri.scheme = builder.scheme;
                request.uri.host = builder.host;
                request.uri.port = builder.port;
            }
            request.uri.query = params;
            if (token) {
                request.headers[OkapiHeaders.TOKEN] = token;
            }
            request.headers[OkapiHeaders.TENANT] = tenantId;
            request.contentType = JSON[0]

            // Register vnd.api+json as parsable json.
            response.parser(EXTRA_JSON_TYPES) { HttpConfig cfg, FromServer fs ->
                NativeHandlers.Parsers.json(cfg as ChainedHttpConfig, fs)
            }
        }

        return client.get()
    }
}
