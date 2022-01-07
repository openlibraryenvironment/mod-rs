databaseChangeLog = {

    changeSet(author: "ibbo (generated)", id: "202201051420-001") {
        createTable(tableName: "status_tag") {
            column(name: "status_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

  changeSet(author: "jskomorowski (manual)", id: "2022-01-07-1655-001") {
    addColumn(tableName: "patron_request_audit") {
      column(name: "pra_user", type: "VARCHAR(36)")
    }
  }

}
