databaseChangeLog = {
    changeSet(author: "EddiTim", id: "20240508-1000-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_zfl_identifiers", type: "TEXT")
        }
    }
}
