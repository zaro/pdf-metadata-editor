package pmedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class Version {
    static final Logger LOG = LoggerFactory.getLogger(Version.class);

    protected static String version;
    protected static String appName;
    protected static String pubKey;
    protected static String uuid;

    public static VersionTuple get() {
        if (version == null) {
            Properties prop = new Properties();
            try {
                prop.load(VersionTuple.class.getClassLoader().getResourceAsStream("pmedit/version.properties"));
                version = prop.getProperty("app.version", "0.0.0-dev");
            } catch (IOException e) {
                LOG.error("get", e);
                version = "0.0.0-dev";
            }
        }
        return new VersionTuple(version);
    }

    public static String getAppName() {
        if (appName == null) {
            Properties prop = new Properties();
            try {
                prop.load(VersionTuple.class.getClassLoader().getResourceAsStream("pmedit/version.properties"));
                appName = prop.getProperty("app.name", "Pdf Metadata Editor");
            } catch (IOException e) {
                LOG.error("getAppName", e);
                appName = "Pdf Metadata Editor";
            }
        }
        return appName;
    }

    public static String getPublicKey() {
        if (pubKey == null) {
            Properties prop = new Properties();
            try {
                prop.load(VersionTuple.class.getClassLoader().getResourceAsStream("pmedit/version.properties"));
                pubKey = prop.getProperty("app.publicKey", null);
            } catch (IOException e) {
                LOG.error("getPublicKey", e);
                pubKey = null;
            }
        }
        return pubKey;
    }

    public static String getUUID() {
        if (uuid == null) {
            Properties prop = new Properties();
            try {
                prop.load(VersionTuple.class.getClassLoader().getResourceAsStream("pmedit/version.properties"));
                uuid = prop.getProperty("app.uuid", null);
            } catch (IOException e) {
                LOG.error("getUUID", e);
                uuid = null;
            }
        }
        return uuid;
    }


}
