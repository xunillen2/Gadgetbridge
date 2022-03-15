package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

public class FitnessData {
    public static final int id = 0x07;

    public static class ActivityReminder {
        public static final int id = 0x07;

        public static final int container = 0x81;
        public static final int longsitSwitch = 0x02;
        public static final int longsitInterval = 0x03;
        public static final int longsitStart = 0x04;
        public static final int longsitEnd = 0x05;
        public static final int longsitCycle = 0x06;
    }

    public static class TruSleep {
        public static final int id = 0x16;

        public static final int trusleepSwitch = 0x01;
    }
}
