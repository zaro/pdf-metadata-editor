package pmedit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pmedit.CommandLine.ParseError;

public class DateFormat {
    
	private static final SimpleDateFormat isoDateFormat[] = new SimpleDateFormat[]{
    		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
      		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
      		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
      		new SimpleDateFormat("yyyy-MM-dd"),
    };
	public static Calendar parseDateOrNull(String value){
		try {
			return parseDate(value);
		} catch (ParseError e) {
			return null;
		}
	}
	
	public static Calendar parseDate(String value) throws ParseError{
		Date d = null;
		for(SimpleDateFormat df:isoDateFormat){
			try {
				d = df.parse(value);
			} catch (ParseException e) {
			}
		}
		if(d != null){
			  Calendar cal = Calendar.getInstance();
			  cal.setTime(d);
			  return cal;
		}
		throw new CommandLine.ParseError("Invalid date format: "+ value);
	}
	
	public static String formatDate(Calendar cal) {
		return isoDateFormat[3].format(cal.getTime());
		
	}

	public static String formatDateTime(Calendar cal) {
		return isoDateFormat[1].format(cal.getTime());		
	}

	public static String formatDateTimeFull(Calendar cal) {
		return isoDateFormat[0].format(cal.getTime());		
	}
	
}
