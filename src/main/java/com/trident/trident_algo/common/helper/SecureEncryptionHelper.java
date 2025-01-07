package com.trident.trident_algo.common.helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecureEncryptionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureEncryptionHelper.class);

    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    private final SecretKey secretKey;

    public SecureEncryptionHelper() {
        // Load key securely from an environment variable or secrets management service
        String encodedKey = "ej/WxIsxWiSUaesea3SxGNPVZvN688S22i/XdEafOeM=";//System.getenv("ENCRYPTION_SECRET_KEY");
        if (encodedKey == null) {
            throw new IllegalStateException("Encryption key is not set in the environment");
        }
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(encodedKey), AES);
    }

    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            byte[] iv = generateIV();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] cipherText = cipher.doFinal(plaintext.getBytes());
            byte[] encrypted = concatenate(iv, cipherText);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            LOGGER.error("Error encrypting data", e);
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] iv = extractIV(decoded);
            byte[] cipherText = extractCipherText(decoded);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] plaintext = cipher.doFinal(cipherText);
            return new String(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] concatenate(byte[] iv, byte[] cipherText) {
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
        return combined;
    }

    private byte[] extractIV(byte[] data) {
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);
        return iv;
    }

    private byte[] extractCipherText(byte[] data) {
        byte[] cipherText = new byte[data.length - GCM_IV_LENGTH];
        System.arraycopy(data, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
        return cipherText;
    }
}
