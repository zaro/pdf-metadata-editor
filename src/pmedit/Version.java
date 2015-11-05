package pmedit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    public static final String version="1.1.0";
    public static class VersionTuple {
    	public int major;
    	public int minor;
    	public int patch;
    	
    	
    	public  VersionTuple(String version) {
    		this(version, "(\\d+)\\.(\\d+)\\.(\\d+)");
    	}
    	public VersionTuple(String version, String pattern) {
    		Matcher matcher = Pattern.compile(pattern).matcher(version);
    		if(matcher.find()){
    			major = Integer.parseInt(matcher.group(1));
    			minor = Integer.parseInt(matcher.group(2));
    			patch = Integer.parseInt(matcher.group(3));
    		}
    	}
    	
    	public int cmp(VersionTuple other){
    		int diff = major - other.major;
    		if(diff != 0) return diff;
    		diff = minor - other.minor;
    		if(diff != 0) return diff;
    		return patch - other.patch;    		
    	}
    	
    	public String getAsString(){
    		return Integer.toString(major) + "." +
    				Integer.toString(minor) + "." +
    				Integer.toString(patch);
    	}
    };
    
    public static VersionTuple get() {
    	return new VersionTuple( version );
    }
}
