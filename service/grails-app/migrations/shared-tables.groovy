databaseChangeLog = {

  changeSet(author: "Ian (manual)", id: "20190328-00001") {

    createTable(tableName: 'tenant_symbol_mapping') {

      column(name: 'tsm_id', type: 'VARCHAR(36)') {
        constraints(nullable: 'false')
      }

      column(name: 'tsm_version', type: 'BIGINT') {
        constraints(nullable: 'false')
      }

      column(name: 'tsm_symbol', type: 'VARCHAR(256)')

      column(name: 'tsm_tenant', type: 'VARCHAR(256)')
    }
  }
}

