package pmedit;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.prefs.LocalDataDir;
import pmedit.prefs.Preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;

public class BatchMan {
    static Logger LOG() {return  LoggerFactory.getLogger(BatchMan.class); }

    public record LicenseValidity(String licensedTo, Date expiresAt) {
    }

    static LicenseValidity validity;

    public static LicenseValidity maybeHasBatch(MotoBoto mb) {
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
                LOG().error("Failed validating license", e);
            }
        }
        return null;
    }

    public static LicenseValidity getBatch() {
        if(validity == null) {
            MotoBoto mb = getMotoBoto();
            if (mb.moto() == null || mb.moto().isEmpty()) {
                String ek = System.getenv("PME_LICENSE");
                mb = new MotoBoto(ek, new Date().getTime());
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
            removeMotoBoto();
            return false;
        }
        validity = maybeHasBatch(new MotoBoto(moto, new Date().getTime()));
        if (null != validity) {
            setMotoBoto(moto);
            return true;
        }
        return false;
    }

    public record MotoBoto(String moto, long timeMs){
        public boolean isEmpty(){
            return moto == null || moto.isEmpty() || timeMs <=0;
        }
    }
    protected static String LICENSE_FILE_NAME="lic";

    public static MotoBoto getMotoBoto(){
        Path f = FileSystems.getDefault().getPath(LocalDataDir.getAppDataDir(), LICENSE_FILE_NAME);
        String moto = "";
        long timeMs = 0;
        if(Files.exists(f)) {
            try {
                timeMs = Files.readAttributes(f, BasicFileAttributes.class).creationTime().toMillis();
                moto = new SecureFileHandler(OsCheck.getComputerName()).readDecryptedFile(f.toString());
            } catch (Exception e) {
                LOG().error("Failed to read license from disk!", e);
            }
        }
        return new MotoBoto(moto, timeMs);
    }

    public static void setMotoBoto(String moto){
        Path f = FileSystems.getDefault().getPath(LocalDataDir.getAppDataDir(), LICENSE_FILE_NAME);
        MotoBoto exist = getMotoBoto();
        try {
            if(exist.moto.isEmpty() || !exist.moto.equals(moto)) {
                new SecureFileHandler(OsCheck.getComputerName()).writeEncryptedFile(f.toString(), moto);
            }
        } catch (Exception e) {
            LOG().error("Failed to write license on disk!", e);
        }
    }

    public static void removeMotoBoto(){
        Path f = FileSystems.getDefault().getPath(LocalDataDir.getAppDataDir(), LICENSE_FILE_NAME);
        try {
            Files.delete(f);
        } catch (IOException e) {
        }
    }
}
