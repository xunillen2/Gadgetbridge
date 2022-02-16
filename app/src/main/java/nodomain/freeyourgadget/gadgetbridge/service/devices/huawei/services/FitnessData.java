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

    public static class MessageCount {
        public static final int id = 0x0C;

        public static final int unknown = 0x81;
        public static final int start = 0x03;
        public static final int end = 0x04;

        public static final int container = 0x81;
        public static final int containerCount = 0x02;
    }

    public static class MessageData {
        public static final int id = 0x0D;

        public static final int container = 0x81;
        public static final int containerNumber = 0x02;

        public static final int containerContainer = 0x83;
        public static final int containerContainerData = 0x04;
        public static final int containerContainerTimestamp = 0x05;
    }

    public static class TruSleep {
        public static final int id = 0x16;

        public static final int trusleepSwitch = 0x01;
    }
}
