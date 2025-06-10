package pmedit;

import io.jsonwebtoken.Claims;
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
    static String subject;
    static Date expires;

    public static String maybeHasBatch(Preferences.MotoBoto mb) {
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
                    expires = exp;
                    return payload.getSubject();
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | RuntimeException ignored) {
            }
        }
        return null;
    }

    public static String getBatch() {
        if(subject == null || subject.isEmpty()) {
            Preferences.MotoBoto mb = Preferences.getMotoBoto();
            if (mb.moto() == null || mb.moto().isEmpty()) {
                String ek = System.getenv("PME_LICENSE");
                mb = new Preferences.MotoBoto(ek, new Date().getTime());
            }
            subject = maybeHasBatch(mb);
        }
        return subject;
    }

    public static Date getExpiration() {
        return expires;
    }

    public static boolean hasBatch() {
        getBatch();
        return subject != null && !subject.isEmpty();
    }

    public static boolean giveBatch(String moto) {
        if(moto == null ) {
            Preferences.removeMotoBoto();
            subject = null;
            expires = null;
        } if (null != maybeHasBatch(new Preferences.MotoBoto(moto, new Date().getTime()))) {
            Preferences.setMotoBoto(moto);
            return true;
        }
        return false;
    }

}
