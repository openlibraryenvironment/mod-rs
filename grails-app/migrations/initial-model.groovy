databaseChangeLog = {

    changeSet(author: "Chas (generated)", id: "1549548974499-1") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-2") {
        createTable(tableName: "custom_property") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "custom_propertyPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "definition_id", type: "VARCHAR(36)")

            column(name: "parent_id", type: "BIGINT")
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-3") {
        createTable(tableName: "custom_property_blob") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "OID") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-4") {
        createTable(tableName: "custom_property_boolean") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-5") {
        createTable(tableName: "custom_property_container") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-6") {
        createTable(tableName: "custom_property_decimal") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "NUMBER(19, 2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-7") {
        createTable(tableName: "custom_property_definition") {
            column(name: "pd_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pd_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pd_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pd_description", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-8") {
        createTable(tableName: "custom_property_integer") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-9") {
        createTable(tableName: "custom_property_refdata") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-10") {
        createTable(tableName: "custom_property_refdata_definition") {
            column(name: "pd_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-11") {
        createTable(tableName: "custom_property_text") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-12") {
        createTable(tableName: "patron_request") {
            column(name: "pr_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pr_is_requester", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "pr_date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "custom_properties_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pr_last_updated", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "pr_state_fk", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "pr_service_type_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pr_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pr_number_of_retries", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "pr_delay_performing_action_until", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "pr_pending_action_fk", type: "VARCHAR(20)") {
                constraints(nullable: "false")
            }

            column(name: "pr_patron_reference", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-13") {
        createTable(tableName: "patron_request_tag") {
            column(name: "patron_request_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-14") {
        createTable(tableName: "refdata_category") {
            column(name: "rdc_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "rdc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "rdc_description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-15") {
        createTable(tableName: "refdata_value") {
            column(name: "rdv_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "rdv_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "rdv_value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "rdv_owner", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "rdv_label", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-16") {
        createTable(tableName: "tag") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "tagPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "norm_value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-17") {
        createTable(tableName: "wf_action") {
            column(name: "act_id", type: "VARCHAR(20)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "act_selectable", type: "BOOLEAN")

            column(name: "act_status_success_yes", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "act_service_class", type: "VARCHAR(64)")

            column(name: "act_bulk_enabled", type: "BOOLEAN")

            column(name: "act_status_failure", type: "VARCHAR(40)")

            column(name: "act_name", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "act_description", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }

            column(name: "act_are_you_sure_dialog", type: "BOOLEAN")

            column(name: "act_status_success_no", type: "VARCHAR(40)")
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-18") {
        createTable(tableName: "wf_state_transition") {
            column(name: "st_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "st_next_action", type: "VARCHAR(20)")

            column(name: "st_to_status", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "st_action", type: "VARCHAR(20)") {
                constraints(nullable: "false")
            }

            column(name: "st_from_status", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "st_qualifier", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-19") {
        createTable(tableName: "wf_status") {
            column(name: "st_id", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "st_name", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "st_description", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-20") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_blobPK", tableName: "custom_property_blob")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-21") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_booleanPK", tableName: "custom_property_boolean")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-22") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_containerPK", tableName: "custom_property_container")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-23") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_decimalPK", tableName: "custom_property_decimal")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-24") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_definitionPK", tableName: "custom_property_definition")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-25") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_integerPK", tableName: "custom_property_integer")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-26") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_refdataPK", tableName: "custom_property_refdata")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-27") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_refdata_definitionPK", tableName: "custom_property_refdata_definition")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-28") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_textPK", tableName: "custom_property_text")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-29") {
        addPrimaryKey(columnNames: "pr_id", constraintName: "patron_requestPK", tableName: "patron_request")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-30") {
        addPrimaryKey(columnNames: "rdc_id", constraintName: "refdata_categoryPK", tableName: "refdata_category")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-31") {
        addPrimaryKey(columnNames: "rdv_id", constraintName: "refdata_valuePK", tableName: "refdata_value")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-32") {
        addPrimaryKey(columnNames: "act_id", constraintName: "wf_actionPK", tableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-33") {
        addPrimaryKey(columnNames: "st_id", constraintName: "wf_state_transitionPK", tableName: "wf_state_transition")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-34") {
        addPrimaryKey(columnNames: "st_id", constraintName: "wf_statusPK", tableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-35") {
        addUniqueConstraint(columnNames: "pd_name", constraintName: "UC_CUSTOM_PROPERTY_DEFINITIONPD_NAME_COL", tableName: "custom_property_definition")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-36") {
        addUniqueConstraint(columnNames: "act_id", constraintName: "UC_WF_ACTIONACT_ID_COL", tableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-37") {
        addUniqueConstraint(columnNames: "act_name", constraintName: "UC_WF_ACTIONACT_NAME_COL", tableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-38") {
        addUniqueConstraint(columnNames: "st_id", constraintName: "UC_WF_STATUSST_ID_COL", tableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-39") {
        addUniqueConstraint(columnNames: "st_qualifier, st_to_status, st_action, st_from_status", constraintName: "UKeff8c909efb95b785c2f415aba2b", tableName: "wf_state_transition")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-40") {
        createIndex(indexName: "rdv_entry_idx", tableName: "refdata_value") {
            column(name: "rdv_value")

            column(name: "rdv_owner")
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-41") {
        createIndex(indexName: "td_type_idx", tableName: "custom_property_definition") {
            column(name: "pd_type")
        }
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-42") {
        addForeignKeyConstraint(baseColumnNames: "st_next_action", baseTableName: "wf_state_transition", constraintName: "FK30ncwxy254t2y2udl7a5u0yom", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-43") {
        addForeignKeyConstraint(baseColumnNames: "definition_id", baseTableName: "custom_property", constraintName: "FK36grvth72fb7wu5i5xaeqjitw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "custom_property_definition")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-44") {
        addForeignKeyConstraint(baseColumnNames: "act_status_success_no", baseTableName: "wf_action", constraintName: "FK54agpfcs2ec291wxorsb6tekf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-45") {
        addForeignKeyConstraint(baseColumnNames: "pr_service_type_fk", baseTableName: "patron_request", constraintName: "FK5fk9w5hr3e7s3f2kg9t4acnmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-46") {
        addForeignKeyConstraint(baseColumnNames: "value_id", baseTableName: "custom_property_refdata", constraintName: "FK5ogn0fedwxxy4fhmq9du4qej2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-47") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "patron_request_tag", constraintName: "FK6h11nyf2iuoowopq6o047957x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tag")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-48") {
        addForeignKeyConstraint(baseColumnNames: "custom_properties_id", baseTableName: "patron_request", constraintName: "FKaeblgdoku7ylgu41p28vbn409", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-49") {
        addForeignKeyConstraint(baseColumnNames: "patron_request_tags_id", baseTableName: "patron_request_tag", constraintName: "FKagafoiedlc7mv2khl13xyfgc1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-50") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "custom_property_refdata_definition", constraintName: "FKbrh88caagajlvrpaydg4tr3qx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-51") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "custom_property", constraintName: "FKd5u2tgpracxvk1xw8pdreuj5h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-52") {
        addForeignKeyConstraint(baseColumnNames: "act_status_failure", baseTableName: "wf_action", constraintName: "FKd9q88hl86s7msg25s7tnyhmid", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-53") {
        addForeignKeyConstraint(baseColumnNames: "rdv_owner", baseTableName: "refdata_value", constraintName: "FKh4fon2a7k4y8b2sicjm0i6oy8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-54") {
        addForeignKeyConstraint(baseColumnNames: "pr_state_fk", baseTableName: "patron_request", constraintName: "FKillr6lvs9tdhtyj1cf2vcrpjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-55") {
        addForeignKeyConstraint(baseColumnNames: "pr_pending_action_fk", baseTableName: "patron_request", constraintName: "FKkdecu5xwh2nia2au733mekfq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-56") {
        addForeignKeyConstraint(baseColumnNames: "st_from_status", baseTableName: "wf_state_transition", constraintName: "FKnc66itnyv4tktwsk1j485uwtb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-57") {
        addForeignKeyConstraint(baseColumnNames: "st_action", baseTableName: "wf_state_transition", constraintName: "FKnk0ip7qktjpoeqx07vfxessai", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-58") {
        addForeignKeyConstraint(baseColumnNames: "st_to_status", baseTableName: "wf_state_transition", constraintName: "FKqbvgp73klrad0cwu4brdg6i59", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1549548974499-59") {
        addForeignKeyConstraint(baseColumnNames: "act_status_success_yes", baseTableName: "wf_action", constraintName: "FKtjmco83abnf357nvx18ung5fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }
}
