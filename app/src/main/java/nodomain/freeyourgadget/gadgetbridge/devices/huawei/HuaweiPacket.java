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

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.util.CheckSums;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants.HUAWEI_MAGIC;

public class HuaweiPacket {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiPacket.class);

    protected static final int PACKET_MINSIZE = 6;

    public byte serviceId;
    public byte commandId;
    public HuaweiTLV tlv;
    public byte [] partialPacket;
    private byte[] payload;
    public boolean complete;

    public HuaweiPacket() {
        this.serviceId = 0;
        this.commandId = 0;
        this.tlv = null;
        this.partialPacket = null;
        this.payload = new byte[0];
        this.complete = false;
    }

    public HuaweiPacket(int serviceId, int commandId, HuaweiTLV tlv) {
        this.serviceId = (byte) serviceId;
        this.commandId = (byte) commandId;
        this.tlv = tlv;
        this.partialPacket = null;
        this.payload = new byte[0];
        this.complete = false;
    }

    public static int length() {
        return 0;
    }

    public HuaweiPacket parse(byte[] data) throws GBException {

        if (partialPacket != null) {
            int newCapacity = partialPacket.length + data.length;
            data = ByteBuffer.allocate(newCapacity)
                .put(partialPacket)
                .put(data)
                .array();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);

        if (buffer.capacity() < PACKET_MINSIZE) {
            throw new GBException("Packet length mismatch : "
                            + String.valueOf(buffer.capacity())
                            + " != 6"
                        );

        }

        byte magic = buffer.get();
        short expectedSize = buffer.getShort();
        int isSLiced = buffer.get();
        byte[] newPayload = new byte[buffer.remaining() - 2];
        buffer.get(newPayload, 0, buffer.remaining() - 2);
        short expectedChecksum = buffer.getShort();
        buffer.rewind();

        if ( magic != HUAWEI_MAGIC) {
            throw new GBException("Magic mismatch : "
                            + Integer.toHexString(magic)
                            + " != 0x5A"
                        );
        }
        if ( expectedSize != (short)(newPayload.length + 1)) {
            // Older band and BT version do not handle message with more than 256 bits.
            this.serviceId = (byte)newPayload[0];
            this.commandId = (byte)newPayload[1];
            this.partialPacket = data;
            return this;
        }

        byte[] dataNoCRC = new byte[buffer.capacity() - 2];
        buffer.get(dataNoCRC, 0, buffer.capacity() - 2);
        short actualChecksum = (short)CheckSums.getCRC16(dataNoCRC, 0x0000);
        if (actualChecksum != expectedChecksum) {
            throw new GBException("Checksum mismatch : "
                            + String.valueOf(actualChecksum)
                            + " != "
                            + String.valueOf(expectedChecksum)
                        );
        }

        if (isSLiced == 1 || isSLiced == 2 || isSLiced == 3) {
            LOG.debug("IsSliced");
            int newCapacity = payload.length + newPayload.length - 1;
            newPayload = ByteBuffer.allocate(newCapacity)
                .put(payload)
                .put(buffer.array(), 5, buffer.capacity() - 7)
                .array();
        }
        LOG.debug("Parsed packet values :\n"
                    + "Service ID: " + newPayload[0] + " - Command ID: " + newPayload[1] + "\n"
                    // + "Magic: " + Integer.toHexString(magic) + "\n"
                    // + "expectedSize: " + String.valueOf(expectedSize) + "\n"
                    // + "isSLiced: " + String.valueOf(isSLiced) + "\n"
                    + "newPayload: " + StringUtils.bytesToHex(newPayload) + "\n"
                    // + "expectedChecksum: " + Integer.toHexString(0xFFFF & expectedChecksum)
        );

        this.serviceId = (byte)newPayload[0];
        this.commandId = (byte)newPayload[1];
        if (isSLiced == 0 || isSLiced == 3) {
            this.tlv = new HuaweiTLV();
            this.tlv.parse(newPayload, 2, newPayload.length - 2);
            this.complete = true;
        } else {
            this.payload = newPayload;
        }
        return this;
    }

    public byte[] serialize() {
        int headerLength = 1 + 2 + 1; //Magic + bodyLength+1 + 0x00
        byte[] serializedTLV = this.tlv.serialize();
        int bodyLength = 1 + 1 + serializedTLV.length;
        ByteBuffer buffer = ByteBuffer.allocate(headerLength + bodyLength);
        buffer.put((byte)0x5A);
        buffer.putShort((short)(bodyLength + 1));
        buffer.put((byte)0x00);
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

    public HuaweiPacket encrypt(byte[] key, byte[] iv) {
        tlv.encrypt(key, iv);
        return this;
    }

    public HuaweiPacket decrypt(byte[] key) {
        tlv.decrypt(key);
        return this;
    }

    public String toString() {
        return "Packet : Service ID: " + serviceId + " - Command ID: " + commandId + "\n"
                    + "TLV: " + tlv.toString() + "\n";
    }

}
