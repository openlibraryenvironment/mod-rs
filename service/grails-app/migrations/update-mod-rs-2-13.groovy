databaseChangeLog = {
  changeSet(author: "Chas (generated)", id: "1679583267378") {
      addColumn(tableName: "action_event") {
          column(defaultValueComputed: "false", name: "ae_is_available_for_bulk", type: "boolean") {
              constraints(nullable: "false")
          }
      }
  }
}
