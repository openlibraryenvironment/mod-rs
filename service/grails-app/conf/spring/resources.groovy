// Place your Spring DSL code here
import grails.util.Environment
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import grails.util.Holders

import org.olf.rs.*;

beans = {
  dataSource(HikariDataSource) { bean ->
      def ds = Holders.config.dataSource

      def hp = new Properties()
      hp.username = ds.username
      hp.password = ds.password
      hp.jdbcUrl = ds.url
      hp.driverClassName = ds.driverClassName

      hp.connectionTimeout = ds.hikariConnectionTimeout
      hp.maximumPoolSize = ds.hikariMaximumPoolSize
      hp.maxLifetime = ds.hikariMaxLifetime
      hp.idleTimeout = ds.hikariIdleTimeout
      hp.minimumIdle = ds.hikariMinimumIdle
      hp.connectionTestQuery = "SELECT 1"

      HikariConfig hc = new HikariConfig(hp)
      bean.constructorArgs = [hc]
  }

  switch(Environment.current) {
    case Environment.TEST:
    //  sharedIndexService(MockSharedIndexImpl)
    //   hostLMSService(MockHostLMSServiceImpl)
      emailService(MockEmailServiceImpl)
      break
    default:
      emailService(FolioEmailServiceImpl)
      break
  }
}
