package org.olf.rs;

import grails.gorm.multitenancy.Tenants

/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class BackgroundTaskService {

  def performReshareTasks(String tenant) {
    log.debug("performReshareTasks(${tenant})");
    Tenants.withId(tenant) {
      checkPullSlips();
    }
  }

  private void checkPullSlips() {
    log.debug("checkPullSlips()");
  }
}
