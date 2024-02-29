databaseChangeLog = {
    changeSet(author: "jskomorowski", id: "20240226-1000-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_pages_requested", type: "varchar(255)")
        }
    }
}
