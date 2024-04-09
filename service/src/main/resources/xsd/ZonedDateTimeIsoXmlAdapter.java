package org.olf.rs.adapter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * {@code XmlAdapter} mapping JSR-310 {@code ZonedDateTime} to ISO-8601 string
 * <p>
 * String format details: {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE_TIME}
 *
 * @see javax.xml.bind.annotation.adapters.XmlAdapter
 * @see java.time.ZonedDateTime
 */
public class ZonedDateTimeIsoXmlAdapter extends io.github.threetenjaxb.core.TemporalAccessorXmlAdapter<ZonedDateTime> {
    public ZonedDateTimeIsoXmlAdapter() {
        super(DateTimeFormatter.ISO_OFFSET_DATE_TIME, ZonedDateTime::from);
    }
}