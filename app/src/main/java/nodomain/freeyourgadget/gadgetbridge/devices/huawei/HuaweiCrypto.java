/*  Copyright (C) 2021 Gaignon Damien

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class HuaweiCrypto {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiCrypto.class);

    public static final byte[] SECRET_KEY_1_v1 = new byte[]{ 0x6F, 0x75, 0x6A, 0x79,
                                                            0x6D, 0x77, 0x71, 0x34,
                                                            0x63, 0x6C, 0x76, 0x39,
                                                            0x33, 0x37, 0x38, 0x79};
    public static final byte[] SECRET_KEY_2_v1 = new byte[]{ 0x62, 0x31, 0x30, 0x6A,
                                                            0x67, 0x66, 0x64, 0x39,
                                                            0x79, 0x37, 0x76, 0x73,
                                                            0x75, 0x64, 0x61, 0x39};
    public static final byte[] SECRET_KEY_1_v23 = new byte[]{ 0x55, 0x53, (byte)0x86, (byte)0xFC,
                                                            0x63, 0x20, 0x07, (byte)0xAA,
                                                            (byte)0x86, 0x49, 0x35, 0x22,
                                                            (byte)0xB8, 0x6A, (byte)0xE2, 0x5C};
    public static final byte[] SECRET_KEY_2_v23 = new byte[]{ 0x33, 0x07, (byte)0x9B, (byte)0xC5,
                                                            0x7A, (byte)0x88, 0x6D, 0x3C,
                                                            (byte)0xF5, 0x61, 0x37, 0x09,
                                                            0x6F, 0x22, (byte)0x80, 0x00};

    public static final byte[] DIGEST_SECRET_v1 = new byte[]{ 0x70, (byte)0xFB, 0x6C, 0x24,
                                                            0x03, 0x5F, (byte)0xDB, 0x55,
                                                            0x2F, 0x38, (byte)0x89, (byte)0x8A,
                                                            (byte) 0xEE, (byte)0xDE, 0x3F, 0x69};
    public static final byte[] DIGEST_SECRET_v2 = new byte[]{ (byte)0x93, (byte)0xAC, (byte)0xDE, (byte)0xF7,
                                                            0x6A, (byte)0xCB, 0x09, (byte)0x85,
                                                            0x7D, (byte)0xBF, (byte)0xE5, 0x26,
                                                            0x1A, (byte)0xAB, (byte)0xCD, 0x78};
    public static final byte[] DIGEST_SECRET_v3 = new byte[]{ (byte)0x9C, 0x27, 0x63, (byte)0xA9,
                                                            (byte)0xCC, (byte)0xE1, 0x34, 0x76,
                                                            0x6D, (byte)0xE3, (byte)0xFF, 0x61,
                                                            0x18, 0x20, 0x05, 0x53};

    public static final byte[] MESSAGE_RESPONSE = new byte[]{0x01, 0x10};
    public static final byte[] MESSAGE_CHALLENGE = new byte[]{0x01, 0x00};

    public static final long ENCRYPTION_COUNTER_MAX = 0xFFFFFFFF;

    protected int authVersion = 0;

    public HuaweiCrypto(byte[] authVersion) {
        this.authVersion = (int)ByteBuffer.wrap(authVersion).getShort();
    }

    public static byte[] generateNonce() {
        return GB.hexStringToByteArray(RandomStringUtils.random(32, true, true));
    }

    private static byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256 = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return hmacSha256;
    }

    public byte[] computeDigest(byte[] message, byte[] clientNonce, byte[] serverNonce) {
        byte[] digestSecret;
        if (authVersion == 1) {
            digestSecret = DIGEST_SECRET_v1;
        } else if (authVersion == 2) {
            digestSecret = DIGEST_SECRET_v2;
        } else {
            digestSecret = DIGEST_SECRET_v3;
        }
        byte[] completeNonce = ByteBuffer.allocate(32)
                                                .put(serverNonce)
                                                .put(clientNonce)
                                                .array();
        byte[] msgToDigest = ByteBuffer.allocate(16 + message.length)
                                                .put(digestSecret)
                                                .put(message)
                                                .array();
        byte[] digestStep1 = calcHmacSha256(msgToDigest, completeNonce);
        return calcHmacSha256(digestStep1, completeNonce);
    }

    public byte[] digestChallenge(byte[] clientNonce, byte[] serverNonce) {
        return computeDigest(MESSAGE_CHALLENGE, clientNonce, serverNonce);
    }

    public byte[] digestResponse(byte[] clientNonce, byte[] serverNonce) {
        return computeDigest(MESSAGE_RESPONSE, clientNonce, serverNonce);
    }

    public static ArrayList initializationVector(long counter) {
        if (counter == ENCRYPTION_COUNTER_MAX) {
            counter = 1;
        } else {
            counter += 1;
        }
        ByteBuffer iv = ByteBuffer.allocate(16);
        iv.put(generateNonce(), 0, 12);
        iv.put(ByteBuffer.allocate(8).putLong(counter).array(), 4, 4);
        ArrayList ivCounter = new ArrayList<>();
        ivCounter.add(iv.array());
        ivCounter.add(counter);
        return ivCounter;
    }

    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv){
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] data, byte[] key, byte[] iv){
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] createSecretKey(String macAddress){
        byte[] secret_key_1 = SECRET_KEY_1_v23;
        byte[] secret_key_2 = SECRET_KEY_2_v23;
        if (authVersion == 1) {
            secret_key_1 = SECRET_KEY_1_v1;
            secret_key_2 = SECRET_KEY_2_v1;
        }

        byte[] macAddressKey = (macAddress.replace(":", "") + "0000").getBytes(StandardCharsets.UTF_8);

        byte[] mixedSecretKey = new byte[16];
        for (int i = 0; i < 16; i++) {
            mixedSecretKey[i] = (byte)((((0xFF & secret_key_1[i]) << 4) ^ (0xFF & secret_key_2[i])) & 0xFF);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] mixedSecretKeyHash = digest.digest(mixedSecretKey);
            byte[] finalMixedKey = new byte[16];
            for (int i = 0; i < 16; i++) {
                finalMixedKey[i] = (byte)((((0xFF & mixedSecretKeyHash[i]) >> 6) ^ (0xFF & macAddressKey[i])) & 0xFF);
            }
            MessageDigest digest2 = MessageDigest.getInstance("SHA-256");
            byte[] finalMixedKeyHash = digest2.digest(finalMixedKey);
            return Arrays.copyOfRange(finalMixedKeyHash, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] createBondingKey(String macAddress, byte[] key, byte[] iv){
        return encrypt(key, createSecretKey(macAddress), iv);
    }
}
