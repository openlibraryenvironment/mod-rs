databaseChangeLog = {
    changeSet(author: "jskomorowski", id: "20240820-1200-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_local_note", type: "TEXT")
        }
    }
}
