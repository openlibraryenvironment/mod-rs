package mod.rs;

import org.olf.rs.EmailService;
import org.olf.rs.files.FileFetchResult
import org.olf.rs.files.ReportCreateUpdateResult
import org.olf.rs.reporting.Report
import org.olf.rs.reporting.ReportService;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Slf4j
@CurrentTenant
@Api(value = "/rs/report", tags = ["Report Controller"], description = "Report Api")
class ReportController extends OkapiTenantAwareController<Report>  {

    static private final String pullSlipDefaultReport = "reports/patronRequests/PullSlip.jrxml";

	ReportController() {
		super(Report)
	}

    EmailService emailService
    ReportService reportService;

    @ApiOperation(
        value = "Create / Update a report",
        nickname = "createUpdate",
        httpMethod = "POST",
        consumes = "multipart/form-data",
        produces = "application/json"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "id",
            paramType = "form",
            allowMultiple = false,
            required = false,
            value = "The id of the report if updating, leave blank for a new report",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "name",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "A user friendly name for this report",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "description",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "The description for this report",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "domain",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "The domain this report is for (eg. patronRequest)",
            dataType = "string",
            allowableValues = "patronRequest"
        ),
        @ApiImplicitParam(
            name = "contentType",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "The content type of the generated report)",
            dataType = "string",
            allowableValues = "application/pdf"
        ),
        @ApiImplicitParam(
            name = "filename",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "The filename to give the generated report",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "isSingleRecord",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "Does this report just take a single identifier",
            dataType = "boolean",
            defaultValue = "false"
        ),
        @ApiImplicitParam(
            name = "file",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "The file to be uploaded",
            dataType = "file"
        )
    ])
    def createUpdate() {
        // Need to convert the parameter isSingleRecord to a boolean first
        boolean isSingleRecord = params.isSingleRecord ? params.isSingleRecord.toBoolean() : false;

        // Just pass it onto the service to do the work
        ReportCreateUpdateResult result = reportService.createUpdate(
            params.name,
            params.description,
            params.domain,
            isSingleRecord,
            params.contentType,
            params.filename,
            params.file,
            params.id
        );

        // Render the result as json
        render result as JSON
    }

    @ApiOperation(
        value = "Executes the specied report",
        nickname = "execute",
        httpMethod = "GET",
        produces = "application/pdf,application/json"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success", response = byte.class)
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "reportId",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "The id of the report to be run, if left null will use the default pull slip",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "id",
            paramType = "query",
            allowMultiple = true,
            required = true,
            value = "The id(s) that the picklist is to be printed for",
            dataType = "string"
        )
    ])
    def execute() {
        List ids;
        if (params.id == null) {
            ids = new ArrayList();
        } else if (params.id instanceof String) {
            ids = new ArrayList();
            ids.add(params.id);
        } else {
            // it must be an array
            ids = params.id;
        }

        try {
            // Attempt to execute the report
            FileFetchResult fetchResult = reportService.generateReport(request.getHeader("X-Okapi-Tenant"), params.reportId, ids, pullSlipDefaultReport);

            // Did we manage to generate the report
            if (fetchResult.inputStream == null) {
                // we had an error
                Map renderResult = [ error: fetchResult.error ];
                render renderResult as JSON, status: 404, contentType: "application/json";
            } else {
                // Present the pdf
                // This header needs adding so that swagger allows you to download the file
                response.addHeader("Content-Disposition", ": attachment; filename=" + fetchResult.filename);
                render file: fetchResult.inputStream, contentType: fetchResult.contentType, status: 200
            }
        } catch (Exception e) {
            String message = "Exception thrown generating report";
            log.error(message, e);
            Map renderResult = [ error: (message + ", exception: " + e.getMessage()) ];
            render renderResult as JSON, status: 404, contentType: "application/json";
        }
/* Commented this out for the time being as it will need to be reworked and live somewhere else
        // In order to test this ensure you have configured mod-email
        // also need to go through okapi, rather than local otherwise it will not find mod-email
        File file = new File(outputFilename);
        byte[] binaryContent = file.bytes;
        String encoded = binaryContent.encodeBase64().toString();
        Map emailParamaters = [
            notificationId: '1',
            to: 'chaswoodfield@gmail.com',
            header: 'Has the pull slip attached',
            body: 'Will it get through',
            outputFormat: 'text/plain',
            attachments: [
                [
                    contentType: 'application/pdf',
                    name: 'Pull Slip',
                    description: 'This is a Pull Slip',
                    data: encoded,
                    disposition: 'base64'
                ]
            ]
        ];

        // Send an email with the pull slip in
        emailService.sendEmail(emailParamaters);
*/
    }
}
