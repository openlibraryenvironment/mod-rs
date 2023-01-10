package mod.rs;

import org.olf.rs.files.FileDefinition;
import org.olf.rs.files.FileDefinitionCreateResult;
import org.olf.rs.files.FileFetchResult;
import org.olf.rs.files.FileService;
import org.olf.rs.files.FileType;

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
@Api(value = "/rs/fileDefinition", tags = ["FileDefinition Controller"], description = "FileDefinition Api")
class FileDefinitionController extends OkapiTenantAwareController<FileDefinition>  {

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
    }
}
