databaseChangeLog = {

    changeSet(author: "ibbo (generated)", id: "202201051420-001") {
        createTable(tableName: "status_tag") {
            column(name: "status_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

}
