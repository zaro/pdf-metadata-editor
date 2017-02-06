package pmedit;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class ListFormat {

	
	public static String humanReadable(List<Object> list){
		StringBuilder sb = new StringBuilder();
		Iterator<Object> it = list.iterator();
		while(it.hasNext()){
			Object v =  it.next();
			if(Calendar.class.isAssignableFrom(v.getClass())){
				sb.append(DateFormat.formatDateTime((Calendar)v));
			} else {
				sb.append(v.toString());
			}
			if(it.hasNext()){
				sb.append(", ");
			}
		}
		return sb.toString();
	}
}
