package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class MusicControl {
    public static final byte id = 0x25;

    // TODO: should this be in HuaweiConstants?
    public static final int successValue = 0x000186A0;

    public static class MusicStatusRequest extends HuaweiPacket {
        public MusicStatusRequest(SecretsProvider secretsProvider, byte commandId, int returnValue) {
            super(secretsProvider);

            this.serviceId = MusicControl.id;
            this.commandId = commandId;
            this.tlv = new HuaweiTLV()
                    .put(0x7F, returnValue);
            this.isEncrypted = false;
            this.complete = true;
        }
    }

    public static class MusicStatusResponse extends HuaweiPacket {
        public static final byte id = 0x01;

        public int status = -1;

        public MusicStatusResponse(SecretsProvider secretsProvider) {
            super(secretsProvider);

            this.serviceId = MusicControl.id;
            this.commandId = id;
        }

        @Override
        protected void parseTlv() {
            if (this.tlv.contains(0x01) && this.tlv.getBytes(0x01).length == 4)
                this.status = this.tlv.getInteger(0x01);
        }
    }

    public static class MusicInfo {
        public static final byte id = 0x02;

        public static class Request extends HuaweiPacket {
            public Request(
                    SecretsProvider secretsProvider,
                    String artistName,
                    String songName,
                    byte playState,
                    byte maxVolume,
                    byte currentVolume
            ) {
                super(secretsProvider);
                this.serviceId = MusicControl.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x01, artistName)
                        .put(0x02, songName)
                        .put(0x03, playState)
                        .put(0x04, maxVolume)
                        .put(0x05, currentVolume);
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public boolean ok = false;
            public String error = "No input has been parsed yet";

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = MusicControl.id;
                this.commandId = id;

                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() {
                if (this.tlv.contains(0x7F)) {
                    if (this.tlv.getInteger(0x7F) == successValue) {
                        this.ok = true;
                        this.error = "";
                    } else {
                        this.ok = false;
                        this.error = "Music information error code: " + Integer.toHexString(this.tlv.getInteger(0x7F));
                    }
                } else {
                    this.ok = false;
                    this.error = "Music information response no status tag";
                }
            }
        }
    }

    public static class Control {
        public static final byte id = 0x03;

        public static class Response extends HuaweiPacket {
            public enum Button {
                Unknown,
                PlayPause,
                Previous,
                Next
            }

            public boolean buttonPresent = false;
            public byte rawButton = 0x00;
            public boolean volumePresent = false;
            public byte volume = 0x00;

            public Button button = null;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = MusicControl.id;
                this.commandId = id;

                this.isEncrypted = false;
            }

            @Override
            protected void parseTlv() {
                if (this.tlv.contains(0x01)) {
                    this.buttonPresent = true;
                    this.rawButton = this.tlv.getByte(0x01);
                    switch (this.rawButton) {
                        case 1:
                            this.button = Button.PlayPause;
                            break;
                        case 3:
                            this.button = Button.Previous;
                            break;
                        case 4:
                            this.button = Button.Next;
                            break;
                        default:
                            this.button = Button.Unknown;
                    }
                }

                if (this.tlv.contains(0x02)) {
                    this.volumePresent = true;
                    this.volume = this.tlv.getByte(0x02);
                }
            }
        }
    }
}
