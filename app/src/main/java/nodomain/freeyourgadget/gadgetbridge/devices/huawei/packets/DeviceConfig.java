/*  Copyright (C) 2021-2022 Gaignon Damien

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
package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

// TODO: complete responses

public class DeviceConfig {
    public static final byte id = 0x01;

    public static class LinkParams {
        public static final byte id = 0x01;

        public static class Request extends HuaweiPacket {
            public Request(SecretsProvider secretsProvider) {
                super(secretsProvider);
                this.serviceId = DeviceConfig.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x01)
                        .put(0x02)
                        .put(0x03)
                        .put(0x04);
                this.complete = true;
                this.isEncrypted = false;
            }
        }

        public static class Response extends HuaweiPacket {
            public byte[] serverNonce;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
                this.serviceId = DeviceConfig.id;
                this.commandId = id;
                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (this.tlv.contains(0x05)) {
                    this.serverNonce = this.tlv.getBytes(0x05);
                } else {
                    throw new MissingTagException(0x05);
                }
            }
        }
    }

    public static class SupportedServices {
        public static final byte id = 0x02;

        public static class Request extends HuaweiPacket {
            public Request(SecretsProvider secretsProvider, byte[] allSupportedServices) {
                super(secretsProvider);
                this.serviceId = DeviceConfig.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x01, allSupportedServices);
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public byte[] supportedServices;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (this.tlv.contains(0x02)) {
                    this.supportedServices = this.tlv.getBytes(0x02);
                } else {
                    throw new MissingTagException(0x02);
                }
            }
        }
    }

    public static class SupportedCommands {
        public static final byte id = 0x03;

        public static class Request extends HuaweiPacket {
            public Request(
                    SecretsProvider secretsProvider
            ) {
                super(secretsProvider);
                this.serviceId = DeviceConfig.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV();
            }

            public boolean addCommandsForService(byte service, byte[] commands) {
                if (this.tlv.length() + commands.length + 2 > 208) {
                    return false;
                }
                this.tlv.put(0x02, service).put(0x03, commands);
                return true;
            }

            @Override
            public byte[] serialize() throws CryptoException {
                this.tlv = new HuaweiTLV()
                        .put(0x81, this.tlv);
                this.complete = true;
                return super.serialize();
            }
        }

        public static class Response extends HuaweiPacket {
            public static class CommandsList {
                public int service;
                public byte[] commands;
            }

            public List<CommandsList> commandsLists;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
                this.serviceId = DeviceConfig.id;
                this.commandId = id;
                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() throws ParseException {
                this.commandsLists = new ArrayList<>();
                CommandsList commandsList = null;
                HuaweiTLV containerTLV = this.tlv.getObject(0x81);

                if (!containerTLV.contains(0x02)) {
                    throw new MissingTagException(0x02);
                }
                if (!containerTLV.contains(0x04)) {
                    throw new MissingTagException(0x04);
                }

                for (HuaweiTLV.TLV tlv : containerTLV.get()) {
                    if ((int) tlv.getTag() == 0x02) {
                        commandsList = new CommandsList();
                        commandsList.service = (int) ByteBuffer.wrap(tlv.getValue()).get();
                    } else if ((int) tlv.getTag() == 0x04) {
                        if (commandsList == null) {
                            // TODO: exception
                            return;
                        }
                        ByteBuffer buffer = ByteBuffer.allocate(tlv.getValue().length);
                        for (int i = 0; i < tlv.getValue().length; i++) {
                            if ((int) tlv.getValue()[i] == 1)
                                buffer.put((byte) (i + 1));
                        }
                        commandsList.commands = new byte[buffer.position()];
                        ((ByteBuffer) buffer.rewind()).get(commandsList.commands);
                        this.commandsLists.add(commandsList);
                    } else {
                        // TODO: exception
                        return;
                    }
                }
            }
        }
    }

    public static class SetDateFormatRequest extends HuaweiPacket {
        public static final byte id = 0x04;

        public SetDateFormatRequest(
                SecretsProvider secretsProvider,
                byte dateFormat,
                byte timeFormat
        ) {
            super(secretsProvider);
            this.serviceId = DeviceConfig.id;
            this.commandId = id;
            this.tlv = new HuaweiTLV()
                    .put(0x81, new HuaweiTLV()
                            .put(0x02, dateFormat)
                            .put(0x03, timeFormat)
                    );
            this.complete = true;
        }
    }

    public static class SetTimeRequest extends HuaweiPacket {
        public static final byte id = 0x05;

        public SetTimeRequest(
                SecretsProvider secretsProvider,
                int timestamp,
                short zoneOffset
        ) {
            super(secretsProvider);
            this.serviceId = DeviceConfig.id;
            this.commandId = id;
            this.tlv = new HuaweiTLV()
                    .put(0x01, timestamp)
                    .put(0x02, zoneOffset);
            this.complete = true;
        }
    }

    public static class ProductInfo {
        public static final byte id = 0x07;

        public static class Request extends HuaweiPacket {

            public Request(SecretsProvider secretsProvider) {
                super(secretsProvider);
                this.serviceId = DeviceConfig.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV();
                for (int i = 1; i < 14; i++) {
                    this.tlv.put(i);
                }
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            // TODO: extend:
            //        public static final int BTVersion = 0x01;
            //        public static final int productType = 0x02;
            //        public static final int phoneNumber = 0x04;
            //        public static final int macAddress = 0x05;
            //        public static final int IMEI = 0x06;
            //        public static final int openSourceVersion = 0x08;
            //        public static final int serialNumber = 0x09;
            //        public static final int eMMCId = 0x0B;
            //        public static final int healthAppSupport = 0x0D;

            public String hardwareVersion;
            public String softwareVersion;
            public String productModel;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
                this.serviceId = DeviceConfig.id;
                this.commandId = id;
                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (!this.tlv.contains(0x03)) {
                    throw new MissingTagException(0x03);
                }
                if (!this.tlv.contains(0x07)) {
                    throw new MissingTagException(0x07);
                }
                if (!this.tlv.contains(0x0A)) {
                    throw new MissingTagException(0x0A);
                }
                this.hardwareVersion = this.tlv.getString(0x03);
                this.softwareVersion = this.tlv.getString(0x07);
                this.productModel = this.tlv.getString(0x0A).trim();
            }
        }
    }

    public static class BondRequest extends HuaweiPacket {
        public static final byte id = 0x0E;
        public BondRequest(
                SecretsProvider secretsProvider,
                byte[] clientSerial,
                String mac,
                HuaweiCrypto huaweiCrypto
        ) throws CryptoException {
            super(secretsProvider);
            this.serviceId = DeviceConfig.id;
            this.commandId = id;
            byte[] iv = secretsProvider.getIv();

            try {
                this.tlv = new HuaweiTLV()
                        .put(0x01)
                        .put(0x03, (byte) 0x00)
                        .put(0x05, clientSerial)
                        .put(0x06, huaweiCrypto.createBondingKey(mac, secretsProvider.getSecretKey(), iv))
                        .put(0x07, iv);
                this.isEncrypted = false;
                this.complete = true;
            } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                e.printStackTrace();
                throw new CryptoException("Bonding key creation exception");
            }
        }
    }

    public static class BondParams {
        public static final byte id = 0x0F;

        public static class Request extends HuaweiPacket {
            public Request(
                    SecretsProvider secretsProvider,
                    byte[] clientSerial,
                    byte[] mac
            ) {
                super(secretsProvider);

                this.serviceId = DeviceConfig.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01)
                        .put(0x03, clientSerial)
                        .put(0x04, (byte) 0x02)
                        .put(0x05)
                        .put(0x07, mac)
                        .put(0x09);
                this.isEncrypted = false;
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public byte status;
            public long encryptionCounter;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = DeviceConfig.id;
                this.commandId = id;

                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() {
                this.status = this.tlv.getByte(0x01);
                this.encryptionCounter = this.tlv.getInteger(0x09) & 0xFFFFFFFFL;
            }
        }
    }

    public static class Auth {
        public static final byte id = 0x13;

        public static class Request extends HuaweiPacket {
            public Request(
                    SecretsProvider secretsProvider,
                    byte[] challenge,
                    byte[] nonce
            ) {
                super(secretsProvider);

                this.serviceId = DeviceConfig.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01, challenge)
                        .put(0x02, nonce);
                this.isEncrypted = false;
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public byte[] challengeResponse;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = DeviceConfig.id;
                this.commandId = id;

                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() {
                this.challengeResponse = this.tlv.getBytes(0x01);
            }
        }
    }

    public static class BatteryLevel {
        public static final byte id = 0x08;

        public static class Request extends HuaweiPacket {
            public Request(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = DeviceConfig.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01);
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public byte level;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);

                // This differs per watch, so we handle it ourselves in parseTlv
                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (this.tlv.contains(0x7C) && this.tlv.getByte(0x7C) == 0x01) {
                     try {
                         this.tlv.decrypt(secretsProvider.getSecretKey());
                     } catch (HuaweiTLV.CryptoException e) {
                         e.printStackTrace();
                         throw new CryptoException("Decrypt exception");
                     }
                }
                if (this.tlv.contains(0x01))
                    this.level = this.tlv.getByte(0x01);
                else
                    throw new MissingTagException(0x01);
            }
        }
    }

    public static class ActivateOnRotateRequest extends HuaweiPacket {
        public static final byte id = 0x09;

        public ActivateOnRotateRequest(SecretsProvider secretsProvider, boolean activate) {
            super(secretsProvider);

            this.serviceId = DeviceConfig.id;
            this.commandId = id;

            this.tlv = new HuaweiTLV()
                    .put(0x01, activate);

            this.complete = true;
        }
    }

    public static class DndDeleteRequest extends HuaweiPacket {
        public static final int id = 0x0B;

        public DndDeleteRequest(HuaweiPacket.SecretsProvider secretsProvider) {
            super(secretsProvider);

            this.serviceId = DeviceConfig.id;
            this.commandId = id;

            this.tlv = new HuaweiTLV()
                .put(0x81, new HuaweiTLV()
                    .put(0x02, (byte) 0x01)
                );
            this.complete = true;
        }
    }

    public static class DndAddRequest extends HuaweiPacket {
        public static final int id = 0x0C;

        public DndAddRequest(
            HuaweiPacket.SecretsProvider secretsProvider,
            boolean dndEnable,
            byte[] start,
            byte[] end,
            int cycle,
            int dndPriority,
            boolean statusLiftWrist,
            boolean statusDndLiftWrist
        ) {
            super(secretsProvider);

            this.serviceId = DeviceConfig.id;
            this.commandId = id;

            HuaweiTLV dndPacket = new HuaweiTLV()
                    .put(0x02, (byte) 0x01)
                    .put(0x03, dndEnable)
                    .put(0x04, (byte) 0x00)
                    .put(0x05, start)
                    .put(0x06, end)
                    .put(0x07, (byte) cycle);

            if (dndPriority == 0x14) {
                dndPacket.put(0x08, (short) ((statusLiftWrist && statusDndLiftWrist) ? dndPriority : 0x00));
            }
            this.tlv = new HuaweiTLV()
                    .put(0x81, dndPacket);
            this.complete = true;
        }
    }

    public static class FactoryResetRequest extends HuaweiPacket {
        public static final byte id = 0x0D;

        public FactoryResetRequest(HuaweiPacket.SecretsProvider secretsProvider) {
            super(secretsProvider);

            this.serviceId = DeviceConfig.id;
            this.commandId = id;

            this.tlv = new HuaweiTLV()
                    .put(0x01, (byte) 0x01);

            this.complete = true;
        }
    }

    public static class NavigateOnRotateRequest extends HuaweiPacket {
        public static final byte id = 0x1B;

        public NavigateOnRotateRequest(HuaweiPacket.SecretsProvider secretsProvider, boolean navigate) {
            super(secretsProvider);

            this.serviceId = DeviceConfig.id;
            this.commandId = id;

            this.tlv = new HuaweiTLV()
                    .put(0x01, navigate);

            this.complete = true;
        }
    }

    public static class WearLocationRequest extends HuaweiPacket {
        public static final byte id = 0x1A;

        public WearLocationRequest(HuaweiPacket.SecretsProvider secretsProvider, byte location) {
            super(secretsProvider);

            this.serviceId = DeviceConfig.id;
            this.commandId = id;

            this.tlv = new HuaweiTLV()
                    .put(0x01, location);

            this.complete = true;
        }
    }

    public static class DndPriority {
        public static final int id = 0x1D;

        public static class Request extends HuaweiPacket {
            public Request(HuaweiPacket.SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = DeviceConfig.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                    .put(0x01);
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public int priority;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
            }

            @Override
            protected void parseTlv() {
                this.priority = (int) this.tlv.getShort(0x01);
            }
        }
    }

    // TODO: wear location enum?

    public static class Date {
        // TODO: enum?

        public static final int yearFirst = 0x01;
        public static final int monthFirst = 0x02;
        public static final int dayFirst = 0x03;
    }

    public static class Time {
        // TODO: enum?

        public static final int hours12 = 0x01;
        public static final int hours24 = 0x02;
    }
}
