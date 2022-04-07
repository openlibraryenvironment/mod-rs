databaseChangeLog = {
  changeSet(author: "efreestone (manual)", id: "20220407-1111-001") {
    addColumn(tableName: "request_volume") {
      column(name: "rv_temporary_item_barcode", type: "VARCHAR(52)")
    }
  }
}
