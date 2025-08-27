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

    changeSet(author: "jskomorowski", id: '20250828-1000-001') {
        createIndex(indexName: "idx_patron_request_audit_patron_request_fk", tableName: "patron_request_audit") {
            column(name: "pra_patron_request_fk")
        }

        createIndex(indexName: "idx_patron_request_rota_patron_request_fk", tableName: "patron_request_rota") {
            column(name: "prr_patron_request_fk")
        }
    }
}
