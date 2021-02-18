databaseChangeLog = {
  changeSet(author: "jskomorowski (manual)", id: "202010302233-001") {
   addPrimaryKey(columnNames: "st_id", constraintName: "app_settingPK", tableName: "app_setting")
   addPrimaryKey(columnNames: "aa_id", constraintName: "available_actionPK", tableName: "available_action")
   addPrimaryKey(columnNames: "ct_id", constraintName: "counterPK", tableName: "counter")
   addPrimaryKey(columnNames: "hll_id", constraintName: "host_lms_locationPK", tableName: "host_lms_location")
   addPrimaryKey(columnNames: "np_id", constraintName: "notice_policyPK", tableName: "notice_policy")
   addPrimaryKey(columnNames: "npn_id", constraintName: "notice_policy_noticePK", tableName: "notice_policy_notice")
   addPrimaryKey(columnNames: "pat_id", constraintName: "patronPK", tableName: "patron")
   addPrimaryKey(columnNames: "prlc_id", constraintName: "patron_request_loan_conditionPK", tableName: "patron_request_loan_condition")
   addPrimaryKey(columnNames: "prn_id", constraintName: "patron_request_notificationPK", tableName: "patron_request_notification")
   addPrimaryKey(columnNames: "str_id", constraintName: "state_transitionPK", tableName: "state_transition")
   addPrimaryKey(columnNames: "id", constraintName: "tenant_changelogPK", tableName: "tenant_changelog")
   addPrimaryKey(columnNames: "tr_id", constraintName: "timerPK", tableName: "timer")
   addPrimaryKey(columnNames: "address_tags_id, tag_id", constraintName: "address_tagPK", tableName: "address_tag")
   addPrimaryKey(columnNames: "directory_entry_tags_id, tag_id", constraintName: "directory_entry_tagPK", tableName: "directory_entry_tag")
   addPrimaryKey(columnNames: "service_tags_id, tag_id", constraintName: "service_tagPK", tableName: "service_tag")
   addPrimaryKey(columnNames: "patron_request_tags_id, tag_id", constraintName: "patron_request_tagPK", tableName: "patron_request_tag")
   addPrimaryKey(columnNames: "pr_previous_states, previous_states_idx", constraintName: "patron_request_previous_statesPK", tableName: "patron_request_previous_states")
  }

  changeSet(author: "efreestone (manual)", id: "20201201-1135-001") {
    addColumn(tableName: "patron_request") {
      column(name:'pr_oclc_number', type: "VARCHAR(255)")
    }
  }

  changeSet(author: "efreestone (manual)", id: "20201210-1026-001") {
    modifyDataType(
      tableName: "app_setting",
      columnName: "st_value", type: "text",
      newDataType: "text",
      confirm: "successfully updated the st_value column."
    )
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-001") {
    createTable(tableName: "template_container") {
      column(name: "tmc_id", type: "VARCHAR(36)") {
        constraints(nullable: "false")
      }
      column(name: "tmc_version", type: "BIGINT") {
        constraints(nullable: "false")
      }
      column(name: "tmc_name", type: "VARCHAR(255)")
      column(name: "tmc_template_resolver", type: "VARCHAR(36)")
      column(name: "tmc_description", type: "VARCHAR(255)")
      column(name: "tmc_date_created", type: "timestamp")
      column(name: "tmc_last_updated", type: "timestamp")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-002") {
    addPrimaryKey(columnNames: "tmc_id", constraintName: "template_containerPK", tableName: "template_container")
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-003") {
    createTable(tableName: "localized_template") {
      column(name: "ltm_id", type: "VARCHAR(36)") {
        constraints(nullable: "false")
      }
      column(name: "ltm_version", type: "BIGINT") {
        constraints(nullable: "false")
      }
      column(name: "ltm_locality", type: "VARCHAR(36)")
      column(name: "ltm_owner_fk", type: "VARCHAR(36)")
      column(name: "ltm_template_fk", type: "VARCHAR(36)")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-004") {
    addPrimaryKey(columnNames: "ltm_id", constraintName: "localizedTemplatePK", tableName: "localized_template")
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-005") {
    createTable(tableName: "template") {
      column(name: "tm_id", type: "VARCHAR(36)") {
        constraints(nullable: "false")
      }
      column(name: "tm_version", type: "BIGINT") {
        constraints(nullable: "false")
      }
      column(name: "tm_header", type: "VARCHAR(255)")
      column(name: "tm_template_body", type: "text")
    }
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-006") {
    addPrimaryKey(columnNames: "tm_id", constraintName: "templatePK", tableName: "template")
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-007") {
    addForeignKeyConstraint(baseColumnNames: "ltm_owner_fk", baseTableName: "localized_template", constraintName: "localized_template_template_containerFK", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tmc_id", referencedTableName: "template_container")
    addForeignKeyConstraint(baseColumnNames: "ltm_template_fk", baseTableName: "localized_template", constraintName: "localized_template_templateFK", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tm_id", referencedTableName: "template")
  }

  changeSet(author: "efreestone (manual)", id: "202101281128-008") {
    addForeignKeyConstraint(baseColumnNames: "tmc_template_resolver", baseTableName: "template_container", constraintName: "tmc_template_container_template_resolverFK", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
  }

  // We need to blow away the existing notice policy notices as part of this template switchover
  changeSet(author: "efreestone (manual)", id: "2021-02-04-1522-001") {
    grailsChange {
      change {
        sql.execute("""
          DELETE FROM ${database.defaultSchemaName}.notice_policy_notice
        """.toString())
      }
    }

    grailsChange {
      change {
        sql.execute("""
          DELETE FROM ${database.defaultSchemaName}.notice_policy
        """.toString())
      }
    }

    addColumn(tableName: "notice_policy_notice") {
      column(name: "npn_template_fk", type: "VARCHAR(36)") {
        constraints(nullable: "false")
      }
    }
    addForeignKeyConstraint(baseColumnNames: "npn_template_fk", baseTableName: "notice_policy_notice", constraintName: "notice_policy_notice_templateFK", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tmc_id", referencedTableName: "template_container")
    dropColumn(tableName: "notice_policy_notice", columnName: "npn_template")
  }

  changeSet(author: "efreestone (manual)", id: "2021-02-05-1219-001") {
    addColumn(tableName: "template_container") {
      column(name: "tmc_context", type: "VARCHAR(255)")
    }
  }
  
}
