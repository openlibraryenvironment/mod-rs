databaseChangeLog = {
    changeSet(author: "Chas (generated)", id: "1669301859960") {
        createTable(tableName: "state_model_inherits_from") {
            column(name: "smif_state_model", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "state_model_inherits_fromPK")
            }

            column(name: "smif_inherited_state_model", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "state_model_inherits_fromPK")
            }

            column(name: "smif_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "99", name: "smif_priority", type: "INTEGER") {
                constraints(nullable: "false")
            }
        }
        
        addForeignKeyConstraint(baseColumnNames: "smif_inherited_state_model", baseTableName: "state_model_inherits_from", constraintName: "FK3inmmr32t3ecevgmad1oh1pqy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model", validate: "true")
        addForeignKeyConstraint(baseColumnNames: "smif_state_model", baseTableName: "state_model_inherits_from", constraintName: "FK6yp12hwcv6wpam9tet9yqxc49", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model", validate: "true")
    }
    
    changeSet(author: "Chas (generated)", id: "1670498957817") {
        // The columns for testing whether an action is available or not using groovy script
        addColumn(tableName: "available_action") {
            column(name: "aa_is_available_groovy", type: "varchar(512)")
        }
        
        addColumn(tableName: "action_event") {
            column(name: "ae_is_available_groovy", type: "varchar(512)")
        }
    }
    
    changeSet(author: "Chas (generated)", id: "1671182393817") {
        createTable(tableName: "file_definition") {
            column(name: "fd_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "file_definitionPK")
            }

            column(name: "fd_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fd_date_created", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "fd_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "fd_file_upload", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "fd_file_type", type: "VARCHAR(32)") {
                constraints(nullable: "false")
            }

            column(name: "fd_description", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }
        }
        
        // Finally the foreign key constraint
        addForeignKeyConstraint(baseColumnNames: "fd_file_upload", baseTableName: "file_definition", constraintName: "FKhac3qrb8qhvqk6aenuw5lxlxt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "fu_id", referencedTableName: "file_upload", validate: "true")
    }
    
    changeSet(author: "Chas (generated)", id: "1671186962032-13") {
        
        // Delete all the records in the report table first, should only be 1
        grailsChange {
            change {
                sql.execute("""delete from ${database.defaultSchemaName}.report""".toString());
            }
        }

        addColumn(tableName: "report") {
            column(defaultValue: "application/pdf", name: "r_content_type", type: "varchar(64)") {
                constraints(nullable: "false")
            }
        }

        addDefaultValue(columnDataType: "varchar(64)", columnName: "r_filename", defaultValue: "report.pdf", tableName: "report")
        
        addColumn(tableName: "report") {
            column(name: "r_file_definition", type: "varchar(36)") {
                constraints(nullable: "false")
            }
        }

        // Add the foreign key constraint        
        addForeignKeyConstraint(baseColumnNames: "r_file_definition", baseTableName: "report", constraintName: "FK6f40ksfw3i43n5yjkpnk69nvj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "fd_id", referencedTableName: "file_definition", validate: "true")

        // Remove the 2 columns that are no longer used
        dropColumn(columnName: "r_report_definition", tableName: "report")
    }
    
    changeSet(author: "Chas (generated)", id: "1673022987400") {
        addColumn(tableName: "state_model") {
            column(name: "sm_pick_slip_printed_action", type: "varchar(36)")
        }
    }
}
