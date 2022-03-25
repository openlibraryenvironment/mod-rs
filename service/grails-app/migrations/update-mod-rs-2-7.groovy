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

  changeSet(author: "ianibbo (manual)", id: "2022-03-10-0830-001") {
    createTable(tableName: "host_lms_shelving_loc") {
      column(name: "hlsl_id", type: "VARCHAR(36)") { constraints(nullable: "false") }
      column(name: "hlsl_version", type: "BIGINT")
      column(name: "hlsl_code", type: "VARCHAR(255)") { constraints(nullable: "false") }
      column(name: "hlsl_name", type: "VARCHAR(255)")
      column(name: "hlsl_date_created", type: "timestamp")
      column(name: "hlsl_last_updated", type: "timestamp")
      column(name: "hlsl_supply_preference", type: "BIGINT")
    }
  }

  changeSet(author: "ianibbo (manual)", id: "2022-03-10-0845-001") {
    createTable(tableName: "shelving_loc_site") {
      column(name: "sls_id", type: "VARCHAR(36)") { constraints(nullable: "false") }
      column(name: "sls_version", type: "BIGINT")
      column(name: "sls_shelving_loc_fk", type: "VARCHAR(36)") { constraints(nullable: "false") }
      column(name: "sls_location_fk", type: "VARCHAR(36)") { constraints(nullable: "false") }
      column(name: "sls_date_created", type: "timestamp")
      column(name: "sls_last_updated", type: "timestamp")
      column(name: "sls_supply_preference", type: "BIGINT")
    }
  }

  changeSet(author: "ianibbo (manual)", id: "2022-03-11-0845-001") {
    createTable(tableName: "host_lms_patron_profile") {
      column(name: "hlpp_id", type: "VARCHAR(36)") { constraints(nullable: "false") }
      column(name: "hlpp_version", type: "BIGINT")
      column(name: "hlpp_code", type: "VARCHAR(255)") { constraints(nullable: "false") }
      column(name: "hlpp_name", type: "VARCHAR(255)")
      column(name: "hlpp_date_created", type: "timestamp")
      column(name: "hlpp_last_updated", type: "timestamp")
    }
  }

    changeSet(author: "cwoodfield", id: "202203111200-001") {
        // Adding column hlpp_can_create_requests to host_lms_patron_profile
        addColumn(tableName: "host_lms_patron_profile") {
            column(name: "hlpp_can_create_requests", type: "BOOLEAN")
        }
    }

    changeSet(author: "cwoodfield", id: "202203141200-001") {
        // Make the field ne_patron_request_fk nullable, so we can make the table notice_event table more generic
        dropNotNullConstraint(columnDataType: "VARCHAR(36)", columnName: "ne_patron_request_fk", tableName: "notice_event")

        // Now add the column ne_json_data so we we can provide notices about anything
        addColumn(tableName: "notice_event") {
            column(name: "ne_json_data", type: "CLOB")
        }
    }

    changeSet(author: "cwoodfield", id: "202203181000-001") {

        // Adding column tmc_predefined_id to template_container
        addColumn(tableName: "template_container") {
            column(name: "tmc_predefined_id", type: "VARCHAR(64)")
        }

        // Adding column ltm_predefined_id to localized_template
        addColumn(tableName: "localized_template") {
            column(name: "ltm_predefined_id", type: "VARCHAR(64)")
        }

        // Adding column np_predefined_id to notice_policy
        addColumn(tableName: "notice_policy") {
            column(name: "np_predefined_id", type: "VARCHAR(64)")
        }
    }

    changeSet(author: "cwoodfield", id: "202203221200-001") {

        // Now having a separate predefined ids table rather than a predefined id column on the various tables
        createTable(tableName: "predefined_id") {
            column(name: "pi_id", type: "VARCHAR(36)") { constraints(nullable: "false") }
            column(name: "pi_references_id", type: "VARCHAR(36)") { constraints(nullable: "false") }
            column(name: "pi_version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }

        // So drop the predefinedId columns created previously
        // Could remove the previous changeset, and not have the drop but this keeps developer databases clean
        dropColumn(columnName: "tmc_predefined_id", tableName: "template_container")
        dropColumn(columnName: "ltm_predefined_id", tableName: "localized_template")
        dropColumn(columnName: "np_predefined_id", tableName: "notice_policy")
    }

    changeSet(author: "cwoodfield", id: "202203231700-001") {

        // Adding column hlpp_hidden to host_lms_patron_profile
        addColumn(tableName: "host_lms_patron_profile") {
            column(name: "hlpp_hidden", type: "BOOLEAN")
        }

        // Adding column hlsl_hidden to host_lms_shelving_loc
        addColumn(tableName: "host_lms_shelving_loc") {
            column(name: "hlsl_hidden", type: "BOOLEAN")
        }

        // Adding column hll_hidden to host_lms_location
        addColumn(tableName: "host_lms_location") {
            column(name: "hll_hidden", type: "BOOLEAN")
        }
    }
}
