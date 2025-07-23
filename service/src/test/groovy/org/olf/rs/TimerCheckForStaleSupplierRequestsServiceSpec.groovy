package org.olf.rs.timers

import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import org.dmfs.rfc5545.DateTime
import spock.lang.Specification

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

@Slf4j
class TimerCheckForStaleSupplierRequestsServiceSpec extends Specification implements ServiceUnitTest<TimerCheckForStaleSupplierRequestsService> {
    def "testing date calculation method" (String startDateString, int numberOfHours, String endDateString, boolean excludeWeekends) {
        when:
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");
        ZonedDateTime zdt = ZonedDateTime.parse(startDateString, formatter);
        log.debug("Got time ${zdt}")
        Long millis = zdt.toInstant().toEpochMilli();
        DateTime startDateTime = (new DateTime(TimeZone.getTimeZone(AbstractTimer.TIME_ZONE_UTC), millis));
        DateTime dt = service.getAdjustedDateTime(numberOfHours, startDateTime, excludeWeekends);
        then:
        assert(dt.toString() == endDateString)
        where:
        startDateString    | numberOfHours | endDateString      | excludeWeekends
        "20250721T083500Z" | 120           | "20250714T083500Z" | true
        "20250721T083500Z" | 120           | "20250716T083500Z" | false
        "20250721T083500Z" | 122           | "20250714T063500Z" | true
        "20250721T083500Z" | 122           | "20250716T063500Z" | false
    }
}
