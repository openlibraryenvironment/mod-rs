databaseChangeLog = {
  // Remove unnecessary flag, just use state code instead
    changeSet(author: "ethanfreestone (manual)", id: "2021-08-12-1205-001") {
      dropColumn(tableName: "patron_request", columnName: "pr_requester_requested_cancellation")
  }
}
