package org.olf

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import grails.testing.mixin.integration.Integration;
import grails.web.http.HttpHeaders;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class FileDefinitionSpec extends TestBase {

    // This method is declared in the HttpSpec
    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
    }

    def setupSpec() {
    }

    def setup() {
    }

    def cleanup() {
    }

    void "Set up test tenant "(tenantid, name) {
        when:"We post a new tenant request to the OKAPI controller"
            boolean response = setupTenant(tenantid, name);

      then:"The response is correct"
          assert(response);

      where:
          tenantid   | name
          TENANT_ONE | TENANT_ONE
    }

    void "Set up test tenants"(String tenantId, String name) {
        when:"We post a new tenant request to the OKAPI controller"
            boolean response = setupTenant(tenantId, name);

        then:"The response is correct"
            assert(response);

        where:
            tenantId   | name
            TENANT_ONE | TENANT_ONE
    }

    void "Configure Tenants for S3 storage"(String tenantId, String key, String value) {
        when:"We fetch the existing settings for ${tenantId}"
            List settings = changeSettings(tenantId, [ (key) : value ]);
            def newSetting = settings.find { setting -> setting.key.equals(key) };
            log.debug("set key: " + key + ", to value: " + value);
            log.debug("Setting: " + settings.toString());
            log.debug("new setting: " + newSetting.toString());

        then:"Tenant is configured"
            assert(newSetting != null);
            assert(newSetting.value.equals(value));

        where:
            tenantId   | key             | value
            TENANT_ONE | "S3SecretKey"   | "RESHARE_AGG_SECRET_KEY"
            TENANT_ONE | "S3Endpoint"    | "http://127.0.0.1:9010"
            TENANT_ONE | "S3AccessKey"   | "RESHARE_AGG_ACCESS_KEY"
            TENANT_ONE | "S3BucketName"  | "reshare-general"
            TENANT_ONE | "storageEngine" | "S3"

    }

    void "Upload_File"(String tenantId, String fileContents, String fileContentType, String filename) {
        when:"We upload a file"
            // Post to the upload end point
            // Build the http entity
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntityBuilder.addBinaryBody("file", fileContents.getBytes(), ContentType.create(fileContentType), filename);
            multipartEntityBuilder.addTextBody("fileType", "REPORT_DEFINITION");
            multipartEntityBuilder.addTextBody("description", "A simple test of file upload");
            HttpEntity httpEntity = multipartEntityBuilder.build();
            InputStream entityInputStream = httpEntity.getContent();
            String body = httpEntity.getContent().text;
            entityInputStream.close();

            // Now set the headers
            setHeaders([
                'X-Okapi-Tenant': tenantId,
                (HttpHeaders.ACCEPT) : 'application/json',
                (HttpHeaders.CONTENT_TYPE) : httpEntity.getContentType().value,
                (HttpHeaders.CONTENT_LENGTH) : httpEntity.getContentLength().toString()
            ]);
            def uploadResponse = doPost("${baseUrl}/rs/fileDefinition/fileUpload".toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(body);
            });

            // Save the uploaded text and id
            testctx.fileDefinition = uploadResponse;
            testctx.uploadedText = fileContents;

            log.debug("Response from posting attachment: " + uploadResponse.toString());


        then:"Check file has been uploaded correctly"
            assert(uploadResponse?.id != null);

        where:
            tenantId   | fileContents                                | fileContentType  | filename
            TENANT_ONE | "Will this manage to get uploaded properly" | "text/plain"     | "test.txt"
    }

    void "Download_File"(String tenantId, String ignore) {
        when:"We download a file"

            // If we successfully uploaded then attempt to download
            String dowloadedText = null;

            // Fetch from the download end point
            setHeaders([
                'X-Okapi-Tenant': tenantId,
                (HttpHeaders.ACCEPT) : 'application/octet-stream',
            ]);
            def downloadResponse = doGet("${baseUrl}rs/fileDefinition/fileDownload/${testctx.fileDefinition.id}");
            if (downloadResponse instanceof String) {
                // That is good we have the response we expected
                dowloadedText = downloadResponse;
            }
            log.debug("Download response: " + downloadResponse);

        then:"Check file has been uploaded correctly"
            assert(downloadResponse != null);
            assert(testctx.uploadedText.equals(dowloadedText));

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
