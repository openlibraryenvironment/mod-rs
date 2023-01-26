package mod.rs

import org.olf.okapi.modules.directory.DirectoryEntry;

import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api

/**
 * Access to InternalContact resources
 */
@Slf4j
@CurrentTenant
@Api(value = "/rs/directoryEntry", tags = ["Directory Entry Controller"], description = "API for all things to do with directory entries")
class DirectoryEntryController extends OkapiTenantAwareSwaggerController<DirectoryEntry>  {

  DirectoryEntryController() {
    super(DirectoryEntry)
  }

}