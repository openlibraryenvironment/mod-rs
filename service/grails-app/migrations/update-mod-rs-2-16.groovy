databaseChangeLog = {
    changeSet(author: "jskomorowski", id: "20240226-1000-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_pages_requested", type: "varchar(255)")
        }
    }

    changeSet(author: "jskomorowski", id: "20240315-1030-001") {
        addColumn(tableName: "patron_request") {
            column(name:'pr_copyright_type_fk', type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "pr_copyright_type_fk", baseTableName: "patron_request", constraintName: "FK_pr_copyright_type", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "jskomorowski", id: "20240326-1700-001") {
        delete(tableName: 'refdata_value') {
            where("rdv_value='copy-non-returnable'")
        }
    }
}
