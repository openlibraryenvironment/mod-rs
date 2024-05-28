databaseChangeLog = {
    changeSet(author: "knordstrom", id: '20240502-1215-001') {
        addColumn(tableName: 'patron_request') {
            column(name: 'pr_external_hold_request_id', type: 'varchar(255)')
        }
    }
}