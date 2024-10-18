databaseChangeLog = {
    changeSet(author: "janis (manual)", id: "20240815-1111-001") {
        addColumn(tableName: "request_volume") {
            column(name: "rv_call_number", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "janis (manual)", id: "20241018-1111-001") {
        modifyDataType(tableName: "patron_request", columnName: "pr_selected_item_barcode", newDataType: "VARCHAR")
    }
}