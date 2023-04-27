databaseChangeLog = {
  changeSet(author: "Chas (generated)", id: "1679583267378") {
      addColumn(tableName: "action_event") {
          column(defaultValueComputed: "false", name: "ae_is_available_for_bulk", type: "boolean") {
              constraints(nullable: "false")
          }
      }
  }
    changeSet(author: "jskomorowski", id: "20230331-1130-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_delivery_method_fk", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "pr_delivery_method_fk", baseTableName: "patron_request", constraintName: "FK_pr_delivery_method", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }
    changeSet(author: "jskomorowski", id: "20230427-1130-001") {
      addColumn(tableName: "patron_request") {
        column(name: "pr_pickup_url", type: "text")
      }
    }
}
