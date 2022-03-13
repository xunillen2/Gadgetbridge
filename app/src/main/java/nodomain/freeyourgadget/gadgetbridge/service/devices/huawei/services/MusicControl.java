package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

public class MusicControl {
    public static final int id = 0x25;

    public static final int statusTag = 0x7F;
    public static final int successValue = 0x0186A0;

    /*
     * This is the message from the band to retrieve the music info from the phone
     */
    public static class MusicInfoResponse {
        public static final int id = 0x01;
    }

    public static class MusicInfo {
        public static final int id = 0x02;

        public static final int artistNameTag = 0x01;
        public static final int songNameTag = 0x02;
        public static final int playStateTag = 0x03;
        public static final int maxVolumeTag = 0x04;
        public static final int currentVolumeTag = 0x05;
    }

    public static class Control {
        public static final int id = 0x03;

        public static final int buttonTag = 0x01;
        public static final int volumeTag = 0x02;
    }
}
