package nodomain.freeyourgadget.gadgetbridge.util;

import android.annotation.SuppressLint;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    public static byte[] encryptAES(byte[] value, byte[] secretKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        @SuppressLint("GetInstance") Cipher ecipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec newKey = new SecretKeySpec(secretKey, "AES");
        ecipher.init(Cipher.ENCRYPT_MODE, newKey);
        return ecipher.doFinal(value);
    }

    public static byte[] decryptAES(byte[] value, byte[] secretKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        @SuppressLint("GetInstance") Cipher ecipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec newKey = new SecretKeySpec(secretKey, "AES");
        ecipher.init(Cipher.DECRYPT_MODE, newKey);
        return ecipher.doFinal(value);
    }

    public static byte[] encryptAES_CBC_Pad(byte[] data, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
        return cipher.doFinal(data);
    }

    public static byte[] decryptAES_CBC_Pad(byte[] data, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
        return cipher.doFinal(data);
    }

    public static byte[] encryptAES_GCM_NoPad(byte[] data, byte[] key, byte[] iv, byte[] aad) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec paramSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        return cipher.doFinal(data);
    }

    public static byte[] calcHmacSha256(byte[] secretKey, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(message);
    }

    public static byte[] digest(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }

    // Thanks to https://www.javatips.net/api/keywhiz-master/hkdf/src/main/java/keywhiz/hkdf/Hkdf.java for light code
    public static byte[] hkdfSha256(byte[] secretKey, byte[] salt, int outputLength) throws InvalidKeyException, NoSuchAlgorithmException { // return 32 byte len session key - outputLength=32 ?
        byte[] pseudoRandomKey = calcHmacSha256(salt, secretKey); //extract
        SecretKey pseudoSecretKey = new SecretKeySpec(pseudoRandomKey, "HmacSHA256");
        byte[] info = new byte[0];
        int hashLen = 32;
        int n = (outputLength % hashLen == 0) ? outputLength / hashLen : (outputLength / hashLen) + 1;
        byte[] hashRound = new byte[0];

        ByteBuffer generatedBytes = ByteBuffer.allocate(Math.multiplyExact(n, hashLen));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(pseudoSecretKey);
        for (int roundNum = 1; roundNum <= n; roundNum++) {
          mac.reset();
          ByteBuffer t = ByteBuffer.allocate(hashRound.length + info.length + 1);
          t.put(hashRound);
          t.put(info);
          t.put((byte)roundNum);
          hashRound = mac.doFinal(t.array());
          generatedBytes.put(hashRound);
        }

        byte[] result = new byte[outputLength];
        generatedBytes.rewind();
        generatedBytes.get(result, 0, outputLength);
        return result;


    }
}
