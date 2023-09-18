databaseChangeLog = {
    changeSet(author: "jskomorowski", id: "20230915-1000-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_preceded_by_fk", type: "varchar(36)")
            column(name: "pr_succeeded_by_fk", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "pr_preceded_by_fk", baseTableName: "patron_request", constraintName: "FK_pr_preceded_by", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
        addForeignKeyConstraint(baseColumnNames: "pr_succeeded_by_fk", baseTableName: "patron_request", constraintName: "FK_pr_succeeded_by", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }
}
