package mod.rs

import com.k_int.okapi.OkapiTenantAdminService

class BootStrap {

  def grailsApplication
  def housekeepingService
  DataSource dataSource
  OkapiTenantAdminService okapiTenantAdminService
  
  def init = { servletContext ->

    log.info("${grailsApplication.getMetadata().getApplicationName()}  (${grailsApplication.config?.info?.app?.version}) initialising");
    log.info("          build number -> ${grailsApplication.metadata['build.number']}");
    log.info("        build revision -> ${grailsApplication.metadata['build.git.revision']}");
    log.info("          build branch -> ${grailsApplication.metadata['build.git.branch']}");
    log.info("          build commit -> ${grailsApplication.metadata['build.git.commit']}");
    log.info("            build time -> ${grailsApplication.metadata['build.time']}");
    log.info("            build host -> ${grailsApplication.metadata['build.host']}");
    log.info("         Base JDBC URL -> ${grailsApplication.config.dataSource.url}");
  }

    def hikariDataSource = dataSource?.unwrap(HikariDataSource)
    if (hikariDataSource) {
      log.info "HikariCP maximumPoolSize: ${hikariDataSource.maximumPoolSize}"
    } else {
      log.info "Could not determine HikariCP maximumPoolSize"
    }

  def destroy = {
  }
}
