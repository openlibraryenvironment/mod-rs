databaseChangeLog = {

    changeSet(author: "Chas (generated)", id: "1649085483547-1") {
        // This change modification is the starting is in preparation for moving the  state changes to the database
        // New table action_event
        createTable(tableName: "action_event") {
            column(name: "ae_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "action_eventPK")
            }

            column(name: "ae_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ae_result_list", type: "VARCHAR(36)")

            column(name: "ae_is_action", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "ae_code", type: "VARCHAR(64)") {
                constraints(nullable: "false")
            }

            column(name: "ae_description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }

        // Unique constraints for action_event
        addUniqueConstraint(columnNames: "ae_code", constraintName: "UC_ACTION_EVENTAE_CODE_COL", tableName: "action_event")

        // New table action_event_result
        createTable(tableName: "action_event_result") {
            column(name: "aer_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "action_event_resultPK")
            }

            column(name: "aer_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "aer_code", type: "VARCHAR(64)") {
                constraints(nullable: "false")
            }

            column(name: "aer_status", type: "VARCHAR(36)")

            column(name: "aer_save_restore_state", type: "VARCHAR(36)")

            column(name: "aer_result", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "aer_description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "aer_next_action_event", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "aer_qualifier", type: "VARCHAR(64)")
        }

        // Unique constraints for action_event_result
        addUniqueConstraint(columnNames: "aer_code", constraintName: "UC_ACTION_EVENT_RESULTAER_CODE_COL", tableName: "action_event_result")

        // New table action_event_result_list
        createTable(tableName: "action_event_result_list") {
            column(name: "aerl_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "action_event_result_listPK")
            }

            column(name: "aerl_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "aerl_code", type: "VARCHAR(64)") {
                constraints(nullable: "false")
            }

            column(name: "aerl_description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }

        // Unique constraints for action_event_result_list
        addUniqueConstraint(columnNames: "aerl_code", constraintName: "UC_ACTION_EVENT_RESULT_LISTAERL_CODE_COL", tableName: "action_event_result_list")

        // New table action_event_result_list_action_event_result
        createTable(tableName: "action_event_result_list_action_event_result") {
            column(name: "action_event_result_list_results_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "action_event_result_id", type: "VARCHAR(36)")
        }

        // Amendments to table available_action
        addColumn(tableName: "available_action") {
            column(name: "aa_action_event", type: "varchar(36)")
        }

        addColumn(tableName: "available_action") {
            column(name: "aa_result_list", type: "varchar(36)")
        }

        // Unique constraints for available_action
        addUniqueConstraint(columnNames: "aa_from_state, aa_model, aa_action_event", constraintName: "UKc031dc3d5e10cefddb0d9a60ad06", tableName: "available_action")

        // Foreign key constraints for available_action
        addForeignKeyConstraint(baseColumnNames: "aa_from_state", baseTableName: "available_action", constraintName: "FK7l7es9gjswsngkpwn8knfvjep", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "aa_result_list", baseTableName: "available_action", constraintName: "FK8g2jku56t50gqbpnhj75tp8k6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "aerl_id", referencedTableName: "action_event_result_list", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "aa_model", baseTableName: "available_action", constraintName: "FK8njd3f91fnvebuhi2dbbvigq0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "aa_action_event", baseTableName: "available_action", constraintName: "FKsgyvqglhqgd3jvc2g28poji2w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ae_id", referencedTableName: "action_event", validate: "true")

        // Foreign key constraints for action_event
        addForeignKeyConstraint(baseColumnNames: "ae_result_list", baseTableName: "action_event", constraintName: "FK9c740m53gnkttw5xxq2gqeps1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "aerl_id", referencedTableName: "action_event_result_list", validate: "true")

        // Foreign key constraints for action_event_result
        addForeignKeyConstraint(baseColumnNames: "aer_status", baseTableName: "action_event_result", constraintName: "FKcn8gtigcg8rbddptings7inch", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")

        // Foreign key constraints for action_event_result_list_action_event_result
        addForeignKeyConstraint(baseColumnNames: "action_event_result_list_results_id", baseTableName: "action_event_result_list_action_event_result", constraintName: "FKdjfhf73xhq7pelqrvsaddijvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "aerl_id", referencedTableName: "action_event_result_list", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "action_event_result_id", baseTableName: "action_event_result_list_action_event_result", constraintName: "FKeqn26v2wfkten7kajil9as5v0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "aer_id", referencedTableName: "action_event_result", validate: "true")

        // Foreign key constraints for action_event_result
        addForeignKeyConstraint(baseColumnNames: "aer_next_action_event", baseTableName: "action_event_result", constraintName: "FKqca8tt0n4iv2y3srjm8d7ay9r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ae_id", referencedTableName: "action_event", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "aer_save_restore_state", baseTableName: "action_event_result", constraintName: "FKrkjbgy0y6lb0vn8j956y4komi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }
}
