databaseChangeLog = {
  changeSet(author: "Chas (generated)", id: "1679583267378") {
      addColumn(tableName: "action_event") {
          column(defaultValueComputed: "false", name: "ae_is_available_for_bulk", type: "boolean") {
              constraints(nullable: "false")
          }
      }
  }

    changeSet(author: "jskomorowski", id: "20230331-1130-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_delivery_method_fk", type: "varchar(36)")
        }
        addForeignKeyConstraint(baseColumnNames: "pr_delivery_method_fk", baseTableName: "patron_request", constraintName: "FK_pr_delivery_method", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "jskomorowski", id: "20230427-1130-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_pickup_url", type: "text")
        }
    }

    changeSet(author: "Chas (generated)", id: "1682588829787") {
        addColumn(tableName: "action_event") {
            column(defaultValueBoolean: "false", name: "ae_show_in_audit_trail", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }
    
    changeSet(author: "Chas (generated)", id: "1683194796434") {
        addColumn(tableName: "action_event_result") {
            column(defaultValueBoolean: "false", name: "aer_update_rota_location", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }
    
    changeSet(author: "Chas (generated)", id: "1684233996004") {
        createTable(tableName: "protocol_audit") {
            column(name: "pa_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "protocol_auditPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pa_response_body", type: "TEXT")

            column(name: "pa_date_created", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "pa_uri", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pa_protocol_method", type: "VARCHAR(30)") {
                constraints(nullable: "false")
            }

            column(name: "pa_protocol_type", type: "VARCHAR(30)") {
                constraints(nullable: "false")
            }

            column(name: "pa_request_body", type: "TEXT")

            column(name: "pa_response_status", type: "VARCHAR(30)")

            column(name: "pa_patron_request", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pa_duration", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
        
        // Not forgetting the foreign key constraint
        addForeignKeyConstraint(baseColumnNames: "pa_patron_request", baseTableName: "protocol_audit", constraintName: "FKlyemccy64kx2cm6mlbuaqjkb6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request", validate: "true")
    }
    
    changeSet(author: "Chas (generated)", id: "1685009183653") {
        createTable(tableName: "state_model_do_not_inherit_transition") {
            column(name: "smdnit_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "state_model_do_not_inherit_transitionPK")
            }

            column(name: "smdnit_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "smdnit_action_event", type: "VARCHAR(36)")

            column(name: "smdnit_state", type: "VARCHAR(36)")

            column(name: "smdnit_state_model", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }

        addUniqueConstraint(columnNames: "smdnit_action_event, smdnit_state, smdnit_state_model", constraintName: "UK6733f2d7bc0ed4dfc7d0f6ea4670", tableName: "state_model_do_not_inherit_transition")

        addForeignKeyConstraint(baseColumnNames: "smdnit_state", baseTableName: "state_model_do_not_inherit_transition", constraintName: "FK4rxfhi3178dvyj8mjm57jxk2r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "smdnit_action_event", baseTableName: "state_model_do_not_inherit_transition", constraintName: "FKqulkd03dg64ujc7j6mjn2xmad", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ae_id", referencedTableName: "action_event", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "smdnit_state_model", baseTableName: "state_model_do_not_inherit_transition", constraintName: "FKsysqbrevxc3hnai0vil5yqml2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model", validate: "true")
    }

    changeSet(author: "Chas (generated)", id: "1685548332293") {
        // I should not have added the unique constraint as the fields are nullable
        dropUniqueConstraint(constraintName: "UK6733f2d7bc0ed4dfc7d0f6ea4670", tableName: "state_model_do_not_inherit_transition")
    }
    
    changeSet(author: "Chas (generated)", id: "1687338130013") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_sent_iso18626_request_response", type: "boolean")
        }
    }
}
