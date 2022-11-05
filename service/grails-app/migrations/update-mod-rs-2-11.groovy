databaseChangeLog = {
    changeSet(author: "Chas (generated)", id: "1663670037743-1") {

        // PR-1340 Added the inital state a model will be in when a request is created
        addColumn(tableName: "state_model") {
            column(name: "sm_initial_state", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "sm_initial_state", baseTableName: "state_model", constraintName: "FK6w8e51foyiqg6rrlboxdgom1y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")
    }

    changeSet(author: "Chas (generated)", id: "1663850777344-1") {
        // The code on status is now unique
        addUniqueConstraint(columnNames: "st_code", constraintName: "UC_STATUSST_CODE_COL", tableName: "status")
    }

    changeSet(author: "Chas (generated)", id: "1663850777344-2") {
        // State model no longer lives on Status
        dropForeignKeyConstraint(baseTableName: "status", constraintName: "FK510qo8iuecwl6gomqmcrwoejg")
    }

    changeSet(author: "Chas (generated)", id: "1663850777344-3") {
        // Column no longer required
        dropColumn(columnName: "st_owner", tableName: "status")
    }

    changeSet(author: "Chas (generated)", id: "1664185362263-1") {
        // Add the state model column to patron request, so that we stick with the same state model for the duration of the request
        addColumn(tableName: "patron_request") {
            column(name: "pr_state_model_fk", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "pr_state_model_fk", baseTableName: "patron_request", constraintName: "FK15q1ukh3csn2iolc5wn0ak93s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model", validate: "true")

        // We need to populate this field for existing requests
        grailsChange {
            change {
                // First the requester requests
                sql.execute("""update ${database.defaultSchemaName}.patron_request
                               set pr_state_model_fk = (select sm_id from ${database.defaultSchemaName}.state_model where sm_shortcode = 'PatronRequest')
                               where pr_is_requester = true""".toString());

                // Now for the responder requests
                sql.execute("""update ${database.defaultSchemaName}.patron_request
                               set pr_state_model_fk = (select sm_id from ${database.defaultSchemaName}.state_model where sm_shortcode = 'Responder')
                               where pr_is_requester = false""".toString());
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1664288044250-1") {
        addColumn(tableName: "action_event") {
            column(name: "ae_service_class", type: "varchar(64)")
        }
    }

    changeSet(author: "Chas (generated)", id: "1664358925815-1") {
        // If a request is determined to be stale then this is the action to be performed
        addColumn(tableName: "state_model") {
            column(name: "sm_stale_action", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "sm_stale_action", baseTableName: "state_model", constraintName: "FKrs32cws730v084r29fyw1awx3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ae_id", referencedTableName: "action_event", validate: "true")

        // If a request is overdue this column holds the status we change to
        addColumn(tableName: "state_model") {
            column(name: "sm_overdue_status", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "sm_overdue_status", baseTableName: "state_model", constraintName: "FKnhr0p3t18clni7rjkx0gu81wm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")
    }

    changeSet(author: "Chas (generated)", id: "1664874996380-1") {
        createTable(tableName: "state_model_status") {
            column(name: "sms_state_model", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "state_model_statusPK")
            }

            column(name: "sms_state", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "state_model_statusPK")
            }

            column(name: "sms_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sms_can_trigger_overdue_request", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "sms_can_trigger_stale_request", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "sms_is_terminal", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }

        // Add the foreign key constraints
        addForeignKeyConstraint(baseColumnNames: "sms_state_model", baseTableName: "state_model_status", constraintName: "FK25422f62cugfnficmn7xoeram", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model", validate: "true")
        addForeignKeyConstraint(baseColumnNames: "sms_state", baseTableName: "state_model_status", constraintName: "FKmkj0x77yy0pixpdwqjc8p71gj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")
    }
    
    changeSet(author: "Chas (generated)", id: "1665136023956-1") {
        addColumn(tableName: "action_event") {
            column(name: "ae_responder_service_class", type: "varchar(64)")
        }
    }
    
    changeSet(author: "Chas (generated)", id: "1665402489495-1") {
        addColumn(tableName: "status") {
            column(name: "st_terminal_sequence", type: "int4")
        }
    }
    
    changeSet(author: "Chas (generated)", id: "1665736736915-1") {
        addColumn(tableName: "state_model_status") {
            column(defaultValueBoolean: "false", name: "sms_trigger_pull_slip_email", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }
    
    changeSet(author: "Chas (generated)", id: "1666622872063-1") {
        addColumn(tableName: "timer") {
            column(defaultValueBoolean: "false", name: "tr_execute_at_day_start", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }
}
