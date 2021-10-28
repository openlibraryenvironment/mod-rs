databaseChangeLog = {
  // Set up completed boolean on patron request
  changeSet(author: "efreestone (manual)", id: "2021-10-14-1428-001") {
    addColumn(tableName:"status") {
      column(name: "st_terminal", type: "BOOLEAN")
    }
  }

  // Set completed to true where status is one of our terminal states
  changeSet(author: "efreestone (manual)", id: "2021-10-14-1428-002") {
    grailsChange {
      change {
        sql.execute("""
          UPDATE ${database.defaultSchemaName}.status AS st
          SET st_terminal = true
          WHERE st_code IN ('RES_COMPLETE', 'RES_CANCELLED', 'RES_NOT_SUPPLIED', 'REQ_CANCELLED', 'REQ_END_OF_ROTA', 'REQ_FILLED_LOCALLY', 'REQ_REQUEST_COMPLETE')
        """.toString())
      }
    }

    grailsChange {
      change {
         sql.execute("""
          UPDATE ${database.defaultSchemaName}.status AS st
          SET st_terminal = false
          WHERE st_code NOT IN ('RES_COMPLETE', 'RES_CANCELLED', 'RES_NOT_SUPPLIED', 'REQ_CANCELLED', 'REQ_END_OF_ROTA', 'REQ_FILLED_LOCALLY', 'REQ_REQUEST_COMPLETE')
        """.toString())
      }
    }
  }

  changeSet(author: "efreestone (manual)", id: "2021-10-28-1025-001") {
    grailsChange {
      change {
        sql.execute("""
          UPDATE ${database.defaultSchemaName}.status AS st
          SET st_terminal = true
          WHERE st_code = 'RES_UNFILLED'
        """.toString())
      }
    }
  }

}
