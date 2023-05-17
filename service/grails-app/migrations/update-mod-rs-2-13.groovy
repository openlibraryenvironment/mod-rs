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
}
