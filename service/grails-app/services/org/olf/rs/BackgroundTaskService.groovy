package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import org.olf.rs.HostLMSLocation 

import org.olf.okapi.modules.directory.Symbol;

/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class BackgroundTaskService {

  def performReshareTasks(String tenant) {
    log.debug("performReshareTasks(${tenant})");
    Tenants.withId(tenant) {
      checkPullSlips();

      def sl = Symbol.list();

      log.debug("Currently ${sl.size()} symbols in the system");
      sl.each { sym ->
        log.debug("symbol ${sym.id}: ${sym.authority.symbol}:${sym.symbol}");
      }
    }
    log.debug("BackgroundTaskService::performReshareTasks exiting");
  }

  private void checkPullSlips() {
    log.debug("checkPullSlips()");
    HostLMSLocation.list().each { loc ->
      log.debug("Check pull slips fro ${loc}");
      checkPullSlipsFor(loc.code);
    }
  }

  private void checkPullSlipsFor(String location) {
    log.debug("checkPullSlipsFor(${location})");
  }
}
