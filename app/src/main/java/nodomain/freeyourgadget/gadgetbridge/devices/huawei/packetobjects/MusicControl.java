package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packetobjects;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class MusicControl {
    public static final int id = 0x25;

    public static final int statusTag = 0x7F;
    public static final int successValue = 0x0186A0;

    public static class MusicInfoResponse {
        public static final int id = 0x01;
    }

    public static class MusicInfo {
        public static final int id = 0x02;

        public static class Request {
            public static HuaweiTLV toTlv(
                    String artistName,
                    String songName,
                    byte playState,
                    byte maxVolume,
                    byte currentVolume
            ) {
                return new HuaweiTLV()
                        .put(0x01, artistName)
                        .put(0x02, songName)
                        .put(0x03, playState)
                        .put(0x04, maxVolume)
                        .put(0x05, currentVolume);
            }
        }

        public static class Response {
            public boolean ok = false;
            public String error = "No input has been parsed yet";

            public static Response fromTlv(HuaweiTLV input) {
                Response response = new Response();
                if (input.contains(0x7F)) {
                    if (input.getShort(0x7F) == 0x0186A0) {
                        response.ok = true;
                        response.error = "";
                    } else {
                        response.ok = false;
                        response.error = "Music information error code: " + Integer.toHexString(input.getShort(MusicControl.statusTag));
                    }
                } else {
                    response.ok = false;
                    response.error = "Music information response no status tag";
                }
                return response;
            }
        }
    }

    public static class Control {
        public static final int id = 0x03;

        public static class Response {
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

            public static Response fromTlv(HuaweiTLV input) {
                Response response = new Response();

                if (input.contains(0x01)) {
                    response.buttonPresent = true;
                    response.rawButton = input.getByte(0x01);
                    switch (response.rawButton) {
                        case 1:
                            response.button = Button.PlayPause;
                            break;
                        case 3:
                            response.button = Button.Previous;
                            break;
                        case 4:
                            response.button = Button.Next;
                            break;
                        default:
                            response.button = Button.Unknown;
                    }
                }

                if (input.contains(0x02)) {
                    response.volumePresent = true;
                    response.volume = input.getByte(0x02);
                }

                return response;
            }
        }
    }
}
