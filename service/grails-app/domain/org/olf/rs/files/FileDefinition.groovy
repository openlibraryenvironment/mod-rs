package org.olf.rs.files

import com.k_int.web.toolkit.files.FileUpload;

import grails.gorm.MultiTenant;

/**
 * Holds the details for a file that has been uploaded
 * @author Chas
 *
 */
class FileDefinition implements MultiTenant<FileDefinition> {

    /** The id for the file definition */
    String id;

    /** The date the file definition was created - maintained by the framework */
    Date dateCreated;

    /** The date the file definition was last updated - maintained by the framework */
    Date lastUpdated;

    /** The type of file that has been uploaded */
    FileType fileType;

    /** A description of this file definition */
    String description;

    /** The uploaded file */
    FileUpload fileUpload;

    static constraints = {
        dateCreated (nullable: true)
        lastUpdated (nullable: true)
           fileType (nullable: false,)
        description (nullable: false, blank: false)
         fileUpload (nullable: false)
    }

    static mapping = {
                 id column : 'fd_id', generator: 'uuid2', length:36
            version column : 'fd_version'
        dateCreated column : 'fd_date_created'
        lastUpdated column : 'fd_last_updated'
           fileType column : 'fd_file_type', length: 32
        description column : 'fd_description', length: 512
         fileUpload column : 'fd_file_upload'
    }
}
