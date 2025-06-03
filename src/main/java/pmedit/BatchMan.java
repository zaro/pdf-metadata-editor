package pmedit;

import pmedit.prefs.Preferences;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class BatchMan {

    public static boolean maybeHasBatch(String moto, String boto) {
        if (moto != null && boto != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(boto.trim().getBytes());
                byte[] toto = md.digest();
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] binMoto = decoder.decode(moto.trim());
                if (binMoto.length == toto.length) {
                    return Arrays.equals(toto, binMoto);
                } else if (binMoto.length == toto.length + 1) {
                    byte key = binMoto[0];
                    byte[] rest = Arrays.copyOfRange(binMoto, 1, binMoto.length);
                    for (int i = 0; i < rest.length; ++i) {
                        rest[i] = (byte) (rest[i] ^ key);
                    }
                    return Arrays.equals(toto, rest);
                }
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return false;
    }

    public static boolean hasBatch() {
        String moto = Preferences.getInstance().get("key", null);
        String boto = Preferences.getInstance().get("email", null);
        if (moto == null || boto == null) {
            String ek = System.getenv("PME_LICENSE");
            if (ek != null) {
                int comaPos = ek.indexOf(",");
                if (comaPos > 0) {
                    boto = ek.substring(0, comaPos);
                    moto = ek.substring(comaPos + 1);
                }
            }
        }
        return maybeHasBatch(moto, boto);
    }

    public static boolean giveBatch(String moto, String boto) {
        if (maybeHasBatch(moto, boto)) {
            Preferences.getInstance().put("key", moto);
            Preferences.getInstance().put("email", boto);
            return true;
        }
        return false;
    }

}
