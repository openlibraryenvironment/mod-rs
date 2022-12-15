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
}
