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

    changeSet(author: "janis", id: '20250508-1514-001') {
        addColumn(tableName: "patron_request_loan_condition") {
            column(name: "prlc_sup_inst_symbol", type: "VARCHAR")
        }
    }

    changeSet(author: "jskomorowski", id: '20250523-1200-001') {
        addColumn(tableName: "patron_request") {
            column(name: "pr_delivery_address", type: "TEXT")
        }
        addColumn(tableName: "patron_request") {
            column(name: "pr_return_address", type: "TEXT")
        }
    }

    changeSet(author: "jskomorowski", id: '20250613-1200-001') {
        addColumn(tableName: "patron_request_loan_condition") {
            column(name: "prlc_cost", type: "DECIMAL(10,2)")
            column(name: "prlc_cost_currency_fk", type: "VARCHAR(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "prlc_cost_currency_fk", baseTableName: "patron_request_loan_condition", constraintName: "FK_prlc_cost_currency", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }
}