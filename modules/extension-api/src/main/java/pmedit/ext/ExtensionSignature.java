package pmedit.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.Version;
import pmedit.VersionTuple;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

public class ExtensionSignature {
    static Logger LOG() { return  LoggerFactory.getLogger(Version.class);}
    private static String getPublicKeyString() {
        Properties prop = new Properties();
        try {
            prop.load(ExtensionSignature.class.getClassLoader().getResourceAsStream("pmedit/extenstions.properties"));
            return prop.getProperty("ext.publicKey", null);
        } catch (IOException e) {
            LOG().error("getPublicKey", e);
        }
        return null;
    }

    private static PublicKey pubKey;
    public static PublicKey getPublicKey(){
        if(pubKey == null) {
            byte[] keyBytes = Base64.getDecoder().decode(getPublicKeyString());

            // Create PublicKey object
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = null;

            try {
                keyFactory = KeyFactory.getInstance("RSA");
                pubKey =  keyFactory.generatePublic(spec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
        return pubKey;
    }
}
