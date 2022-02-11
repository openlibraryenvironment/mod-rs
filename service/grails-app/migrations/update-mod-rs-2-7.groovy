databaseChangeLog = {

  changeSet(author: "ibbo (generated)", id: "202202111355-001") {
    addColumn(tableName: "patron") {
      column(name: "pat_user_profile", type: "VARCHAR(255)")
    }
  }

}

