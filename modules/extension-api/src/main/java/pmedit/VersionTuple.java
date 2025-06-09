package pmedit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionTuple {
    public int major;
    public int minor;
    public int patch;
    public String tag = "";
    public boolean parseSuccess = false;


    public VersionTuple(String version) {
        this(version, "^v?(\\d+)\\.(\\d+)\\.(\\d+)-?(\\S*)$");
    }

    public VersionTuple(String version, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(version);
        if (matcher.find()) {
            major = Integer.parseInt(matcher.group(1));
            minor = Integer.parseInt(matcher.group(2));
            patch = Integer.parseInt(matcher.group(3));
            if (matcher.groupCount() > 3) {
                tag = matcher.group(4);
                if (tag == null) {
                    tag = "";
                }
            }
            parseSuccess = true;
        } else {
            tag = "dev";
        }
    }

    public int cmp(VersionTuple other) {
        int diff = major - other.major;
        if (diff != 0) return diff;
        diff = minor - other.minor;
        if (diff != 0) return diff;
        diff = patch - other.patch;
        if (diff != 0) return diff;
        return tag.compareToIgnoreCase(other.tag);
    }

    public String getAsString() {
        return major + "." +
                minor + "." +
                patch + ((tag.length() > 0) ? ("-" + tag) : "");
    }
}
