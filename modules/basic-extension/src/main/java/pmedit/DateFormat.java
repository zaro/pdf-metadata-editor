package pmedit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

public class DateFormat {

    private static final SimpleDateFormat[] isoDateFormat = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd"),
    };

    private static final SimpleDateFormat isoDateFormatForPath = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public static Calendar parseDateOrNull(String value) {
        try {
            return parseDate(value);
        } catch (InvalidValue e) {
            return null;
        }
    }

    public static Calendar parseDate(String value) throws InvalidValue {
        Date d = null;
        try {
            TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(value);
            Instant i = Instant.from(ta);
            d = Date.from(i);
        } catch (DateTimeParseException e){

            for (SimpleDateFormat df : isoDateFormat) {
                try {
                    d = df.parse(value);
                    break;
                } catch (ParseException pe) {
                }
            }

        }
        if (d != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return cal;
        }
        throw new InvalidValue("Invalid date format: " + value);
    }

    public static String formatDate(Calendar cal) {
        return cal != null ? isoDateFormat[3].format(cal.getTime()) :null;

    }

    public static String formatDateTime(Calendar cal) {
        return cal != null? isoDateFormat[1].format(cal.getTime()): null;
    }

    public static String formatDateTimeFull(Calendar cal) {
        return cal != null ? isoDateFormat[0].format(cal.getTime()) :  null;
    }

    public static String formatDateTimeForPath(Calendar cal) {
        return cal != null? isoDateFormatForPath.format(cal.getTime()): null;
    }
}
