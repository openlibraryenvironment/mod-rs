package mod.rs;

import org.olf.rs.files.FileDefinition;
import org.olf.rs.files.FileDefinitionCreateResult;
import org.olf.rs.files.FileFetchResult;
import org.olf.rs.files.FileService;
import org.olf.rs.files.FileType;
import org.olf.rs.logging.ContextLogging;

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
@Api(value = "/rs/fileDefinition", tags = ["FileDefinition Controller"], description = "FileDefinition Api")
class FileDefinitionController extends OkapiTenantAwareSwaggerController<FileDefinition>  {

    private static final String RESOURCE_FILE_DEFINITION = FileDefinition.getSimpleName();

    /** The service that handles the storage and retrieval of files */
    FileService fileService;

  	FileDefinitionController() {
		super(FileDefinition)
	}

    @ApiOperation(
        value = "File upload",
        nickname = "fileUpload",
        httpMethod = "POST",
        consumes = "multipart/form-data",
        produces = "application/json"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "fileType",
            paramType = "form",
            allowMultiple = false,
            allowableValues = "LOGO,REPORT_DEFINITION,REPORT_OUTPUT",
            required = true,
            value = "The type of file being uploaded",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "description",
            paramType = "form",
            allowMultiple = false,
            required = true,
            value = "The description for this file",
            dataType = "string"
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
    def fileUpload() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, RESOURCE_FILE_DEFINITION);
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_FILE_UPLOAD);

        FileDefinitionCreateResult result = null;
        def file = params.file;
        if (params.fileType) {
            FileType fileType = convertToFileType(params.fileType);
            if (fileType == null) {
                result = new FileDefinitionCreateResult();
                result.error = "Unkown value \"${params.fileType}\" for fileType has been supplied";
            } else {
                FileDefinition.withTransaction { tstatus ->
                    result = fileService.create(fileType, params.description, file);
                }
            }
        } else {
            result = new FileDefinitionCreateResult();
            result.error = "File type must be supplied";
        }
        Map jsonResult = [ id : result.fileDefinition?.id, error : result.error ];
        render jsonResult as JSON

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     * Converts the supplied string to a FileType
     * @param fileTypeAsString The string yo be converted
     * @return The FileType the string represents or null if there is not an appropriate FileType for it to map onto
     */
    private FileType convertToFileType(String fileTypeAsString) {
        FileType fileType = null;
        try {
            // Convert it to the enum of FileType
            fileType = fileTypeAsString as FileType;
        } catch(Exception e) {
            // Do nothing as null will just be returned
        }

        return(fileType);
    }

    @ApiOperation(
        value = "File download",
        nickname = "fileDownload/{fileId}",
        httpMethod = "GET",
        produces = "application/octet"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "File not found"),
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "fileId",
            paramType = "path",
            allowMultiple = false,
            required = true,
            value = "The id of the file to be retrieved",
            dataType = "string"
        )
    ])
    def fileDownload() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, RESOURCE_FILE_DEFINITION);
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_FILE_DOWNLOAD);

        String fileId = params.fileId;
        FileFetchResult result = fileService.fetch(fileId);
        if (result.inputStream != null) {
            // Success so render the stream back
            render file: result.inputStream, contentType: result.contentType;
        } else {
            // Just render the error
            Map renderResult = [ error: result.error ];
            render renderResult as JSON, status: 404, contentType: "application/json";
        }

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }
}
