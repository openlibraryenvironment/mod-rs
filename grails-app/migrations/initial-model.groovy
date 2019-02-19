databaseChangeLog = {

    changeSet(author: "Chas (generated)", id: "1550575000792-1") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-2") {
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

    changeSet(author: "Chas (generated)", id: "1550575000792-3") {
        createTable(tableName: "custom_property_blob") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "OID") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-4") {
        createTable(tableName: "custom_property_boolean") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-5") {
        createTable(tableName: "custom_property_container") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-6") {
        createTable(tableName: "custom_property_decimal") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "NUMBER(19, 2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-7") {
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

            column(name: "pd_label", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pd_description", type: "VARCHAR(255)")

            column(name: "pd_weight", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-8") {
        createTable(tableName: "custom_property_integer") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-9") {
        createTable(tableName: "custom_property_refdata") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-10") {
        createTable(tableName: "custom_property_refdata_definition") {
            column(name: "pd_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-11") {
        createTable(tableName: "custom_property_text") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-12") {
        createTable(tableName: "patron_request") {
            column(name: "pr_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pr_pre_error_status_fk", type: "VARCHAR(40)")

            column(name: "pr_is_requester", type: "BOOLEAN")

            column(name: "pr_publisher", type: "VARCHAR(255)")

            column(name: "pr_pub_date", type: "VARCHAR(255)")

            column(name: "pr_date_created", type: "timestamp")

            column(name: "custom_properties_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pr_edition", type: "VARCHAR(255)")

            column(name: "pr_last_updated", type: "timestamp")

            column(name: "pr_pub_type_fk", type: "VARCHAR(36)")

            column(name: "pr_place_of_pub", type: "VARCHAR(255)")

            column(name: "pr_state_fk", type: "VARCHAR(40)")

            column(name: "pr_author", type: "VARCHAR(255)")

            column(name: "pr_service_type_fk", type: "VARCHAR(36)")

            column(name: "pr_volume", type: "VARCHAR(255)")

            column(name: "pr_title", type: "VARCHAR(255)")

            column(name: "pr_start_page", type: "VARCHAR(255)")

            column(name: "pr_num_pages", type: "VARCHAR(255)")

            column(name: "pr_pubdate_of_component", type: "VARCHAR(255)")

            column(name: "pr_number_of_retries", type: "INT")

            column(name: "pr_delay_performing_action_until", type: "timestamp")

            column(name: "pr_pending_action_fk", type: "VARCHAR(20)")

            column(name: "pr_sponsoring_body", type: "VARCHAR(255)")

            column(name: "pr_patron_reference", type: "VARCHAR(255)")

            column(name: "pr_issue", type: "VARCHAR(255)")

            column(name: "pr_error_action_fk", type: "VARCHAR(20)")

            column(name: "pr_sub_title", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-13") {
        createTable(tableName: "patron_request_audit") {
            column(autoIncrement: "true", name: "pra_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "patron_request_auditPK")
            }

            column(name: "pra_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pra_to_status_fk", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "pra_date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "pra_action_fk", type: "VARCHAR(20)") {
                constraints(nullable: "false")
            }

            column(name: "pra_patron_request_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pra_from_status_fk", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }

            column(name: "pra_duration", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-14") {
        createTable(tableName: "patron_request_tag") {
            column(name: "patron_request_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-15") {
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

    changeSet(author: "Chas (generated)", id: "1550575000792-16") {
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

    changeSet(author: "Chas (generated)", id: "1550575000792-17") {
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

    changeSet(author: "Chas (generated)", id: "1550575000792-18") {
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

            column(name: "act_are_you_sure_dialog", type: "BOOLEAN")

            column(name: "act_description", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }

            column(name: "act_status_success_no", type: "VARCHAR(40)")
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-19") {
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

    changeSet(author: "Chas (generated)", id: "1550575000792-20") {
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

    changeSet(author: "Chas (generated)", id: "1550575000792-21") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_blobPK", tableName: "custom_property_blob")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-22") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_booleanPK", tableName: "custom_property_boolean")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-23") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_containerPK", tableName: "custom_property_container")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-24") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_decimalPK", tableName: "custom_property_decimal")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-25") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_definitionPK", tableName: "custom_property_definition")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-26") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_integerPK", tableName: "custom_property_integer")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-27") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_refdataPK", tableName: "custom_property_refdata")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-28") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_refdata_definitionPK", tableName: "custom_property_refdata_definition")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-29") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_textPK", tableName: "custom_property_text")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-30") {
        addPrimaryKey(columnNames: "pr_id", constraintName: "patron_requestPK", tableName: "patron_request")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-31") {
        addPrimaryKey(columnNames: "rdc_id", constraintName: "refdata_categoryPK", tableName: "refdata_category")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-32") {
        addPrimaryKey(columnNames: "rdv_id", constraintName: "refdata_valuePK", tableName: "refdata_value")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-33") {
        addPrimaryKey(columnNames: "act_id", constraintName: "wf_actionPK", tableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-34") {
        addPrimaryKey(columnNames: "st_id", constraintName: "wf_state_transitionPK", tableName: "wf_state_transition")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-35") {
        addPrimaryKey(columnNames: "st_id", constraintName: "wf_statusPK", tableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-36") {
        addUniqueConstraint(columnNames: "pd_name", constraintName: "UC_CUSTOM_PROPERTY_DEFINITIONPD_NAME_COL", tableName: "custom_property_definition")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-37") {
        addUniqueConstraint(columnNames: "act_id", constraintName: "UC_WF_ACTIONACT_ID_COL", tableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-38") {
        addUniqueConstraint(columnNames: "act_name", constraintName: "UC_WF_ACTIONACT_NAME_COL", tableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-39") {
        addUniqueConstraint(columnNames: "st_id", constraintName: "UC_WF_STATUSST_ID_COL", tableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-40") {
        addUniqueConstraint(columnNames: "st_qualifier, st_to_status, st_action, st_from_status", constraintName: "UKeff8c909efb95b785c2f415aba2b", tableName: "wf_state_transition")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-41") {
        createIndex(indexName: "rdv_entry_idx", tableName: "refdata_value") {
            column(name: "rdv_value")

            column(name: "rdv_owner")
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-42") {
        createIndex(indexName: "td_label_idx", tableName: "custom_property_definition") {
            column(name: "pd_label")
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-43") {
        createIndex(indexName: "td_type_idx", tableName: "custom_property_definition") {
            column(name: "pd_type")
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-44") {
        createIndex(indexName: "td_weight_idx", tableName: "custom_property_definition") {
            column(name: "pd_weight")
        }
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-45") {
        addForeignKeyConstraint(baseColumnNames: "st_next_action", baseTableName: "wf_state_transition", constraintName: "FK30ncwxy254t2y2udl7a5u0yom", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-46") {
        addForeignKeyConstraint(baseColumnNames: "definition_id", baseTableName: "custom_property", constraintName: "FK36grvth72fb7wu5i5xaeqjitw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "custom_property_definition")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-47") {
        addForeignKeyConstraint(baseColumnNames: "act_status_success_no", baseTableName: "wf_action", constraintName: "FK54agpfcs2ec291wxorsb6tekf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-48") {
        addForeignKeyConstraint(baseColumnNames: "pr_pre_error_status_fk", baseTableName: "patron_request", constraintName: "FK572bsmwrj7ngybno7ni0shmbo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-49") {
        addForeignKeyConstraint(baseColumnNames: "pr_service_type_fk", baseTableName: "patron_request", constraintName: "FK5fk9w5hr3e7s3f2kg9t4acnmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-50") {
        addForeignKeyConstraint(baseColumnNames: "pr_error_action_fk", baseTableName: "patron_request", constraintName: "FK5g4uhtsfrndbtm0yle0q9sr0j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-51") {
        addForeignKeyConstraint(baseColumnNames: "value_id", baseTableName: "custom_property_refdata", constraintName: "FK5ogn0fedwxxy4fhmq9du4qej2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-52") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "patron_request_tag", constraintName: "FK6h11nyf2iuoowopq6o047957x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tag")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-53") {
        addForeignKeyConstraint(baseColumnNames: "pra_from_status_fk", baseTableName: "patron_request_audit", constraintName: "FK828uo4wvgtle3h5gjgjtdaqeb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-54") {
        addForeignKeyConstraint(baseColumnNames: "pra_patron_request_fk", baseTableName: "patron_request_audit", constraintName: "FK9x1a04d0r113lvv8f7cbxcmxu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-55") {
        addForeignKeyConstraint(baseColumnNames: "custom_properties_id", baseTableName: "patron_request", constraintName: "FKaeblgdoku7ylgu41p28vbn409", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-56") {
        addForeignKeyConstraint(baseColumnNames: "patron_request_tags_id", baseTableName: "patron_request_tag", constraintName: "FKagafoiedlc7mv2khl13xyfgc1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-57") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "custom_property_refdata_definition", constraintName: "FKbrh88caagajlvrpaydg4tr3qx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-58") {
        addForeignKeyConstraint(baseColumnNames: "pra_to_status_fk", baseTableName: "patron_request_audit", constraintName: "FKbtnhcw5087d39eubo6h04p1jq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-59") {
        addForeignKeyConstraint(baseColumnNames: "pr_pub_type_fk", baseTableName: "patron_request", constraintName: "FKc8gk5vkafp9rxv346hvoxrpcy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-60") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "custom_property", constraintName: "FKd5u2tgpracxvk1xw8pdreuj5h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-61") {
        addForeignKeyConstraint(baseColumnNames: "act_status_failure", baseTableName: "wf_action", constraintName: "FKd9q88hl86s7msg25s7tnyhmid", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-62") {
        addForeignKeyConstraint(baseColumnNames: "rdv_owner", baseTableName: "refdata_value", constraintName: "FKh4fon2a7k4y8b2sicjm0i6oy8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-63") {
        addForeignKeyConstraint(baseColumnNames: "pr_state_fk", baseTableName: "patron_request", constraintName: "FKillr6lvs9tdhtyj1cf2vcrpjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-64") {
        addForeignKeyConstraint(baseColumnNames: "pr_pending_action_fk", baseTableName: "patron_request", constraintName: "FKkdecu5xwh2nia2au733mekfq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-65") {
        addForeignKeyConstraint(baseColumnNames: "pra_action_fk", baseTableName: "patron_request_audit", constraintName: "FKmmpfwbq5yrlp7f6ybg9voo6qh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-66") {
        addForeignKeyConstraint(baseColumnNames: "st_from_status", baseTableName: "wf_state_transition", constraintName: "FKnc66itnyv4tktwsk1j485uwtb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-67") {
        addForeignKeyConstraint(baseColumnNames: "st_action", baseTableName: "wf_state_transition", constraintName: "FKnk0ip7qktjpoeqx07vfxessai", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "act_id", referencedTableName: "wf_action")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-68") {
        addForeignKeyConstraint(baseColumnNames: "st_to_status", baseTableName: "wf_state_transition", constraintName: "FKqbvgp73klrad0cwu4brdg6i59", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }

    changeSet(author: "Chas (generated)", id: "1550575000792-69") {
        addForeignKeyConstraint(baseColumnNames: "act_status_success_yes", baseTableName: "wf_action", constraintName: "FKtjmco83abnf357nvx18ung5fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "wf_status")
    }
}
