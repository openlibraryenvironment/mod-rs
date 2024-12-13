databaseChangeLog = {
    changeSet(author: "janis (manual)", id: "20240815-1111-001") {
        addColumn(tableName: "request_volume") {
            column(name: "rv_call_number", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "janis (manual)", id: "20241018-1111-001") {
        modifyDataType(tableName: "patron_request", columnName: "pr_selected_item_barcode", newDataType: "VARCHAR")
    }

    changeSet(author: "knordstrom", id: '20241211-1514-001') {
        addColumn(tableName: "patron_request") {
            column(name: "pr_maximum_costs_value", type: "DECIMAL(10,2)")
        }

        addColumn(tableName: "patron_request") {
            column(name: "pr_maximum_costs_code_fk", type: "VARCHAR(36)")
        }

        addForeignKeyConstraint(baseColumnNames: "pr_maximum_costs_code_fk", baseTableName: "patron_request", constraintName: "FK_pr_maximum_costs_code", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")

        addColumn(tableName: "patron_request") {
            column(name: "pr_service_level_fk", type: "VARCHAR(36)")
        }

        addForeignKeyConstraint(baseColumnNames: "pr_service_level_fk", baseTableName: "patron_request", constraintName: "FK_pr_service_level", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")

    }
}