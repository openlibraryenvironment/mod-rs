package org.olf.rs

import org.olf.okapi.modules.directory.DirectoryEntry;

import grails.gorm.MultiTenant;

/**
 *
 *
 *
 * Note - because the domain language used here is god-awful. A HostLMSLocation is a record in an institutions library management system that
 * represents "A location" - because there isn't a good definition this can mean different things in different LMS systems. For our purposes
 * think of a "Location" as "A Building".
 *
 * Libraries also have "Shelving Locations" - "The Stacks (A shelving location) at the St Georges Library (A location)"
 *
 * In SOME LMS systems the shelving locations are distributed over the locations - so we can have Stacks at Location A B and C and
 * "Automated Storage" at location A and D. This gives us a M:N model Location -< JOIN >- ShelvingLocation. in other systems there is a more
 * simple Location -< ShelvingLocation setup.
 *
 * We model the more complex situation here and have to live with the extra complexity for the simple model. Our model is
 *   HOSTLMSLocation -1:M-< ShelvingLocationSite >-N:1- HostLMSShelvingLocation.
 *
 * In instance terms
 *   "St Georges Library" -<   "The Stacks at St Georges Library"   >-   "The Stacks"
 *
 * This allows us to state preferences at each level and override as needed.
 *
 * In general
 *     The HostLMSShelvingLocation is the default         - We will not lend ILL from <ShelvingLocation>"Reserves"
 *                                                        - We will lend ILL from <ShelvingLocation> "Stacks"
 *                                                        - We will not lend ILL from <ShelvingLocation> "Oversized"
 *
 *       The HostLMSLocation can override a shelving loc  - <Location>St Georges Library is flooded - by default no lending at all from here at the moment
 *
 *         ShelvingLocationSite can override that         - <Location>St Georges Library <ShelvingLocation>Annex is not flooded and can lend anyway (Override)
 *
 */
class HostLMSLocation implements MultiTenant<HostLMSLocation> {

  String id
  String code
  String name

  // The iCal recurrence rule (https://www.kanzaki.com/docs/ical/rrule.html) that defines how
  // often pull slip batching happens for this location.
  String icalRrule

  // The datetime the icalRule last matched a new date time
  // considering https://github.com/dmfs/lib-recur
  // and https://github.com/dlemmermann/google-rfc-2445 (see also https://www.programcreek.com/java-api-examples/index.php?api=com.google.ical.values.RRule)
  // My intention is to store the time of last run, then use an iterator on that time to work out the next (as per google example above)
  // event. If now() > that next event time, then the event needs to run.
  // note: https://javalibs.com/artifact/org.scala-saddle/google-rfc-2445
  Long lastCompleted

  Date dateCreated
  Date lastUpdated

  // < 0 - Never use
  // 0 - No preference / default
  // > 0 - Preference order
  Long supplyPreference

  /** The hidden field if set to true, means they have tried to delete it but it is still linked to another record, so we just mark it as hidden */
  Boolean hidden;

  static hasMany = [
    sites : ShelvingLocationSite,
  ]

  static mappedBy = [
    sites: 'location'
  ]


  DirectoryEntry correspondingDirectoryEntry

  static constraints = {
    code (nullable: false)
    name (nullable: true)
    icalRrule (nullable: true)
    lastCompleted (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    supplyPreference (nullable: true)
    correspondingDirectoryEntry (nullable: true)
    hidden (nullable: true)
  }

  static mapping = {
    table 'host_lms_location'
                               id column : 'hll_id', generator: 'uuid2', length:36
                          version column : 'hll_version'
                             code column : 'hll_code'
                             name column : 'hll_name'
                        icalRrule column : 'hll_ical_rrule'
                    lastCompleted column : 'hll_last_completed'
                      dateCreated column : 'hll_date_created'
                      lastUpdated column : 'hll_last_updated'
                 supplyPreference column : 'hll_supply_preference'
      correspondingDirectoryEntry column : 'hll_corresponding_de'
                           hidden column : 'hll_hidden', defaulValue: false
  }

  public String toString() {
    return "HostLMSLocation: ${code}".toString()
  }

    def canDelete() {
        def deleteValid = true;
        def prs = [];
        String error;
        PatronRequest.withSession {
            def patronRequestsUsingThisLocation = PatronRequest.executeQuery("""
                SELECT id FROM PatronRequest AS pr WHERE pr.pickLocation.id = :hostLMSId
                """.toString(), [hostLMSId: id]);

            if (patronRequestsUsingThisLocation.size() > 0) {
                deleteValid = false;
                error = 'There are ' + patronRequestsUsingThisLocation.size().toString() + ' associated with this location';
                prs = patronRequestsUsingThisLocation;

            } else {
                // Is there an associated location site pointing at this record
                long numberShelvingLocationSites = ShelvingLocationSite.countByLocation(this);
                if (numberShelvingLocationSites > 0) {
                    // There is at least 1 site that has this location associated with it
                    deleteValid = false;
                    error = "There are " + numberShelvingLocationSites.toString() + " location sites attached to this location";
                }
            }
        }

        return([
            deleteValid: deleteValid,
            prs: prs,
            error: error
        ]);
    }

    def beforeDelete() {
        return(canDelete().deleteValid);
    }

    static HostLMSLocation EnsureActive(String code, String name) {
        HostLMSLocation loc = HostLMSLocation.findByCodeOrName(code, name);

        // Did we find a location
        if (loc == null) {
            // We do not so create a new one
            loc = new HostLMSLocation(
                code : code,
                name : name,
                icalRrule :'RRULE:FREQ=MINUTELY;INTERVAL=10;WKST=MO'
            );
            loc.save(flush : true, failOnError : true);
        }  else if (loc.hidden == true) {
            // It is hidden, so unhide it
            loc.hidden = false;
            loc.save(flush : true, failOnError : true);
        }
        return(loc);
    }
}

