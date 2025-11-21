package pmedit;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.prefs.Preferences;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;

public class BatchMan {
    static Logger LOG = LoggerFactory.getLogger(BatchMan.class);

    public record LicenseValidity(String licensedTo, Date expiresAt) {
    }

    static LicenseValidity validity;

    public static LicenseValidity maybeHasBatch(Preferences.MotoBoto mb) {
        if (mb.moto() != null) {
            try {
                // Decode Base64
                byte[] keyBytes = Base64.getDecoder().decode(Version.getPublicKey());

                // Create PublicKey object
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory kf = null;

                kf = KeyFactory.getInstance("EC");
                PublicKey publicKey = kf.generatePublic(spec);

                Claims payload =  Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(mb.moto())
                        .getPayload();

                Date iat = payload.getIssuedAt();
                Date exp = payload.getExpiration();
                Date now = new Date();
                if(now.after(iat) && now.before(exp) && mb.timeMs() <= now.getTime()){
                    String deviceId = payload.get("deviceId", String.class);
                    if(deviceId != null && deviceId.equals(OsCheck.getComputerName())) {
                        return new LicenseValidity(payload.getSubject(), exp);
                    }
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | RuntimeException e) {
                LOG.error("Failed validating license", e);
            }
        }
        return null;
    }

    public static LicenseValidity getBatch() {
        if(validity == null) {
            Preferences.MotoBoto mb = Preferences.getMotoBoto();
            if (mb.moto() == null || mb.moto().isEmpty()) {
                String ek = System.getenv("PME_LICENSE");
                mb = new Preferences.MotoBoto(ek, new Date().getTime());
            }
            validity = maybeHasBatch(mb);
        }
        return validity;
    }

    public static Date getExpiration() {
        return validity.expiresAt();
    }

    public static boolean hasBatch() {
        getBatch();
        return validity != null && !validity.licensedTo.isEmpty();
    }

    public static boolean giveBatch(String moto) {
        if(moto == null ) {
            Preferences.removeMotoBoto();
        }
        validity = maybeHasBatch(new Preferences.MotoBoto(moto, new Date().getTime()));
        if (null != validity) {
            Preferences.setMotoBoto(moto);
            return true;
        }
        return false;
    }

}
