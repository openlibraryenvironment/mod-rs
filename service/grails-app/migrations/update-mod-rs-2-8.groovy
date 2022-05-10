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

    changeSet(author: "Chas (generated)", id: "1649156404150-1") {
        // This migration is to bring the auto generated changes in sync with the code, except for the drops, as these may not have been done on purpose
        // Create table file_object
        createTable(tableName: "file_object") {
            column(name: "fo_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "file_objectPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "fo_s3ref", type: "VARCHAR(255)")

            column(name: "file_contents", type: "OID")
        }

        // Create table file_upload
        createTable(tableName: "file_upload") {
            column(name: "fu_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "file_uploadPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fu_filesize", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fu_last_mod", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "file_content_type", type: "VARCHAR(255)")

            column(name: "fu_owner", type: "VARCHAR(36)")

            column(name: "file_object_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "fu_filename", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }

        // Create table single_file_attachment
        createTable(tableName: "single_file_attachment") {
            column(name: "id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "single_file_attachmentPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }


        // Amendments to action_event_result
        dropNotNullConstraint(columnDataType: "varchar(36)", columnName: "aer_next_action_event", tableName: "action_event_result")

        // Amendments to address
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "addr_country_code", tableName: "address", validate: "true")

        dropNotNullConstraint(columnDataType: "varchar(36)", columnName: "owner_id", tableName: "address")

        // Amendments to announcement
        dropNotNullConstraint(columnDataType: "varchar(36)", columnName: "ann_owner_fk", tableName: "announcement")

        // Amendments to app_setting
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "st_key", tableName: "app_setting", validate: "true")

        // Amendments to counter
        addUniqueConstraint(columnNames: "ct_context", constraintName: "UC_COUNTERCT_CONTEXT_COL", tableName: "counter")

        // Amendments to directory_entry
        addUniqueConstraint(columnNames: "de_slug", constraintName: "UC_DIRECTORY_ENTRYDE_SLUG_COL", tableName: "directory_entry")

        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "de_slug", tableName: "directory_entry", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "de_type_rv_fk", baseTableName: "directory_entry", constraintName: "FKp9dfcluxbguwcd8nh88rgdm1e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")

        // Amendments to file_upload
        addForeignKeyConstraint(baseColumnNames: "fu_owner", baseTableName: "file_upload", constraintName: "FK2kjo91mrq9mt35oo8a94c0p5o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "single_file_attachment", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "file_object_id", baseTableName: "file_upload", constraintName: "FKmu4soh4lkppq4a7llu3k7yfqq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "fo_id", referencedTableName: "file_object", validate: "true")

        // Foreign keys for group_member
        addForeignKeyConstraint(baseColumnNames: "custom_properties_id", baseTableName: "group_member", constraintName: "FKdeoqlneol2the2ewsx6nvhafg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "custom_property_container", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "gm_group_fk", baseTableName: "group_member", constraintName: "FKi9k8olfysurk0c24b3y4yia6h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "gm_member_fk", baseTableName: "group_member", constraintName: "FKj7reocypu9lgjaa2kgyc3qv8t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry", validate: "true")

        // Amendments to host_lms_location
        addForeignKeyConstraint(baseColumnNames: "hll_corresponding_de", baseTableName: "host_lms_location", constraintName: "FK9qsslbormfhqfbv8heftfiaaa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "de_id", referencedTableName: "directory_entry", validate: "true")

        // Amendments to host_lms_patron_profile
        addPrimaryKey(columnNames: "hlpp_id", constraintName: "host_lms_patron_profilePK", tableName: "host_lms_patron_profile")

        addNotNullConstraint(columnDataType: "bigint", columnName: "hlpp_version", tableName: "host_lms_patron_profile", validate: "true")

        // Amendments to host_lms_shelving_loc
        addPrimaryKey(columnNames: "hlsl_id", constraintName: "host_lms_shelving_locPK", tableName: "host_lms_shelving_loc")

        addNotNullConstraint(columnDataType: "bigint", columnName: "hlsl_version", tableName: "host_lms_shelving_loc", validate: "true")

        // Amendments to localized_template
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "ltm_locality", tableName: "localized_template", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(36)", columnName: "ltm_owner_fk", tableName: "localized_template", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(36)", columnName: "ltm_template_fk", tableName: "localized_template", validate: "true")

        // Amendments to naming_authority
        addUniqueConstraint(columnNames: "na_symbol", constraintName: "UC_NAMING_AUTHORITYNA_SYMBOL_COL", tableName: "naming_authority")

        // Amendments to notice_event
        addForeignKeyConstraint(baseColumnNames: "ne_patron_request_fk", baseTableName: "notice_event", constraintName: "FKa5k6rycb3krngfgwjy86s2c1f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "ne_trigger_fk", baseTableName: "notice_event", constraintName: "FKistverq395h96epcfi3bx3gn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")

        // Amendments to notice_policy
        addNotNullConstraint(columnDataType: "boolean", columnName: "np_active", tableName: "notice_policy", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "np_name", tableName: "notice_policy", validate: "true")

        // Amendments to notice_policy_notice
        addNotNullConstraint(columnDataType: "boolean", columnName: "npn_real_time", tableName: "notice_policy_notice", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "npn_notice_policy_fk", baseTableName: "notice_policy_notice", constraintName: "FKb6cynbmendwmylxh55js5p1mg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "np_id", referencedTableName: "notice_policy", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "npn_trigger_fk", baseTableName: "notice_policy_notice", constraintName: "FKgswrmju1oks3eff8lde73p91i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "npn_format_fk", baseTableName: "notice_policy_notice", constraintName: "FKkn5puvv3qw3pjgaanvl4nmpjq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")

        // Amendments to patron
        addUniqueConstraint(columnNames: "pat_host_system_identifier", constraintName: "UC_PATRONPAT_HOST_SYSTEM_IDENTIFIER_COL", tableName: "patron")

        addNotNullConstraint(columnDataType: "timestamp", columnName: "pat_date_created", tableName: "patron", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "pat_host_system_identifier", tableName: "patron", validate: "true")

        addNotNullConstraint(columnDataType: "timestamp", columnName: "pat_last_updated", tableName: "patron", validate: "true")

        // Amendments to patron_request
        addForeignKeyConstraint(baseColumnNames: "pr_pick_location_fk", baseTableName: "patron_request", constraintName: "FK4889ia61wssvlcw0xvxmklyce", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "hll_id", referencedTableName: "host_lms_location", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "pr_cancellation_reason_fk", baseTableName: "patron_request", constraintName: "FK4aw5fit9g92h9aa8m1lrstbxe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "pr_resolved_req_inst_symbol_fk", baseTableName: "patron_request", constraintName: "FK61gadu70wevdeymw1j6el9919", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sym_id", referencedTableName: "symbol", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "pr_resolved_patron_fk", baseTableName: "patron_request", constraintName: "FK6j9vuo69m5buh8afkcvsoga4o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pat_id", referencedTableName: "patron", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "pr_resolved_sup_inst_symbol_fk", baseTableName: "patron_request", constraintName: "FK99gxpx2u44f6gph56epet7s03", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sym_id", referencedTableName: "symbol", validate: "true")

        // Amendments to patron_request_audit
        dropNotNullConstraint(columnDataType: "timestamp", columnName: "pra_date_created", tableName: "patron_request_audit")

        // Amendments to patron_request_loan_condition
        dropNotNullConstraint(columnDataType: "varchar(36)", columnName: "prlc_patron_request_fk", tableName: "patron_request_loan_condition")

        addForeignKeyConstraint(baseColumnNames: "prlc_relevant_supplier_fk", baseTableName: "patron_request_loan_condition", constraintName: "FKbqdtif8jijys5hev8xsfx6hty", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sym_id", referencedTableName: "symbol", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "prlc_patron_request_fk", baseTableName: "patron_request_loan_condition", constraintName: "FKqr44bb8anxc8t7q99qlb4ieyk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request", validate: "true")

        // Amendments to patron_request_notification
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "prn_message_content", tableName: "patron_request_notification", validate: "true")

        dropNotNullConstraint(columnDataType: "varchar(36)", columnName: "prn_patron_request_fk", tableName: "patron_request_notification")

        addForeignKeyConstraint(baseColumnNames: "prn_message_sender_fk", baseTableName: "patron_request_notification", constraintName: "FK8griyr6wktlurvthfi2vv8yxp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sym_id", referencedTableName: "symbol", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "prn_patron_request_fk", baseTableName: "patron_request_notification", constraintName: "FKaf2coeimmt1qlwfc6sr650bft", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "prn_message_receiver_fk", baseTableName: "patron_request_notification", constraintName: "FKc3he923ia92chiyb5qhj35btk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sym_id", referencedTableName: "symbol", validate: "true")

        // Foreign keys for patron_request_rota
        addForeignKeyConstraint(baseColumnNames: "prr_peer_symbol_fk", baseTableName: "patron_request_rota", constraintName: "FKdhhowd9sytgx46jxs9gka8dyq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sym_id", referencedTableName: "symbol", validate: "true")

        dropForeignKeyConstraint(baseTableName: "patron_request_rota", constraintName: "FKk9y7c5o52xpbwtt7ydd8eajb6")

        // Amendments to predefined_id
        addPrimaryKey(columnNames: "pi_id", constraintName: "predefined_idPK", tableName: "predefined_id")

        // Amendments to request_volume
        addNotNullConstraint(columnDataType: "varchar(36)", columnName: "rv_status_fk", tableName: "request_volume", validate: "true")

        // Amendments to service_account
        addUniqueConstraint(columnNames: "sa_slug", constraintName: "UC_SERVICE_ACCOUNTSA_SLUG_COL", tableName: "service_account")

        // Amendments to shelving_loc_site
        addPrimaryKey(columnNames: "sls_id", constraintName: "shelving_loc_sitePK", tableName: "shelving_loc_site")

        addForeignKeyConstraint(baseColumnNames: "sls_location_fk", baseTableName: "shelving_loc_site", constraintName: "FKntwihfkoewgyfj41pf3pw2bs7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "hll_id", referencedTableName: "host_lms_location", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "sls_shelving_loc_fk", baseTableName: "shelving_loc_site", constraintName: "FKsud5a7xwxnfyyo7am41ytqd99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "hlsl_id", referencedTableName: "host_lms_shelving_loc", validate: "true")

        // Amendments to service_account
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "sa_slug", tableName: "service_account", validate: "true")

        // Amendments to shelving_loc_site
        addNotNullConstraint(columnDataType: "timestamp", columnName: "sls_date_created", tableName: "shelving_loc_site", validate: "true")

        addNotNullConstraint(columnDataType: "timestamp", columnName: "sls_last_updated", tableName: "shelving_loc_site", validate: "true")

        addNotNullConstraint(columnDataType: "bigint", columnName: "sls_version", tableName: "shelving_loc_site", validate: "true")

        // Amendments to state_transition
        addForeignKeyConstraint(baseColumnNames: "str_from_state", baseTableName: "state_transition", constraintName: "FKa26org1dw9jk9j0wm4t22lvhl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "str_model", baseTableName: "state_transition", constraintName: "FKrswurmyf6ggmbfeu481cxk34x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sm_id", referencedTableName: "state_model", validate: "true")

        // Foreign keys for status_tag
        addForeignKeyConstraint(baseColumnNames: "status_tags_id", baseTableName: "status_tag", constraintName: "FKb2kv85cc9297rvrtdy5wswfar", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "st_id", referencedTableName: "status", validate: "true")

        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "status_tag", constraintName: "FKd6kycny7g016s74kwj4j4fwc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tag", validate: "true")

        // Amendments to template
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "tm_header", tableName: "template", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "tm_template_body", tableName: "template", validate: "true")

        // Amendments to template_container
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "tmc_context", tableName: "template_container", validate: "true")

        addNotNullConstraint(columnDataType: "timestamp", columnName: "tmc_date_created", tableName: "template_container", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "tmc_description", tableName: "template_container", validate: "true")

        addNotNullConstraint(columnDataType: "timestamp", columnName: "tmc_last_updated", tableName: "template_container", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "tmc_name", tableName: "template_container", validate: "true")

        addNotNullConstraint(columnDataType: "varchar(36)", columnName: "tmc_template_resolver", tableName: "template_container", validate: "true")

        // Amendments to timer
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "tr_task_code", tableName: "timer", validate: "true")
    }

    changeSet(author: "Chas (generated)", id: "1649337411753-1") {
        // Create the request_identifier table
        createTable(tableName: "request_identifier") {
            column(name: "ri_id", type: "VARCHAR(36)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "request_identifierPK")
            }

            column(name: "ri_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ri_identifier_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ri_patron_request", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "ri_identifier", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }

        // The foreign key constraint for the request_identifier table
        addForeignKeyConstraint(baseColumnNames: "ri_patron_request", baseTableName: "request_identifier", constraintName: "FK7n0txdwj2oeqa77ksiharg86k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "patron_request", validate: "true")
    }

    changeSet(author: "Chas (generated)", id: "1649670543538-1") {
        addColumn(tableName: "patron_request") {
            column(name: "pr_last_protocol_action", type: "varchar(32)")
        }

        addColumn(tableName: "patron_request") {
            column(name: "pr_last_send_attempt", type: "timestamp")
        }
        addColumn(tableName: "patron_request") {
            column(name: "pr_last_sequence_received", type: "int4")
        }

        addColumn(tableName: "patron_request") {
            column(name: "pr_last_sequence_sent", type: "int4")
        }

        addColumn(tableName: "patron_request") {
            column(name: "pr_network_status", type: "varchar(32)")
        }

        addColumn(tableName: "patron_request") {
            column(name: "pr_next_send_attempt", type: "timestamp")
        }

        addColumn(tableName: "patron_request") {
            column(name: "pr_number_of_send_attempts", type: "int4")
        }
    }

    changeSet(author: "Chas (generated)", id: "1649859559363-1") {
        // Slight change in plan, no longer need the action but the event data
        dropColumn(columnName: "pr_last_protocol_action", tableName: "patron_request")

        // This column will only be used i fthere is a network problem, once it has been resolved the data will be cleared
        addColumn(tableName: "patron_request") {
            column(name: "pr_last_protocol_data", type: "varchar(20000)")
        }
    }

    changeSet(author: "jskomorowski (manual)", id: "20220510-1100-001") {
        addNotNullConstraint(tableName: "status", columnName: "st_terminal", defaultNullValue: false)
    }
}
