databaseChangeLog = {
    changeSet(author: "jskomorowski", id: "20230306-1245-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_item_format_fk", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "pr_item_format_fk", baseTableName: "patron_request", constraintName: "FK_pr_item_format", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }
}
