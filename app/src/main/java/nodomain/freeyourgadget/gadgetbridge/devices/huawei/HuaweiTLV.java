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

/* TLV parsing and serialisation thanks to https://github.com/yihleego/tlv */
package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants.CryptoTags;

public class HuaweiTLV {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiTLV.class);

    protected Map<Byte, byte[]> valueMap;

    public HuaweiTLV() {
        this.valueMap = new HashMap<>();
    }

    public int length() {
        int length = 0;
        for (Map.Entry<Byte, byte[]> entry : valueMap.entrySet()) {
            int value_length = entry.getValue().length;
            length += 1 + VarInt.getVarIntSize(value_length) + value_length;
        }
        return length;
    }

    public HuaweiTLV parse(byte[] buffer, int offset, int length) {
        if (buffer == null) {
            return null;
        }
        int parsed = 0;
        while (parsed < length) {
            // Tag is 1 byte
            byte tag = buffer[offset + parsed];
            parsed += 1;
            // Size is a VarInt >= 1 byte
            VarInt varInt = new VarInt(buffer, offset + parsed);
            int size = varInt.dValue;
            parsed += varInt.size;
            byte[] value = new byte[size];
            System.arraycopy(buffer, offset + parsed, value, 0, size);
            put(tag, value);
            parsed += size;
        }
        LOG.debug("Parsed TLV: " + this.toString());
        return this;
    }

    public HuaweiTLV parse(byte[] buffer) {
        if (buffer == null) {
            return null;
        }
        return parse(buffer, 0, buffer.length);
    }

    public byte[] serialize() {
        int offset = 0;
        int length = this.length();
        if (length == 0) {
            return new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        for (Map.Entry<Byte, byte[]> entry : valueMap.entrySet()) {
            byte tag = entry.getKey();
            byte[] value = entry.getValue();
            byte[] varIntValue = VarInt.putVarIntValue(value.length);
            buffer.put(tag);
            buffer.put(varIntValue);
            buffer.put(value);
        }
        LOG.debug("Serialized TLV: " + this.toString());
        return buffer.array();
    }

    public HuaweiTLV put(int tag) {
        byte[] value = new byte[0];
        valueMap.put((byte)tag, value);
        return this;
    }

    public HuaweiTLV put(int tag, byte[] value) {
        if (value == null) {
            return this;
        }
        valueMap.put((byte)tag, value);
        return this;
    }

    public HuaweiTLV put(int tag, byte value) {
        return put(tag, new byte[]{value});
    }

    public HuaweiTLV put(int tag, boolean value) {
        return put(tag, new byte[]{value ? (byte) 1 : (byte) 0});
    }

    public HuaweiTLV put(int tag, int value) {
        return put(tag, ByteBuffer.allocate(4).putInt(value).array());
    }

    public HuaweiTLV put(int tag, String value) {
        if (value == null) {
            return this;
        }
        return put(tag, value.getBytes(StandardCharsets.UTF_8));
    }

    public HuaweiTLV put(int tag, HuaweiTLV value) {
        if (value == null) {
            return this;
        }
        return put(tag, value.serialize());
    }

    public byte[] getBytes(int tag) {
        return valueMap.get((byte)tag);
    }

    public Byte getByte(int tag) {
        byte[] bytes = getBytes(tag);
        if (bytes == null) {
            return null;
        }
        return bytes[0];
    }

    public Boolean getBoolean(int tag) {
        byte[] bytes = getBytes(tag);
        if (bytes == null) {
            return null;
        }
        return bytes[0] == 1;
    }

    public Integer getInteger(int tag) {
        byte[] bytes = getBytes(tag);
        if (bytes == null) {
            return null;
        }
        return ByteBuffer.wrap(bytes).getInt();
    }

    public String getString(int tag) {
        byte[] bytes = getBytes(tag);
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public HuaweiTLV getObject(int tag) {
        byte[] bytes = getBytes(tag);
        if (bytes == null) {
            return null;
        }
        return new HuaweiTLV().parse(bytes, 0, bytes.length);
    }

    public boolean contains(int tag) {
        return valueMap.containsKey((byte)tag);
    }

    public byte[] remove(byte tag) {
        return valueMap.remove((byte)tag);
    }

    public String toString() {
        String msg = "";
        for (Map.Entry<Byte, byte[]> entry : valueMap.entrySet()) {
            msg += "{tag: " + (entry.getKey() & 0xFF);
            msg += " - Value: " + StringUtils.bytesToHex(entry.getValue());
            msg += "} - ";
        }
        return msg.substring(0, msg.length()-3);
    }

    public void encrypt(byte[] key, byte[] iv) {
        byte[] serializedTLV = serialize();
        byte[] encryptedTLV = HuaweiCrypto.encrypt(serializedTLV, key, iv);
        this.valueMap = new HashMap<>();
        put(CryptoTags.encryption, (byte)1);
        put(CryptoTags.initVector, iv);
        put(CryptoTags.cipherText, encryptedTLV);
    }

    public void decrypt(byte[] key) {
        byte[] decryptedTLV = HuaweiCrypto.decrypt(getBytes(CryptoTags.cipherText), key, getBytes(CryptoTags.initVector));
        this.valueMap = new HashMap<>();
        parse(decryptedTLV);

    }

}

final class VarInt {
    protected int dValue; // Decoded value of the VarInt
    protected int size; // Size of the encoded value
    protected byte[] eValue; // Encoded value of the VarInt

    public VarInt(byte[] src, int offset) {
        this.dValue = getVarIntValue(src, offset);
        this.eValue = putVarIntValue(this.dValue);
        this.size = this.eValue.length;
    }

    public String toString() {
        return "VarInt(dValue: " + this.dValue + ", size: " + this.size + ", eValue: " + StringUtils.bytesToHex(this.eValue) + ")";
    }
    
    /**
    * Returns the size of the encoded input value.
    *
    * @param value the integer to be measured
    * @return the encoding size of the input value
    */
    public static int getVarIntSize(int value) {
        int result = 0;
        do {
        result++;
        value >>>= 7;
        } while (value != 0);
        return result;
    }

    /**
    * Decode a byte array of a variable-length encoding from start,
    * 7 bits per byte.
    * Return the decoded value in int.
    *
    * @param src the byte array to get the var int from
    * @return the decoded value in int
    */
    public static int getVarIntValue(byte[] src, int offset) {
        int result = 0;
        int shift = 0;
        int b;
        do {
            if (shift >= 32) {
                // Out of range
                throw new IndexOutOfBoundsException("varint too long");
            }
            // Get 7 bits from next byte
            b = src[offset++];
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    /**
    * Encode an integer in a variable-length encoding, 7 bits per byte.
    * Return the encoded value in byte[]
    *
    * @param value the int value to encode
    * @return the encoded value in byte[]
    */
    public static byte[] putVarIntValue(int value) {
        int offset = 0;
        byte[] result = new byte[getVarIntSize(value)];
        do {
            // Encode next 7 bits + terminator bit
            int bits = value & 0x7F;
            value >>>= 7;
            byte b = (byte) (bits + ((value != 0) ? 0x80 : 0));
            result[offset++] = b;
        } while (value != 0);
        return result;
    }
}