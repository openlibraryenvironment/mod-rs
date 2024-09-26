databaseChangeLog = {
    changeSet(author: "janis (manual)", id: "20240815-1111-001") {
        addColumn(tableName: "request_volume") {
            column(name: "rv_call_number", type: "VARCHAR(255)")
        }
    }
}