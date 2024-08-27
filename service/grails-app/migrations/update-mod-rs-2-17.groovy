databaseChangeLog = {
    changeSet(author: "EddiTim", id: "20240324-1000-001") {
        addColumn(tableName: "available_action") {
            column(name: "aa_is_primary", type: "BOOLEAN")
        }
        addColumn(tableName: "available_action") {
            column(name: "aa_primary_only", type: "BOOLEAN")
        }
    }

    changeSet(author: "jskomorowski", id: "20240821-1200-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_local_note", type: "TEXT")
        }
    }
}
