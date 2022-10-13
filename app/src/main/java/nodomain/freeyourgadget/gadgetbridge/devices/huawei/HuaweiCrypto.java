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

import nodomain.freeyourgadget.gadgetbridge.util.CryptoUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuaweiCrypto {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiCrypto.class);

    public static class CryptoException extends Exception { }

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
        // While technically not a nonce, we need it to be random and rely on the length for the chance of repitition to be small
        byte[] returnValue = new byte[16];
        (new SecureRandom()).nextBytes(returnValue);
        return returnValue;
    }

    public byte[] computeDigest(byte[] message, byte[] clientNonce, byte[] serverNonce) throws NoSuchAlgorithmException, InvalidKeyException {
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
        byte[] digestStep1 = CryptoUtils.calcHmacSha256(msgToDigest, completeNonce);
        return CryptoUtils.calcHmacSha256(digestStep1, completeNonce);
    }

    public byte[] digestChallenge(byte[] clientNonce, byte[] serverNonce) throws NoSuchAlgorithmException, InvalidKeyException {
        return computeDigest(MESSAGE_CHALLENGE, clientNonce, serverNonce);
    }

    public byte[] digestResponse(byte[] clientNonce, byte[] serverNonce) throws NoSuchAlgorithmException, InvalidKeyException {
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

    public byte[] createSecretKey(String macAddress) throws NoSuchAlgorithmException {
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

        byte[] mixedSecretKeyHash = CryptoUtils.digest(mixedSecretKey);
        byte[] finalMixedKey = new byte[16];
        for (int i = 0; i < 16; i++) {
            finalMixedKey[i] = (byte)((((0xFF & mixedSecretKeyHash[i]) >> 6) ^ (0xFF & macAddressKey[i])) & 0xFF);
        }
        byte[] finalMixedKeyHash = CryptoUtils.digest(finalMixedKey);
        return Arrays.copyOfRange(finalMixedKeyHash, 0, 16);
    }

    public byte[] createBondingKey(String macAddress, byte[] key, byte[] iv) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] secretKey = createSecretKey(macAddress);
        return CryptoUtils.encryptAES_CBC_Pad(key, secretKey, iv);
    }

    public byte[] decryptPinCode(byte[] message, byte[] iv) {
        byte[] secretKey;
        if (authVersion == 1) {
            secretKey = DIGEST_SECRET_v1;
        } else if (authVersion == 2) {
            secretKey= DIGEST_SECRET_v2;
        } else {
            secretKey = DIGEST_SECRET_v3;
        }
        try {
            return CryptoUtils.decryptAES_CBC_Pad(message, secretKey, iv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static byte[] encrypt(byte authMode, byte[] message, byte[] key, byte[] iv) throws CryptoException {
        try {
            if (authMode == 0x04) {
                LOG.debug("GCM encrypt");
                return CryptoUtils.encryptAES_GCM_NoPad(message, key, iv, null);
            } else {
                LOG.debug("CBC encrypt");
                return CryptoUtils.encryptAES_CBC_Pad(message, key, iv);
            }
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new CryptoException();
        }
    }

    public static byte[] decrypt(byte authMode, byte[] message, byte[] key, byte[] iv) throws CryptoException {
        try {
            if (authMode == 0x04) {
                LOG.debug("GCM decrypt");
                return CryptoUtils.decryptAES_GCM_NoPad(message, key, iv, null);
            } else {
                LOG.debug("CBC decrypt");
                return CryptoUtils.decryptAES_CBC_Pad(message, key, iv);
            }
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new CryptoException();
        }
    }
}
