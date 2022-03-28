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

import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants.HUAWEI_MAGIC;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FindPhoneResponse;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.MusicControl;
import nodomain.freeyourgadget.gadgetbridge.util.CheckSums;

public class HuaweiPacket {

    // TODO: make moment of encrypting/decrypting consistent

    public interface SecretsProvider {
        byte[] getSecretKey();
        byte[] getIv();
    }

    public static abstract class ParseException extends Exception {
        ParseException(String message) {
            super(message);
        }
    }

    public static class LengthMismatchException extends ParseException {
        LengthMismatchException(String message) {
            super(message);
        }
    }

    public static class MagicMismatchException extends ParseException {
        MagicMismatchException(String message) {
            super(message);
        }
    }

    public static class ChecksumIncorrectException extends ParseException {
        ChecksumIncorrectException(String message) {
            super(message);
        }
    }

    public static class MissingTagException extends ParseException {
        public MissingTagException(int tag) {
            super("Missing tag: " + Integer.toHexString(tag));
        }
    }

    protected static final int PACKET_MINIMAL_SIZE = 6;

    protected SecretsProvider secretsProvider;

    public byte serviceId = 0;
    public byte commandId = 0;
    protected HuaweiTLV tlv = null;

    private byte[] partialPacket = null;
    private byte[] payload = null;

    public boolean complete = false;

    private static final HashMap<Short, Class<? extends HuaweiPacket>> responsePacketTypes = new HashMap<>();
    static {
        responsePacketTypes.put((short) 0x0101, DeviceConfig.LinkParams.Response.class);
        responsePacketTypes.put((short) 0x0103, DeviceConfig.SupportedCommands.Response.class);
        responsePacketTypes.put((short) 0x0107, DeviceConfig.ProductInfo.Response.class);
        responsePacketTypes.put((short) 0x010F, DeviceConfig.BondParams.Response.class);
        responsePacketTypes.put((short) 0x0113, DeviceConfig.Auth.Response.class);
        responsePacketTypes.put((short) 0x0108, DeviceConfig.BatteryLevel.Response.class);

        responsePacketTypes.put((short) 0x0703, FitnessData.FitnessTotals.Response.class);
        responsePacketTypes.put((short) 0x070A, FitnessData.MessageCount.Response.class);
        responsePacketTypes.put((short) 0x070B, FitnessData.MessageData.StepResponse.class);
        responsePacketTypes.put((short) 0x070C, FitnessData.MessageCount.Response.class);
        responsePacketTypes.put((short) 0x070D, FitnessData.MessageData.SleepResponse.class);

        responsePacketTypes.put((short) 0x0b01, FindPhoneResponse.class);

        responsePacketTypes.put((short) 0x2501, MusicControl.MusicStatusResponse.class);
        responsePacketTypes.put((short) 0x2502, MusicControl.MusicInfo.Response.class);
        responsePacketTypes.put((short) 0x2503, MusicControl.Control.Response.class);
    }

    public HuaweiPacket(SecretsProvider secretsProvider) {
        this.secretsProvider = secretsProvider;
    }

    /*
     * This function is to convert the Packet into the proper subclass
     */
    protected HuaweiPacket fromPacket(HuaweiPacket packet) throws ParseException {
        this.secretsProvider = packet.secretsProvider;
        this.serviceId = packet.serviceId;
        this.commandId = packet.commandId;
        this.tlv = packet.tlv;
        this.partialPacket = packet.partialPacket;
        this.payload = packet.payload;
        this.complete = packet.complete;

        this.parseTlv();

        return this;
    }

    /*
     * This function is to set up the subclass for easy usage
     */
    protected void parseTlv() throws ParseException {
        throw new UnsupportedOperationException();
    }

    public HuaweiPacket parse(byte[] data) throws ParseException {
        if (partialPacket != null) {
            int newCapacity = partialPacket.length + data.length;
            data = ByteBuffer.allocate(newCapacity)
                    .put(partialPacket)
                    .put(data)
                    .array();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);

        if (buffer.capacity() < PACKET_MINIMAL_SIZE) {
            throw new LengthMismatchException("Packet length mismatch : "
                    + buffer.capacity()
                    + " != 6");
        }

        byte magic = buffer.get();
        short expectedSize = buffer.getShort();
        int isSliced = buffer.get();
        if (isSliced == 1 || isSliced == 2 || isSliced == 3) {
            int sliceFlag = buffer.get();
        }
        byte[] newPayload = new byte[buffer.remaining() - 2];
        buffer.get(newPayload, 0, buffer.remaining() - 2);
        short expectedChecksum = buffer.getShort();
        buffer.rewind();

        if (magic != HUAWEI_MAGIC) {
            throw new MagicMismatchException("Magic mismatch : "
                    + Integer.toHexString(magic)
                    + " != 0x5A");
        }

        int newPayloadLen = newPayload.length + 1;
        if (isSliced == 1 || isSliced == 2 || isSliced == 3) {
            newPayloadLen = newPayload.length + 2;
        }
        if (expectedSize != (short) newPayloadLen) {
            // Older band and BT version do not handle message with more than 256 bits.
            this.partialPacket = data;
            return this;
        }
        this.partialPacket = null;

        byte[] dataNoCRC = new byte[buffer.capacity() - 2];
        buffer.get(dataNoCRC, 0, buffer.capacity() - 2);
        short actualChecksum = (short) CheckSums.getCRC16(dataNoCRC, 0x0000);
        if (actualChecksum != expectedChecksum) {
            throw new ChecksumIncorrectException("Checksum mismatch : "
                    + String.valueOf(actualChecksum)
                    + " != "
                    + String.valueOf(expectedChecksum));
        }

        if (isSliced == 1 || isSliced == 2 || isSliced == 3) {
            // LOG.debug("IsSliced");
            if (payload != null) {
                int newCapacity = payload.length + newPayload.length;
                newPayload = ByteBuffer.allocate(newCapacity)
                        .put(payload)
                        .put(newPayload)
                        .array();
            }

            if (isSliced != 3) {
                // Sliced packet isn't complete yet
                this.payload = newPayload;
                return this;
            }
        }
//        LOG.debug("Parsed packet values :\n"
//                        + "Service ID: " + Integer.toHexString(newPayload[0]) + " - Command ID: " + Integer.toHexString(newPayload[1]) + "\n"
//                        // + "Magic: " + Integer.toHexString(magic) + "\n"
//                        // + "expectedSize: " + String.valueOf(expectedSize) + "\n"
//                        // + "isSliced: " + String.valueOf(isSliced) + "\n"
//                        + "newPayload: " + StringUtils.bytesToHex(newPayload) + "\n"
//                // + "expectedChecksum: " + Integer.toHexString(0xFFFF & expectedChecksum)
//        );

        this.tlv = new HuaweiTLV();
        this.tlv.parse(newPayload, 2, newPayload.length - 2);
        this.complete = true;

        this.serviceId = newPayload[0];
        this.commandId = newPayload[1];

        short tableKey = (short) ((short) serviceId << 8 | commandId);
        Class<? extends HuaweiPacket> packetType = responsePacketTypes.get(tableKey);

        // No alternative packet, return as raw packet
        if (packetType == null)
            return this;

        try {
            return packetType.getDeclaredConstructor(SecretsProvider.class).newInstance(secretsProvider).fromPacket(this);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException | ParseException e) {
            e.printStackTrace();
            // The new instance cannot be created, so the packet is returned as "raw packet"
            return this;
        }
    }

    public byte[] serialize() {
        // TODO: necessary for this to work:
        //       - serviceId
        //       - commandId
        //       - tlv
        // TODO: maybe use the complete flag to know if it can be serialized?

        int headerLength = 4; // Magic + (bodyLength + 1) + 0x00
        byte[] serializedTLV = this.tlv.serialize();
        int bodyLength = 2 + serializedTLV.length;
        ByteBuffer buffer = ByteBuffer.allocate(headerLength + bodyLength);
        buffer.put((byte) 0x5A);
        buffer.putShort((short)(bodyLength + 1));
        buffer.put((byte) 0x00);
        buffer.put(this.serviceId);
        buffer.put(this.commandId);
        buffer.put(serializedTLV);
        int crc16 = CheckSums.getCRC16(buffer.array(), 0x0000);
        int footerLength = 2; //CRC16
        ByteBuffer finalBuffer = ByteBuffer.allocate(buffer.capacity() + footerLength);
        finalBuffer.put(buffer.array());
        finalBuffer.putShort((short)crc16);
        return finalBuffer.array();
    }
}
