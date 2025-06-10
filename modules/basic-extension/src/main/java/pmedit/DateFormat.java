package pmedit;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class DateFormat {

    private static final DateTimeFormatter[] isoDateFormat = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE,
    };

    private static final DateTimeFormatter isoDateFormatForPath = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static Calendar parseDateOrNull(String value) {
        try {
            return parseDate(value);
        } catch (InvalidValue e) {
            return null;
        }
    }
    public static Calendar parseDate(String dateTimeString) throws InvalidValue {
        return parseDate(dateTimeString, ZoneId.systemDefault());
    }

    public static Calendar parseDate(String dateTimeString, ZoneId defaultZone) throws InvalidValue {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return  null;
        }

        try {
            // Try multiple formatters in order of specificity
            TemporalAccessor temporal = parseWithMultipleFormatters(dateTimeString.trim());
            Instant instant = buildInstantWithDefaults(temporal, defaultZone);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(instant.toEpochMilli());
            return calendar;

        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse date-time string: " + dateTimeString, e);
        }
    }

    public static String formatDateTime(Calendar calendar) {
        if(calendar == null){
            return null;
        }
        ZonedDateTime zdt = calendar.toInstant().atZone(calendar.getTimeZone().toZoneId());
        return zdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String formatDateTimeFull(Calendar calendar) {
        if(calendar == null){
            return null;
        }
        ZonedDateTime zdt = calendar.toInstant().atZone(calendar.getTimeZone().toZoneId());
        return zdt.format(DateTimeFormatter.ISO_INSTANT);    }

    public static List<String> formatDateTimeFull(List<Calendar> cal) {
        return cal.stream().map(DateFormat::formatDateTimeFull).toList();
    }

    public static String formatDateTimeForPath(Calendar cal) {
        return cal != null? isoDateFormatForPath.format(LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId())): null;
    }

    private static TemporalAccessor parseWithMultipleFormatters(String dateTimeString) throws InvalidValue {

        for (DateTimeFormatter formatter : isoDateFormat) {
            try {
                ParsePosition pos = new ParsePosition(0);
                TemporalAccessor ta = formatter.parse(dateTimeString, pos);
                if(pos.getIndex() == dateTimeString.length()){
                    return ta;
                }
            } catch (Exception ignored) {
                // Try next formatter
            }
        }

        throw new InvalidValue("Unable to parse date/time with any known format: " + dateTimeString);
    }

    private static Instant buildInstantWithDefaults(TemporalAccessor temporal, ZoneId defaultZone) {
        // Direct conversion if possible
        if (temporal instanceof Instant) {
            return (Instant) temporal;
        }
        if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal).toInstant();
        }
        if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).toInstant();
        }

        // Query for components
        LocalDate date = temporal.query(TemporalQueries.localDate());
        LocalTime time = temporal.query(TemporalQueries.localTime());
        ZoneOffset offset = temporal.query(TemporalQueries.offset());

        // Build complete LocalDateTime with defaults
        LocalDate finalDate = date != null ? date : LocalDate.now();
        LocalTime finalTime = time != null ? time : LocalTime.MIDNIGHT; // 00:00:00

        LocalDateTime localDateTime = LocalDateTime.of(finalDate, finalTime);

        // Apply timezone/offset
        if (offset != null) {
            return OffsetDateTime.of(localDateTime, offset).toInstant();
        } else {
            return localDateTime.atZone(defaultZone).toInstant();
        }
    }
}
