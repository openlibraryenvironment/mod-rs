databaseChangeLog = {
  changeSet(author: "efreestone (manual)", id: "20220407-1111-001") {
    addColumn(tableName: "request_volume") {
      column(name: "rv_temporary_item_barcode", type: "VARCHAR(52)")
    }
  }

    changeSet(author: "Chas (generated)", id: "1652284970385-1") {
        addColumn(tableName: "action_event_result") {
            column(name: "aer_override_save_state", type: "varchar(36)")
        }

        addForeignKeyConstraint(baseColumnNames: "aer_override_save_state", baseTableName: "action_event_result", constraintName: "FKrb7qtrmsg3hxu3ei59qo0uqel", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")
    }

    changeSet(author: "Chas (generated)", id: "1652704268056-1") {
        addColumn(tableName: "status") {
            column(name: "st_stage", type: "varchar(255)")
        }
    }

    changeSet(author: "Chas (generated)", id: "1652795570679-1") {
        addColumn(tableName: "action_event_result") {
            column(name: "aer_from_state", type: "varchar(36)")
        }

        addForeignKeyConstraint(baseColumnNames: "aer_from_state", baseTableName: "action_event_result", constraintName: "FKde3d1w46dlb59n70g1uxsutma", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")
    }
}
