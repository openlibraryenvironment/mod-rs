databaseChangeLog = {
    changeSet(author: "jskomorowski", id: "20240820-1200-001") {
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

    changeSet(author: "jskomorowski", id: '20250909-1500-001') {
        createIndex(indexName: "idx_patron_request_notification_unread", tableName: "patron_request_notification") {
            column(name: "prn_patron_request_fk")
            column(name: "prn_seen")
            column(name: "prn_is_sender")
        }

        createIndex(indexName: "idx_patron_request_state_type", tableName: "patron_request") {
            column(name: "pr_state_fk")
            column(name: "pr_is_requester")
        }

        createIndex(indexName: "idx_patron_request_date_created_desc", tableName: "patron_request") {
            column(name: "pr_date_created", descending: true)
        }

        createIndex(indexName: "idx_patron_request_hrid", tableName: "patron_request") {
            column(name: "pr_hrid")
        }
    }
}
