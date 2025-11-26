package pmedit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class SecureFileHandler {
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // bits
    private static final int PBKDF2_ITERATIONS = 100000;

    private final String password;

    public SecureFileHandler(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }
        this.password = password;
    }

    public void writeEncryptedFile(String filePath, String plaintext) throws Exception {
        byte[] plaintextBytes = plaintext.getBytes("UTF-8");

        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        SecretKey key = deriveKey(password, iv); // Use IV as salt for simplicity and uniqueness
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
        byte[] ciphertext = cipher.doFinal(plaintextBytes);

        // Write IV + ciphertext to file
        try (OutputStream out = new FileOutputStream(filePath)) {
            out.write(iv);
            out.write(ciphertext);
        }
    }

    public String readDecryptedFile(String filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(new File(filePath).toPath());
        if (fileContent.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("File too short to contain valid IV");
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(fileContent, 0, iv, 0, GCM_IV_LENGTH);
        byte[] ciphertext = new byte[fileContent.length - GCM_IV_LENGTH];
        System.arraycopy(fileContent, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        SecretKey key = deriveKey(password, iv);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext, "UTF-8");
    }

    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}