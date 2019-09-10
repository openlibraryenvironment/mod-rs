databaseChangeLog = {
  changeSet(author: "grails-okapi-module", id: "1") {
    grailsChange {
      change {
        // grailsChange gives us an sql variable which inherits the current connection, and hence should
        // get the schema
        // sql.execute seems to get a bit confused when passed a GString. Work it out before
        sql.execute('CREATE EXTENSION IF NOT EXISTS pg_trgm;');
      }
    }
  }
}
