databaseChangeLog = {
  // Remove unnecessary flag, just use state code instead
    changeSet(author: "ethanfreestone (manual)", id: "2021-08-12-1205-001") {
      dropColumn(tableName: "patron_request", columnName: "pr_requester_requested_cancellation")
  }

  changeSet(author: "jskomorowski (manual)", id: "2021-08-19-1500-001") {
    addColumn(tableName: "patron_request") {
      column(name:'pr_pickup_location_slug', type: "VARCHAR(255)")
    }
  }
}
