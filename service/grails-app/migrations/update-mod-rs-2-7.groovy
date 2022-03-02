databaseChangeLog = {

  changeSet(author: "ibbo (generated)", id: "202202111355-001") {
    addColumn(tableName: "patron") {
      column(name: "pat_user_profile", type: "VARCHAR(255)")
    }
  }

	changeSet(author: "cwoodfield", id: "20220214120000-001") {
		// We need to eradicate the duplicates
		grailsChange {
			change {
				// Iterating through each row in table.
				sql.eachRow("""SELECT s.sym_authority_fk authority, s.sym_symbol symbol
							   FROM ${database.defaultSchemaName}.symbol s
							   group by s.sym_authority_fk, s.sym_symbol
							   having count(*) > 1
							   ORDER BY authority, symbol""".toString(), { duplicateData ->
				    // We need to run through this and make them non unique
					sql.eachRow("""SELECT sym_id id, de_slug slug, sym_owner_fk directoryEntryId
								   FROM ${database.defaultSchemaName}.symbol s, ${database.defaultSchemaName}.directory_entry de
								   where sym_authority_fk = '${duplicateData.authority}' and
									     sym_symbol = '${duplicateData.symbol}' and
										 de.de_id = s.sym_owner_fk""".toString(), { duplicateRow ->

						// Now update the symbol on the record to make it unique	
						// This will not work if a directory entry has duplicate symbols for the same authority
						// I did have it working off a running number, but since the same migration is running against mod-rs,
						// needed to ensure that they would be changed to the same value on both modules 			  
						sql.execute("""UPDATE ${database.defaultSchemaName}.symbol
									   SET sym_symbol = '${duplicateData.symbol}-DUPLICATE-${duplicateRow.slug.toUpperCase()}'
									   WHERE sym_id = '${duplicateRow.id}'""".toString());

						// We need to update the lastUpdateDate on the directory ebtry and that of its parents
						Long updateTime = System.currentTimeMillis();
						def row = duplicateRow;
						while (row.directoryEntryId != null) {
							// Update the entry so that the last de_published_last_update field is updated if it is not null
							sql.execute("""UPDATE ${database.defaultSchemaName}.directory_entry
									   	   SET de_published_last_update = ${updateTime}
										   WHERE de_id = '${row.directoryEntryId}' and
												 de_published_last_update is not null""".toString());
											 
							// Now if the direct entry has a parent we need to update that as well
							row = sql.firstRow("""select de_parent directoryEntryId
														 from ${database.defaultSchemaName}.directory_entry
														 WHERE de_id = '${row.directoryEntryId}'""".toString());
						}
					});
				});
			}
		}

		// Now we have no duplicates, we can add the unique index
		addUniqueConstraint(columnNames: "sym_authority_fk, sym_symbol", tableName: "symbol")
	}
	
	changeSet(author: "cwoodfield", id: "202202211400-001") {

		// Adding column tr_code
		addColumn(tableName: "timer") {
            column(name: "tr_code", type: "VARCHAR(255)")
		}

		// Adding column tr_next_exec
		addColumn(tableName: "timer") {
            column(name: "tr_next_exec", type: "BIGINT")
		}
		
		// now set the next execution field with the last execution field
		grailsChange {
			change {
				// Update the entry so that the last de_published_last_update field is updated if it is not null
				sql.execute("""UPDATE ${database.defaultSchemaName}.timer
							   SET tr_next_exec = tr_last_exec""".toString());
			}
		}
	}

  changeSet(author: "ianibbo (manual)", id: "202201261047-001") {
    addColumn (tableName: "custom_property_definition" ) {
      column(name: "pd_ctx", type: "VARCHAR(255)")
    }
  }


}

