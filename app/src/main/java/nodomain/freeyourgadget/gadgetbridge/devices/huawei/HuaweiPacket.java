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

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Calls;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FindPhoneResponse;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.MusicControl;
import nodomain.freeyourgadget.gadgetbridge.util.CheckSums;

public class HuaweiPacket {

    public interface ParamsProvider {
        byte[] getSecretKey();
        byte[] getIv();
        int getMtu();
    }

    public static abstract class ParseException extends Exception {
        ParseException(String message) {
            super(message);
        }

        ParseException(String message, Exception e) {
            super(message, e);
        }
    }

    public static class LengthMismatchException extends ParseException {
        public LengthMismatchException(String message) {
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

    public static class CryptoException extends ParseException {
        public CryptoException(String message, Exception e) {
            super(message, e);
        }
    }

    protected static final int PACKET_MINIMAL_SIZE = 6;

    protected ParamsProvider paramsProvider;

    public byte serviceId = 0;
    public byte commandId = 0;
    protected HuaweiTLV tlv = null;

    private byte[] partialPacket = null;
    private byte[] payload = null;

    public boolean complete = false;

    // Encryption is enabled by default, packets which don't use it must disable it
    protected boolean isEncrypted = true;

    protected boolean isSliced = false;

    private static final HashMap<Short, Class<? extends HuaweiPacket>> responsePacketTypes = new HashMap<>();
    static {
        responsePacketTypes.put((short) 0x0101, DeviceConfig.LinkParams.Response.class);
        responsePacketTypes.put((short) 0x0102, DeviceConfig.SupportedServices.Response.class);
        responsePacketTypes.put((short) 0x0103, DeviceConfig.SupportedCommands.Response.class);
        responsePacketTypes.put((short) 0x0107, DeviceConfig.ProductInfo.Response.class);
        responsePacketTypes.put((short) 0x010F, DeviceConfig.BondParams.Response.class);
        responsePacketTypes.put((short) 0x0113, DeviceConfig.Auth.Response.class);
        responsePacketTypes.put((short) 0x0108, DeviceConfig.BatteryLevel.Response.class);
        responsePacketTypes.put((short) 0x011D, DeviceConfig.DndPriority.Response.class);
        responsePacketTypes.put((short) 0x0128, DeviceConfig.HiCHain.Response.class);

        responsePacketTypes.put((short) 0x0401, Calls.AnswerCallResponse.class);

        responsePacketTypes.put((short) 0x0703, FitnessData.FitnessTotals.Response.class);
        responsePacketTypes.put((short) 0x070A, FitnessData.MessageCount.Response.class);
        responsePacketTypes.put((short) 0x070B, FitnessData.MessageData.StepResponse.class);
        responsePacketTypes.put((short) 0x070C, FitnessData.MessageCount.Response.class);
        responsePacketTypes.put((short) 0x070D, FitnessData.MessageData.SleepResponse.class);

        responsePacketTypes.put((short) 0x0b01, FindPhoneResponse.class);

        responsePacketTypes.put((short) 0x1707, Workout.WorkoutCount.Response.class);
        responsePacketTypes.put((short) 0x1708, Workout.WorkoutTotals.Response.class);
        responsePacketTypes.put((short) 0x170a, Workout.WorkoutData.Response.class);
        responsePacketTypes.put((short) 0x170c, Workout.WorkoutPace.Response.class);

        responsePacketTypes.put((short) 0x2501, MusicControl.MusicStatusResponse.class);
        responsePacketTypes.put((short) 0x2502, MusicControl.MusicInfo.Response.class);
        responsePacketTypes.put((short) 0x2503, MusicControl.Control.Response.class);
    }

    public HuaweiPacket(ParamsProvider paramsProvider) {
        this.paramsProvider = paramsProvider;
    }

    /*
     * This function is to convert the Packet into the proper subclass
     */
    protected HuaweiPacket fromPacket(HuaweiPacket packet) throws ParseException {
        this.paramsProvider = packet.paramsProvider;
        this.serviceId = packet.serviceId;
        this.commandId = packet.commandId;
        this.tlv = packet.tlv;
        this.partialPacket = packet.partialPacket;
        this.payload = packet.payload;
        this.complete = packet.complete;

        if (this.tlv.contains(0x7C) && this.tlv.getBoolean(0x7C)) {
            try {
                this.tlv.decrypt(paramsProvider.getSecretKey());
            } catch (HuaweiTLV.CryptoException e) {
                e.printStackTrace();
                throw new CryptoException("Decrypt exception", e);
            }
        } else {
            if (this.isEncrypted) {
                // TODO: potentially a log message? We expect it to be encrypted, but it isn't.
            }
        }

        return this;
    }

    /*
     * This function is to set up the subclass for easy usage
     * Needs to be called separately so the exceptions can be used more easily
     */
    public void parseTlv() throws ParseException {}

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
             return packetType.getDeclaredConstructor(ParamsProvider.class).newInstance(paramsProvider).fromPacket(this);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            // The new instance cannot be created, so the packet is returned as "raw packet"
            return this;
        }
    }

    public byte[] serialize() throws CryptoException {
        // TODO: necessary for this to work:
        //       - serviceId
        //       - commandId
        //       - tlv
        // TODO: maybe use the complete flag to know if it can be serialized?

        HuaweiTLV serializableTlv;
        if (this.isEncrypted) {
            try {
                serializableTlv = this.tlv.encrypt(paramsProvider.getSecretKey(), paramsProvider.getIv());
            } catch (HuaweiTLV.CryptoException e) {
                e.printStackTrace();
                throw new CryptoException("Encrypt exception", e);
            }
        } else {
            serializableTlv = this.tlv;
        }

        int headerLength = 4; // Magic + (short)(bodyLength + 1) + 0x00
        int bodyHeaderLength = 2; // sID + cID
        int footerLength = 2; //CRC16
        byte[] serializedTLV = serializableTlv.serialize();
        if (isSliced) {
            headerLength += 1; //Add extra slice info
            ByteBuffer tlvBuffer = ByteBuffer.wrap(serializedTLV);
            int numberPacket = (int)Math.ceil(tlvBuffer.capacity() / paramsProvider.getMtu());
            int numberSlice = (int)Math.ceil(numberPacket);
            if (numberSlice >= 3) {numberSlice = 3;}
            int numberPacektInSlice = (int)Math.ceil(numberPacket / numberSlice);
            int maxSliceSize = paramsProvider.getMtu() * numberPacektInSlice;
            byte[] packet = new byte[tlvBuffer.capacity() + numberSlice*(headerLength + footerLength) + bodyHeaderLength];
            int packetPos = 0x00;
            int slice = 0x00;
            int bodyPos = 0x00;
            while (tlvBuffer.hasRemaining()) {
                int bufferSize = Math.min(maxSliceSize - footerLength, (tlvBuffer.remaining() + headerLength));
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
                buffer.put((byte) 0x5A);
                int bodyLength = bufferSize - headerLength;
                if (slice == 0x00) bodyLength -= bodyHeaderLength;
                buffer.putShort((short)(bodyLength + bodyHeaderLength + footerLength));
                if (tlvBuffer.remaining() < (maxSliceSize - (headerLength + footerLength))) slice = 0x02;
                buffer.put((byte)(slice + 1))
                    .put((byte)slice);
                if (slice == 0x00) {
                    buffer.put(this.serviceId)
                        .put(this.commandId)
                        .put(serializedTLV, bodyPos, bodyLength);
                } else {
                    buffer.put(serializedTLV, bodyPos, bodyLength);
                }
                int crc16 = CheckSums.getCRC16(buffer.array(), 0x0000);
                ByteBuffer finalBuffer = ByteBuffer.allocate(buffer.capacity() + footerLength);
                finalBuffer.put(buffer)
                    .putShort((short)crc16);
                slice += 0x01;
                bodyPos += bodyLength;
                finalBuffer.get(packet, packetPos, finalBuffer.capacity());
                packetPos += finalBuffer.capacity();
            }
            return packet;
        } else {
            int bodyLength = bodyHeaderLength + serializedTLV.length;
            ByteBuffer buffer = ByteBuffer.allocate(headerLength + bodyLength);
            buffer.put((byte) 0x5A);
            buffer.putShort((short)(bodyLength + 1));
            buffer.put((byte) 0x00);
            buffer.put(this.serviceId);
            buffer.put(this.commandId);
            buffer.put(serializedTLV);
            int crc16 = CheckSums.getCRC16(buffer.array(), 0x0000);
            ByteBuffer finalBuffer = ByteBuffer.allocate(buffer.capacity() + footerLength);
            finalBuffer.put(buffer.array());
            finalBuffer.putShort((short)crc16);
            return finalBuffer.array();
        }
    }
}
