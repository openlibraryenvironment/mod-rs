databaseChangeLog = {

    changeSet(author: "ianibbo (generated)", id: "1567949522352-1") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-2") {
        createTable(tableName: "address") {
            column(name: "addr_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "addr_label", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-3") {
        createTable(tableName: "address_line") {
            column(name: "al_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "al_seq", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "al_value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "al_type_rv_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-4") {
        createTable(tableName: "address_tag") {
            column(name: "address_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-5") {
        createTable(tableName: "announcement") {
            column(name: "ann_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ann_code", type: "VARCHAR(255)")

            column(name: "ann_expiry_date", type: "timestamp")

            column(name: "ann_announce_date", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "ann_owner_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "ann_description", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-6") {
        createTable(tableName: "custom_property") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "custom_propertyPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "public_note", type: "CLOB")

            column(name: "definition_id", type: "VARCHAR(36)")

            column(name: "note", type: "CLOB")

            column(name: "internal", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "parent_id", type: "BIGINT")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-7") {
        createTable(tableName: "custom_property_blob") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "OID") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-8") {
        createTable(tableName: "custom_property_boolean") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-9") {
        createTable(tableName: "custom_property_container") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-10") {
        createTable(tableName: "custom_property_decimal") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "NUMBER(19, 2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-11") {
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

            column(name: "pd_primary", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "pd_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "default_internal", type: "BOOLEAN") {
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

    changeSet(author: "ianibbo (generated)", id: "1567949522352-12") {
        createTable(tableName: "custom_property_integer") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-13") {
        createTable(tableName: "custom_property_refdata") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-14") {
        createTable(tableName: "custom_property_refdata_definition") {
            column(name: "pd_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-15") {
        createTable(tableName: "custom_property_text") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-16") {
        createTable(tableName: "directory_entry") {
            column(name: "de_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "custom_properties_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "de_slug", type: "VARCHAR(255)")

            column(name: "de_foaf_timestamp", type: "BIGINT")

            column(name: "de_foaf_url", type: "VARCHAR(255)")

            column(name: "de_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "de_status_fk", type: "VARCHAR(36)")

            column(name: "de_desc", type: "VARCHAR(255)")

            column(name: "de_parent", type: "VARCHAR(36)")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-17") {
        createTable(tableName: "directory_entry_tag") {
            column(name: "directory_entry_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-18") {
        createTable(tableName: "friend_assertion") {
            column(name: "fa_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fa_friend_org", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "fa_owner", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-19") {
        createTable(tableName: "naming_authority") {
            column(name: "na_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "na_symbol", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-20") {
        createTable(tableName: "patron_request") {
            column(name: "pr_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pr_patron_surname", type: "VARCHAR(255)")

            column(name: "pr_date_created", type: "timestamp")

            column(name: "pr_pub_date", type: "VARCHAR(255)")

            column(name: "pr_edition", type: "VARCHAR(255)")

            column(name: "pr_artnum", type: "VARCHAR(255)")

            column(name: "pr_req_inst_symbol", type: "VARCHAR(255)")

            column(name: "pr_doi", type: "VARCHAR(255)")

            column(name: "pr_isbn", type: "VARCHAR(255)")

            column(name: "pr_information_source", type: "VARCHAR(255)")

            column(name: "pr_bici", type: "VARCHAR(255)")

            column(name: "pr_place_of_pub", type: "VARCHAR(255)")

            column(name: "pr_patron_identifier", type: "VARCHAR(255)")

            column(name: "pr_state_fk", type: "VARCHAR(36)")

            column(name: "pr_needed_by", type: "timestamp")

            column(name: "pr_volume", type: "VARCHAR(255)")

            column(name: "pr_title_of_component", type: "VARCHAR(255)")

            column(name: "pr_coden", type: "VARCHAR(255)")

            column(name: "pr_num_pages", type: "VARCHAR(255)")

            column(name: "pr_delay_performing_action_until", type: "timestamp")

            column(name: "pr_stitle", type: "VARCHAR(255)")

            column(name: "pr_patron_reference", type: "VARCHAR(255)")

            column(name: "pr_system_instance_id", type: "VARCHAR(255)")

            column(name: "pr_issue", type: "VARCHAR(255)")

            column(name: "pr_pre_error_status_fk", type: "VARCHAR(36)")

            column(name: "pr_part", type: "VARCHAR(255)")

            column(name: "pr_is_requester", type: "BOOLEAN")

            column(name: "pr_publisher", type: "VARCHAR(255)")

            column(name: "pr_patron_name", type: "VARCHAR(255)")

            column(name: "custom_properties_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pr_sponsor", type: "VARCHAR(255)")

            column(name: "pr_author_of_component", type: "VARCHAR(255)")

            column(name: "pr_last_updated", type: "timestamp")

            column(name: "pr_rota_position", type: "BIGINT")

            column(name: "pr_pub_type_fk", type: "VARCHAR(36)")

            column(name: "pr_author", type: "VARCHAR(255)")

            column(name: "pr_service_type_fk", type: "VARCHAR(36)")

            column(name: "pr_issn", type: "VARCHAR(255)")

            column(name: "pr_title", type: "VARCHAR(255)")

            column(name: "pr_start_page", type: "VARCHAR(255)")

            column(name: "pr_send_to_patron", type: "BOOLEAN")

            column(name: "pr_eissn", type: "VARCHAR(255)")

            column(name: "pr_pubdate_of_component", type: "VARCHAR(255)")

            column(name: "pr_number_of_retries", type: "INT")

            column(name: "pr_ssn", type: "VARCHAR(255)")

            column(name: "pr_awaiting_protocol_response", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "pr_sici", type: "VARCHAR(255)")

            column(name: "pr_sponsoring_body", type: "VARCHAR(255)")

            column(name: "pr_patron_type", type: "VARCHAR(255)")

            column(name: "pr_quarter", type: "VARCHAR(255)")

            column(name: "pr_sub_title", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-21") {
        createTable(tableName: "patron_request_audit") {
            column(name: "pra_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pra_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pra_to_status_fk", type: "VARCHAR(36)")

            column(name: "pra_message", type: "CLOB")

            column(name: "pra_date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "pra_audit_data", type: "CLOB")

            column(name: "pra_patron_request_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pra_from_status_fk", type: "VARCHAR(36)")

            column(name: "pra_duration", type: "BIGINT")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-22") {
        createTable(tableName: "patron_request_rota") {
            column(name: "prr_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "prr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prr_system_identifier", type: "VARCHAR(255)")

            column(name: "prr_date_created", type: "timestamp")

            column(name: "prr_last_updated", type: "timestamp")

            column(name: "prr_protocol_status", type: "BIGINT")

            column(name: "prr_rota_position", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prr_directory_id_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "prr_patron_request_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "prr_state_fk", type: "VARCHAR(36)")

            column(name: "prr_peer_fk", type: "VARCHAR(36)")

            column(name: "prr_availability", type: "VARCHAR(255)")

            column(name: "prr_normalised_availability", type: "VARCHAR(255)")

            column(name: "prr_shelfmark", type: "VARCHAR(255)")

            column(name: "prr_available_from", type: "timestamp")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-23") {
        createTable(tableName: "patron_request_tag") {
            column(name: "patron_request_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-24") {
        createTable(tableName: "protocol_conversion") {
            column(name: "pc_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pc_protocol", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pc_conversion_value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pc_reference_value", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-25") {
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

    changeSet(author: "ianibbo (generated)", id: "1567949522352-26") {
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

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-27") {
        createTable(tableName: "service") {
            column(name: "se_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "se_address", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "custom_properties_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "se_name", type: "VARCHAR(255)")

            column(name: "se_type_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "se_business_function_fk", type: "VARCHAR(36)")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-28") {
        createTable(tableName: "service_account") {
            column(name: "sa_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sa_account_holder", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "custom_properties_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sa_service", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "sa_account_details", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-29") {
        createTable(tableName: "service_tag") {
            column(name: "service_tags_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "BIGINT")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-30") {
        createTable(tableName: "shipment") {
            column(name: "sh_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "sh_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sh_date_created", type: "timestamp")

            column(name: "sh_last_updated", type: "timestamp")

            column(name: "sh_received_date", type: "timestamp")

            column(name: "sh_shipment_method_fk", type: "VARCHAR(36)")

            column(name: "sh_directory_entry_fk", type: "VARCHAR(36)")

            column(name: "sh_status_fk", type: "VARCHAR(36)")

            column(name: "sh_ship_date", type: "timestamp")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-31") {
        createTable(tableName: "shipment_item") {
            column(name: "si_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "si_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "si_date_created", type: "timestamp")

            column(name: "si_last_updated", type: "timestamp")

            column(name: "si_shipment_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "is_returning", type: "BOOLEAN")

            column(name: "si_patron_request_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-32") {
        createTable(tableName: "state_model") {
            column(name: "sm_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "sm_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sm_name", type: "VARCHAR(255)")

            column(name: "sm_shortcode", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-33") {
        createTable(tableName: "status") {
            column(name: "st_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "st_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "st_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "st_owner", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-34") {
        createTable(tableName: "symbol") {
            column(name: "sym_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sym_priority", type: "VARCHAR(255)")

            column(name: "sym_authority_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "sym_owner_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "sym_symbol", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-35") {
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

    changeSet(author: "ianibbo (generated)", id: "1567949522352-36") {
        createTable(tableName: "tenant_symbol_mapping") {
            column(name: "tsm_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tsm_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tsm_symbol", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "tsm_tenant", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "tsm_block_loopback", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-37") {
        addPrimaryKey(columnNames: "addr_id", constraintName: "addressPK", tableName: "address")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-38") {
        addPrimaryKey(columnNames: "al_id", constraintName: "address_linePK", tableName: "address_line")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-39") {
        addPrimaryKey(columnNames: "ann_id", constraintName: "announcementPK", tableName: "announcement")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-40") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_blobPK", tableName: "custom_property_blob")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-41") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_booleanPK", tableName: "custom_property_boolean")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-42") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_containerPK", tableName: "custom_property_container")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-43") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_decimalPK", tableName: "custom_property_decimal")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-44") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_definitionPK", tableName: "custom_property_definition")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-45") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_integerPK", tableName: "custom_property_integer")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-46") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_refdataPK", tableName: "custom_property_refdata")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-47") {
        addPrimaryKey(columnNames: "pd_id", constraintName: "custom_property_refdata_definitionPK", tableName: "custom_property_refdata_definition")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-48") {
        addPrimaryKey(columnNames: "id", constraintName: "custom_property_textPK", tableName: "custom_property_text")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-49") {
        addPrimaryKey(columnNames: "de_id", constraintName: "directory_entryPK", tableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-50") {
        addPrimaryKey(columnNames: "fa_id", constraintName: "friend_assertionPK", tableName: "friend_assertion")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-51") {
        addPrimaryKey(columnNames: "na_id", constraintName: "naming_authorityPK", tableName: "naming_authority")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-52") {
        addPrimaryKey(columnNames: "pr_id", constraintName: "patron_requestPK", tableName: "patron_request")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-53") {
        addPrimaryKey(columnNames: "pra_id", constraintName: "patron_request_auditPK", tableName: "patron_request_audit")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-54") {
        addPrimaryKey(columnNames: "prr_id", constraintName: "patron_request_rotaPK", tableName: "patron_request_rota")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-55") {
        addPrimaryKey(columnNames: "pc_id", constraintName: "protocol_conversionPK", tableName: "protocol_conversion")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-56") {
        addPrimaryKey(columnNames: "rdc_id", constraintName: "refdata_categoryPK", tableName: "refdata_category")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-57") {
        addPrimaryKey(columnNames: "rdv_id", constraintName: "refdata_valuePK", tableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-58") {
        addPrimaryKey(columnNames: "se_id", constraintName: "servicePK", tableName: "service")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-59") {
        addPrimaryKey(columnNames: "sa_id", constraintName: "service_accountPK", tableName: "service_account")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-60") {
        addPrimaryKey(columnNames: "sh_id", constraintName: "shipmentPK", tableName: "shipment")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-61") {
        addPrimaryKey(columnNames: "si_id", constraintName: "shipment_itemPK", tableName: "shipment_item")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-62") {
        addPrimaryKey(columnNames: "sm_id", constraintName: "state_modelPK", tableName: "state_model")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-63") {
        addPrimaryKey(columnNames: "st_id", constraintName: "statusPK", tableName: "status")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-64") {
        addPrimaryKey(columnNames: "sym_id", constraintName: "symbolPK", tableName: "symbol")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-65") {
        addPrimaryKey(columnNames: "tsm_id", constraintName: "tenant_symbol_mappingPK", tableName: "tenant_symbol_mapping")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-66") {
        addUniqueConstraint(columnNames: "pd_name", constraintName: "UC_CUSTOM_PROPERTY_DEFINITIONPD_NAME_COL", tableName: "custom_property_definition")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-67") {
        createIndex(indexName: "rdv_entry_idx", tableName: "refdata_value") {
            column(name: "rdv_value")

            column(name: "rdv_owner")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-68") {
        createIndex(indexName: "td_label_idx", tableName: "custom_property_definition") {
            column(name: "pd_label")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-69") {
        createIndex(indexName: "td_primary_idx", tableName: "custom_property_definition") {
            column(name: "pd_primary")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-70") {
        createIndex(indexName: "td_type_idx", tableName: "custom_property_definition") {
            column(name: "pd_type")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-71") {
        createIndex(indexName: "td_weight_idx", tableName: "custom_property_definition") {
            column(name: "pd_weight")
        }
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-72") {
        addForeignKeyConstraint(baseColumnNames: "de_status_fk", baseTableName: "directory_entry", constraintName: "FK19lypn8h0g8kvr1cke6ddyjwg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-73") {
        addForeignKeyConstraint(baseColumnNames: "de_parent", baseTableName: "directory_entry", constraintName: "FK1lcdvuk9hkmebm544kwmxoclj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-74") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "address_line", constraintName: "FK27dakevcmnu3o22tdrpob6npg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "addr_id", referencedTableName: "address")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-75") {
        addForeignKeyConstraint(baseColumnNames: "custom_properties_id", baseTableName: "directory_entry", constraintName: "FK2qp9dd004mntrub21o6djlxqh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-76") {
        addForeignKeyConstraint(baseColumnNames: "definition_id", baseTableName: "custom_property", constraintName: "FK36grvth72fb7wu5i5xaeqjitw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "custom_property_definition")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-77") {
        addForeignKeyConstraint(baseColumnNames: "se_type_fk", baseTableName: "service", constraintName: "FK37qd0xlyn5tpy48wega3ss3hy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-78") {
        addForeignKeyConstraint(baseColumnNames: "prr_patron_request_fk", baseTableName: "patron_request_rota", constraintName: "FK44e42grtfehtfv3q11yhbvxl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-79") {
        addForeignKeyConstraint(baseColumnNames: "ann_owner_fk", baseTableName: "announcement", constraintName: "FK4hir8ts72q8qvhr7skxe8wss9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-80") {
        addForeignKeyConstraint(baseColumnNames: "st_owner", baseTableName: "status", constraintName: "FK510qo8iuecwl6gomqmcrwoejg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-81") {
        addForeignKeyConstraint(baseColumnNames: "pr_service_type_fk", baseTableName: "patron_request", constraintName: "FK5fk9w5hr3e7s3f2kg9t4acnmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-82") {
        addForeignKeyConstraint(baseColumnNames: "value_id", baseTableName: "custom_property_refdata", constraintName: "FK5ogn0fedwxxy4fhmq9du4qej2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-83") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "patron_request_tag", constraintName: "FK6h11nyf2iuoowopq6o047957x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tag")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-84") {
        addForeignKeyConstraint(baseColumnNames: "directory_entry_tags_id", baseTableName: "directory_entry_tag", constraintName: "FK73prfacykqmx20o3gr9dr7b98", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-85") {
        addForeignKeyConstraint(baseColumnNames: "pc_protocol", baseTableName: "protocol_conversion", constraintName: "FK7ap6nhx5h49y414ixd87v9yhd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-86") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "address_tag", constraintName: "FK8mggv80lsn331xa42585kim18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tag")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-87") {
        addForeignKeyConstraint(baseColumnNames: "sh_status_fk", baseTableName: "shipment", constraintName: "FK8p7dgp99ud090qg4todpfxb5p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-88") {
        addForeignKeyConstraint(baseColumnNames: "pra_patron_request_fk", baseTableName: "patron_request_audit", constraintName: "FK9x1a04d0r113lvv8f7cbxcmxu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-89") {
        addForeignKeyConstraint(baseColumnNames: "pc_reference_value", baseTableName: "protocol_conversion", constraintName: "FK9xvislmhbajmpsasxavur17o3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-90") {
        addForeignKeyConstraint(baseColumnNames: "pra_from_status_fk", baseTableName: "patron_request_audit", constraintName: "FKabffyd5nlq7y7qnlt5hvgll1i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-91") {
        addForeignKeyConstraint(baseColumnNames: "custom_properties_id", baseTableName: "patron_request", constraintName: "FKaeblgdoku7ylgu41p28vbn409", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-92") {
        addForeignKeyConstraint(baseColumnNames: "patron_request_tags_id", baseTableName: "patron_request_tag", constraintName: "FKagafoiedlc7mv2khl13xyfgc1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-93") {
        addForeignKeyConstraint(baseColumnNames: "fa_friend_org", baseTableName: "friend_assertion", constraintName: "FKam7kxpwd75are1h7o0easuo03", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-94") {
        addForeignKeyConstraint(baseColumnNames: "sym_owner_fk", baseTableName: "symbol", constraintName: "FKatkxebh688uppornia9wp6u0o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-95") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "custom_property_refdata_definition", constraintName: "FKbrh88caagajlvrpaydg4tr3qx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-96") {
        addForeignKeyConstraint(baseColumnNames: "pr_pub_type_fk", baseTableName: "patron_request", constraintName: "FKc8gk5vkafp9rxv346hvoxrpcy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-97") {
        addForeignKeyConstraint(baseColumnNames: "prr_state_fk", baseTableName: "patron_request_rota", constraintName: "FKcdhnwi83af93vgvhxq39a4cu0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-98") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "custom_property", constraintName: "FKd5u2tgpracxvk1xw8pdreuj5h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-99") {
        addForeignKeyConstraint(baseColumnNames: "sym_authority_fk", baseTableName: "symbol", constraintName: "FKgd9iwv5imahohd3irh7a4tysq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "na_id", referencedTableName: "naming_authority")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-100") {
        addForeignKeyConstraint(baseColumnNames: "sh_shipment_method_fk", baseTableName: "shipment", constraintName: "FKgkow56qifdc59yquh1wgcjiww", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-101") {
        addForeignKeyConstraint(baseColumnNames: "rdv_owner", baseTableName: "refdata_value", constraintName: "FKh4fon2a7k4y8b2sicjm0i6oy8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-102") {
        addForeignKeyConstraint(baseColumnNames: "custom_properties_id", baseTableName: "service_account", constraintName: "FKh8o9kxfjd3rn84sjhf2m8k1kd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-103") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "address", constraintName: "FKiscq9dhgj0e6hxlj49ejxavw1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-104") {
        addForeignKeyConstraint(baseColumnNames: "pra_to_status_fk", baseTableName: "patron_request_audit", constraintName: "FKjpyw1slnckaux7ypxrbu21b5r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-105") {
        addForeignKeyConstraint(baseColumnNames: "pr_state_fk", baseTableName: "patron_request", constraintName: "FKk4ew47ehoi4whapjey3nyin3h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-106") {
        addForeignKeyConstraint(baseColumnNames: "prr_peer_fk", baseTableName: "patron_request_rota", constraintName: "FKk9y7c5o52xpbwtt7ydd8eajb6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-107") {
        addForeignKeyConstraint(baseColumnNames: "sa_account_holder", baseTableName: "service_account", constraintName: "FKl0sums8w3h2i90a7gudkkvs6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-108") {
        addForeignKeyConstraint(baseColumnNames: "si_shipment_fk", baseTableName: "shipment_item", constraintName: "FKl9tqg5oeklojv0sm755yc8889", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sh_id", referencedTableName: "shipment")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-109") {
        addForeignKeyConstraint(baseColumnNames: "custom_properties_id", baseTableName: "service", constraintName: "FKlcsx75pv26118e28ske0wgft7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-110") {
        addForeignKeyConstraint(baseColumnNames: "sa_service", baseTableName: "service_account", constraintName: "FKlw0rgy9jm8bhf9cn2ok7yr76b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "se_id", referencedTableName: "service")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-111") {
        addForeignKeyConstraint(baseColumnNames: "se_business_function_fk", baseTableName: "service", constraintName: "FKm4goei4gs0kc3o37owkar9qmn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-112") {
        addForeignKeyConstraint(baseColumnNames: "si_patron_request_fk", baseTableName: "shipment_item", constraintName: "FKmtbbcokkut7sm5w8da1gvmmsk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-113") {
        addForeignKeyConstraint(baseColumnNames: "al_type_rv_fk", baseTableName: "address_line", constraintName: "FKnrum0mlrqrdim99tpv2fsrppf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-114") {
        addForeignKeyConstraint(baseColumnNames: "pr_pre_error_status_fk", baseTableName: "patron_request", constraintName: "FKoitf6o8ntq4hh399cujuwhbxv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-115") {
        addForeignKeyConstraint(baseColumnNames: "fa_owner", baseTableName: "friend_assertion", constraintName: "FKq0b79ux6oihg46yoks9vg154c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-116") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "service_tag", constraintName: "FKq56hgx6qad4r28rntiynnitg8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tag")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-117") {
        addForeignKeyConstraint(baseColumnNames: "service_tags_id", baseTableName: "service_tag", constraintName: "FKq58uyhoq6ouyw991t9aps47ka", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "se_id", referencedTableName: "service")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-118") {
        addForeignKeyConstraint(baseColumnNames: "sh_directory_entry_fk", baseTableName: "shipment", constraintName: "FKqxgo143a42gb4njo2f974to9g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-119") {
        addForeignKeyConstraint(baseColumnNames: "address_tags_id", baseTableName: "address_tag", constraintName: "FKsfnxyiyhbwabho720nkg34mjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "addr_id", referencedTableName: "address")
    }

    changeSet(author: "ianibbo (generated)", id: "1567949522352-120") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "directory_entry_tag", constraintName: "FKt8qbn40lvi5a2hi726uqc5igv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tag")
    }

    changeSet(author: "ethanfreestone (manual)", id: "2019-09-11-955-001") {
        addColumn(tableName: "shipment") {
            column(name: "sh_shipping_library_fk", type: "VARCHAR(36)")
            column(name: "sh_receiving_library_fk", type: "VARCHAR(36)")
            column(name: "sh_tracking_number", type: "VARCHAR(36)")
        }
        dropColumn(tableName: "shipment", columnName: "sh_directory_entry_fk")
        addForeignKeyConstraint(baseColumnNames: "sh_shipping_library_fk", baseTableName: "shipment", constraintName: "FK_shipping_library_constraint", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
        addForeignKeyConstraint(baseColumnNames: "sh_receiving_library_fk", baseTableName: "shipment", constraintName: "FK_receiving_library_constraint", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")

    }


    changeSet(author: "ianibbo (manual)", id: "2019-09-13-0906-001") {
        addColumn(tableName: "patron_request_rota") {
            column(name: "prr_instance_identifier", type: "VARCHAR(256)")
            column(name: "prr_copy_identifier", type: "VARCHAR(256)")
        }
    }

    changeSet(author: "ianibbo (manual)", id: "2019-09-19-0940-001") {
        createTable(tableName: "host_lms_location") {
            column(name: "hll_id", type: "VARCHAR(36)") { constraints(nullable: "false") } 
            column(name: "hll_version", type: "BIGINT") { constraints(nullable: "false") }
            column(name: "hll_code", type: "VARCHAR(255)") { constraints(nullable: "false") }
            column(name: "hll_last_completed", type: "BIGINT")
            column(name: "hll_ical_rrule", type: "VARCHAR(255)")
            column(name: "hll_date_created", type: "timestamp")
            column(name: "hll_last_updated", type: "timestamp")

        }
    }

    changeSet(author: "ianibbo (manual)", id: "2019-09-19-0940-003") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_local_call_number", type: "VARCHAR(256)")
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20190921-1002-001") {
        addColumn(tableName: "service_account") {
            column(name: "sa_slug", type: "VARCHAR(255)");
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20190924-0943-001") {
        createTable(tableName: "group_member") {
            column(name: "gm_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "gm_group_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "gm_member_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }

        addPrimaryKey(columnNames: "gm_id", constraintName: "GroupMemberPK", tableName: "group_member")


        addColumn(tableName: "directory_entry") {
            column(name: "de_lms_location_code", type: "VARCHAR(255)");
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20190926-1407-001") {

        addColumn(tableName: "group_member") {
            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "custom_properties_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }

        addColumn(tableName: "directory_entry") {
            column(name: "de_entry_url", type: "VARCHAR(255)")
        }

    }

    changeSet(author: "ianibbo (manual)", id: "20191003-0931-001") {

        addColumn(tableName: "host_lms_location") {
            column(name: "hll_name", type: "VARCHAR(255)")
        }

        addColumn(tableName: "patron_request") {
            column(name:'pr_pick_location_fk', type: "VARCHAR(36)") 
            column(name:'pr_pick_shelving_location', type: "VARCHAR(255)") 
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191004-1035-001") {
        addColumn(tableName: "patron_request") {
            column(name:'pr_sup_inst_symbol', type: "VARCHAR(255)") 
            column(name:'pr_peer_request_identifier', type: "VARCHAR(255)") 
       }
    }

    changeSet(author: "ianibbo (manual)", id: "20191004-1451-001") {
        createTable(tableName: "app_setting") {
            column(name: "st_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "st_version", type: "BIGINT") {
                constraints(nullable: "false")
            }
            column(name: 'st_section', type: "VARCHAR(255)")
            column(name: 'st_key', type: "VARCHAR(255)")
            column(name: 'st_setting_type', type: "VARCHAR(255)")
            column(name: 'st_vocab', type: "VARCHAR(255)")
            column(name: 'st_default_value', type: "VARCHAR(255)")
            column(name: 'st_value', type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191005-1141-001") {
        addColumn(tableName: "patron_request") {
            column(name:'pr_resolved_req_inst_symbol_fk', type: "VARCHAR(36)") 
            column(name:'pr_resolved_sup_inst_symbol_fk', type: "VARCHAR(36)") 
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191006-1440-001") {
        addColumn(tableName: "patron_request_rota") {
            column(name: 'prr_peer_symbol_fk', type: 'VARCHAR(36)')
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191027-1005-001") {
        createTable(tableName: "state_transition") {
            column(name: "str_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "str_version", type: "BIGINT") {
                constraints(nullable: "false")
            }
            column(name: "str_model", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "str_from_state", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "str_action_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191030-1034-001") {
        addColumn(tableName: "status") {
            column(name: 'st_presentation_sequence', type: 'VARCHAR(10)')
            column(name: 'st_visible', type: 'BOOLEAN')
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191105-1113-001") {
        createSequence(sequenceName:'pr_hrid_seq')

        addColumn(tableName: "patron_request") {
            // column(name: 'pr_hrid', type: 'VARCHAR(32)', defaultValueSequenceNext:'pr_hrid_seq');
            column(name: 'pr_hrid', type: 'VARCHAR(32)')
        }

    }

    changeSet(author: "ianibbo (manual)", id: "20191112-1700-001") {
        addColumn(tableName: "patron_request") {
            column(name: 'pr_bib_record', type: 'TEXT')
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191114-1200-001") {
        addColumn(tableName: "patron_request") {
            column(name: 'pr_selected_item_barcode', type: 'VARCHAR(255)')
        }
    }

   changeSet(author: "ianibbo (manual)", id: "20191115-1348-001") {
        addColumn(tableName: "directory_entry") {
            column(name: "de_phone_number", type: "VARCHAR(255)");
            column(name: "de_email_address", type: "VARCHAR(255)");
            column(name: "de_contact_name", type: "VARCHAR(255)");
        }

    }

    changeSet(author: "ianibbo (manual)", id: "20191127-1847-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_patron_email", type: "VARCHAR(255)");
            column(name: "pr_patron_note", type: "VARCHAR(255)");
            column(name: "pr_pref_service_point", type: "VARCHAR(255)");
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20191206-0947-001") {
        addColumn(tableName: "patron_request_rota") {
            column(name: "prr_note", type: "TEXT");
        }
    }

   changeSet(author: "ianibbo (generated)", id: "20191211-1508-001") {
        createTable(tableName: "patron") {
            column(name: "pat_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "pat_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pat_date_created", type: "timestamp")

            column(name: "pat_last_updated", type: "timestamp") {
                constraints(nullable: "true")
            }


            column(name: "pat_host_system_identifier", type: "VARCHAR(255)") {
                constraints(nullable: "true")
            }

            column(name: "pat_given_name", type: "VARCHAR(255)") {
                constraints(nullable: "true")
            }

            column(name: "pat_surame", type: "VARCHAR(255)") {
                constraints(nullable: "true")
            }
        }
    }

   changeSet(author: "ianibbo (generated)", id: "20191211-1519-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_resolved_patron_fk", type: "VARCHAR(36)");
        }
   }

   changeSet(author: "ianibbo (manual)", id: "20200113-0811-001") {
        createTable(tableName: "available_action") {
            column(name: "aa_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "aa_version", type: "BIGINT") {
                constraints(nullable: "false")
            }
            column(name: "aa_model", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "aa_from_state", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "aa_action_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: 'aa_trigger_type', type: "VARCHAR(10)")
            column(name: 'aa_action_type', type: "VARCHAR(10)")
            column(name: 'aa_action_body', type: "TEXT")
        }
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200113-1658-001") {
        createTable(tableName: "patron_request_notification") {
            column(name: "prn_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "prn_version", type: "BIGINT") {
                constraints(nullable: "false")
            }
            column(name: "prn_date_created", type: "timestamp")
            column(name: "prn_last_updated", type: "timestamp")
            column(name: "prn_timestamp", type: "timestamp")
            column(name: "prn_seen", type: "BOOLEAN")
            column(name: "prn_is_sender", type: "BOOLEAN")
            column(name: "prn_message_sender_fk", type: "VARCHAR(36)")
            column(name: "prn_message_receiver_fk", type: "VARCHAR(36)")
            column(name: "prn_message_content", type: "VARCHAR(36)")
            column(name: "prn_patron_request_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200116-09068-001") {
        modifyDataType(
            tableName: "patron_request_notification",
            columnName: "prn_message_content", type: "text",
            newDataType: "text",
            confirm: "successfully updated the prn_message_content column."
        )
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200206-1144-001") {
        addColumn(tableName: "patron_request_notification") {
            column(name: "prn_attached_action", type: "VARCHAR(36)");
        }
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200305-1648-001") {
        addColumn(tableName: "patron_request_notification") {
            column(name: "prn_action_status", type: "VARCHAR(36)");
        }
        addColumn(tableName: "patron_request_notification") {
            column(name: "prn_action_data", type: "VARCHAR(36)");
        }
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200306-1633-001") {
        createTable(tableName: "patron_request_loan_condition") {
            column(name: "prlc_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "prlc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }
            column(name: "prlc_date_created", type: "timestamp")
            column(name: "prlc_last_updated", type: "timestamp")
            column(name: "prlc_code", type: "VARCHAR(36)")
            column(name: "prlc_note", type: "text")
            column(name: "prlc_patron_request_fk", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }
            column(name: "prlc_relevant_supplier_fk", type: "VARCHAR(36)")
        }
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200309-1345-001") {
        validCheckSum ("7:db3c885be8cf39cd82c93f37f8548b3a")
        addColumn(tableName: "patron_request") {
            column(name: "pr_requester_requested_cancellation", type: "BOOLEAN")
        }
        addNotNullConstraint (tableName: "patron_request", columnName: "pr_requester_requested_cancellation", defaultNullValue: false )
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200309-13451602-001") {
        validCheckSum ("7:507c3225d1ed283e90ba7a6a8c3e8d90")
        addColumn(tableName: "patron_request") {
            column(name: "pr_request_to_continue", type: "BOOLEAN") 
        }
        addNotNullConstraint (tableName: "patron_request", columnName: "pr_request_to_continue", defaultNullValue: true )
    }

    changeSet(author: "ethanfreestone (manual)", id: "20200310-0958-001") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_previous_state", type: "VARCHAR(50)") {
            }
        }
    }

    // changeSet(author: "ianibbo (generated)", id: "20200331-1432-01") {
    //     addUniqueConstraint(columnNames: "na_symbol", constraintName: "unique_naming_authority", tableName: "naming_authority")
    //     addUniqueConstraint(columnNames: "norm_value", constraintName: "unique_tag", tableName: "tag")
    // }

    changeSet(author: "ethanfreestone (manual)", id: "202004031014-001") {
        addColumn(tableName: "directory_entry") {
            column(name: "de_type_rv_fk", type: "VARCHAR(36)")
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20200406-0919-001") {
        addColumn(tableName: "directory_entry") {
            column(name: "de_published_last_update", type: "BIGINT");
        }
    }

    changeSet(author: "knordstrom (manual)", id: "20200501-1024-001") {
      addColumn(tableName: "patron_request") {
        column(name: "pr_pref_service_point_code", type: "VARCHAR(255)")
      }
       addColumn(tableName: "patron_request") {
        column(name: "pr_resolved_pickup_location_fk", type: "VARCHAR(36)")
      }
      addForeignKeyConstraint(baseColumnNames: "pr_resolved_pickup_location_fk", baseTableName: "patron_request", constraintName: "FK_pickup_location_constraint", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry")
    }
    
    changeSet(author: "ianibbo (manual)", id: "20200423-1021-001") {
        addColumn(tableName: "patron_request_rota") {
            column(name: "prr_lb_score", type: "BIGINT");
            column(name: "prr_lb_reason", type: "TEXT");
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20200423-1021-002") {
        createTable(tableName: "counter") {
            column(name: "ct_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "ct_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ct_context", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ct_description", type: "VARCHAR(255)")

            column(name: "ct_value", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20200423-1021-003") {
        addColumn(tableName: "patron_request") {
            column(name:"pr_active_loan", type: "BOOLEAN")
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20200518-0800-001") {
        createTable(tableName: "timer") {
            column(name: "tr_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "tr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tr_description", type: "TEXT")
            column(name: "tr_rrule", type: "VARCHAR(255)")
            column(name: "tr_last_exec", type: "BIGINT")
            column(name: "tr_task_code", type: "VARCHAR(255)")
            column(name: "tr_task_config", type: "TEXT")
        }
    }

    changeSet(author: "efreestone (manual)", id: "20200514-1535-001") {
        addColumn(tableName: "address") {
            column(name: "addr_country_code", type: "VARCHAR(15)");
        }
    }

    changeSet(author: "jskomorowski (manual)", id: "20200515-1239-001") {
        addColumn(tableName: "status") {
            column(name: "st_needs_attention", type: "BOOLEAN");
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20200601-0845-001") {
        addColumn(tableName: "patron_request") {
            column(name:"pr_needs_attention", type: "BOOLEAN")
        }
    }

    changeSet(author: "ianibbo (manual)", id: "20200602-0930-001") {
        addColumn(tableName: "timer") {
            column(name:"tr_enabled", type: "BOOLEAN")
        }
    }

    // due date from lms is a string of length 64 in case we get back something not-date-like
    // from the lms, or with additional text
    changeSet(author: "ianibbo (manual)", id: "20200615-0936-001") {
        addColumn(tableName: "patron_request") {
            column(name:"pr_due_date_from_lms", type: "VARCHAR(64)")
        }
    }

    changeSet(author: "efreestone (manual)", id: "20200617-1030-001") {
        addColumn(tableName: "service") {
            column(name: "se_status_fk", type: "VARCHAR(36)");
        }
    }

    changeSet(author: "efreestone (manual)", id: "202006171030-01") {
        addForeignKeyConstraint(baseColumnNames: "se_status_fk", baseTableName: "service", constraintName: "FK-service-status", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

  // Bump to web toolkit 
  changeSet(author: "claudia (manual)", id: "202003191925-2") {
    addColumn(tableName: "refdata_category") {
      column(name: "internal", type: "boolean")
    }
    addNotNullConstraint (tableName: "refdata_category", columnName: "internal", defaultNullValue: false)
  }

  changeSet(author: "claudia (manual)", id: "202003191925-3") {
    grailsChange {
      change {
        sql.execute("""
          UPDATE ${database.defaultSchemaName}.refdata_category SET internal = true
        """.toString())
      }
    }
  }

  changeSet(author: "efreestone (manual)", id: "202008111728-001") {
    createTable(tableName: "patron_request_previous_states") {
      column(name: "pr_previous_states", type: "VARCHAR(255)")

      column(name: "previous_states_object", type: "VARCHAR(255)")

      column(name: "previous_states_idx", type: "VARCHAR(255)")

      column(name: "previous_states_elt", type: "VARCHAR(255)") {
        constraints(nullable: "false")
      }
    }
  }

  changeSet(author: "efreestone (manual)", id: "202008111728-002") {
    dropColumn(tableName: "patron_request", columnName: "pr_previous_state")
  }
  
  changeSet(author: "knordstrom (manual)", id: "20200821-1402-001") {
     addColumn(tableName:"patron_request") {
       column(name: "pr_parsed_due_date_lms", type: "timestamp")
       column(name: "pr_due_date_rs", type: "VARCHAR(64)")
       column(name: "pr_parsed_due_date_rs", type: "timestamp")
       column(name: "pr_overdue", type: "BOOLEAN")
     }
  }

  changeSet(author: "efreestone (manual)", id: "202008281228-002") {
    addColumn(tableName: "patron_request_loan_condition") {
        column(name: "prlc_accepted", type: "BOOLEAN");
    }
  }

  changeSet(author: "jskomorowski (manual)", id: "202008281440-101") {
      createTable(tableName: "notice_policy") {
          column(name: "np_id", type: "VARCHAR(36)") {
              constraints(nullable: "false")
          }

          column(name: "np_version", type: "BIGINT") {
              constraints(nullable: "false")
          }

          column(name: "np_date_created", type: "timestamp")

          column(name: "np_last_updated", type: "timestamp")

          column(name: "np_name", type: "VARCHAR(255)")

          column(name: "np_description", type: "TEXT")

          column(name: "np_active", type: "BOOLEAN")
      }
  }

  changeSet(author: "jskomorowski (manual)", id: "202008281440-102") {
      createTable(tableName: "notice_policy_notice") {
          column(name: "npn_id", type: "VARCHAR(36)") {
              constraints(nullable: "false")
          }

          column(name: "npn_version", type: "BIGINT") {
              constraints(nullable: "false")
          }

          column(name: "npn_date_created", type: "timestamp")

          column(name: "npn_last_updated", type: "timestamp")

          column(name: "npn_template", type: "VARCHAR(36)") {
              constraints(nullable: "false")
          }

          column(name: "npn_real_time", type: "BOOLEAN")

          column(name: "npn_notice_policy_fk", type: "VARCHAR(36)") {
              constraints(nullable: "false")
          }
      }
  } 

  changeSet(author: "jskomorowski (manual)", id: "202009281618-000") {
    addColumn(tableName: "notice_policy_notice") {
      column(name: "npn_format_fk", type: "VARCHAR(36)") {
        constraints(nullable: "false")
      }
      column(name: "npn_trigger_fk", type: "VARCHAR(36)") {
        constraints(nullable: "false")
      }
    }
  }
}
