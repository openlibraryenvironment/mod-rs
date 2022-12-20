package org.olf.rs.files;

import org.springframework.web.multipart.MultipartFile;

import com.k_int.web.toolkit.files.FileUpload;
import com.k_int.web.toolkit.files.FileUploadService;

/**
 * Service to handle the various things that happen around file storage
 */
public class FileService {

    FileUploadService fileUploadService;

    /**
     * Uploads the file to storage and creates a FileDefinition record
     * @param fileType The type of file being uploaded
     * @param description A description of the file
     * @param multipartFile The file to be stored
     * @return A FileDefinitionCreateResult record which will contain either an error or the FileDefinition that was created
     */
    public FileDefinitionCreateResult create(FileType fileType, String description, MultipartFile multipartFile) {
        FileDefinitionCreateResult result = new FileDefinitionCreateResult();
        if (fileType && description && multipartFile) {
            // First of all let us try and upload the file
            FileUpload fileUpload = fileUploadService.save(multipartFile);
            if (fileUpload == null) {
                // Failed for some reason
                result.error = "null returned by fileUploadService.save";
            } else {
                // We have manage to upload the file so create a ourselves a FileDefinition record
                result.fileDefinition = new FileDefinition();
                result.fileDefinition.fileUpload = fileUpload;
                result.fileDefinition.fileType = fileType;
                result.fileDefinition.description = description;
                result.fileDefinition.save(flush:true, failOnError:true);
            }
        } else {
            // Can't do anything if we havn't been supplied one of the parameters
            result.error = "One of fileType, description or multipartFile has not been supplied";
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Fetches an InputStram for the specified file id
     * @param fileDefinitionId The id of the file
     * @return A FileFetchResult object that contains either the error if any occured or the InputStream if successful
     */
    public FileFetchResult fetch(String fileDefinitionId) {
        FileFetchResult result = new FileFetchResult();
        if (fileDefinitionId) {
            FileDefinition fileDefinition = FileDefinition.get(fileDefinitionId);
            result = fetch(fileDefinition);
        } else {
            // Cannot do anything without a file identifier
            result.error = "No file identifier supplied";
        }

        // Let the caller know the result
        return(result);
    }

    /**
     * Fetches an InputStram for the specified file definition
     * @param fileDefinition The file definition of the file
     * @return A FileFetchResult object that contains either the error if any occurred or the InputStream if successful
     */
    public FileFetchResult fetch(FileDefinition fileDefinition) {
        FileFetchResult result = new FileFetchResult();
        if (fileDefinition == null) {
            // Cannot do anything without a file definition
            result.error = "No file definition supplied";
        } else {
            // The file upload object exists, let us see if we can get hold of the input stream
            result.inputStream = fileUploadService.getInputStreamFor(fileDefinition.fileUpload.fileObject);
            if (result.inputStream == null) {
                // Failed to get the input stream
                result.error = "Unable to get the input stream for file with id: ${fileDefinition.id}";
            } else {
                // Success so set the content type and filename on the result
                result.contentType = fileDefinition.fileUpload.fileContentType;
                result.filename = fileDefinition.fileUpload.fileName;
            }
        }

        // Let the caller know the result
        return(result);
    }
}
