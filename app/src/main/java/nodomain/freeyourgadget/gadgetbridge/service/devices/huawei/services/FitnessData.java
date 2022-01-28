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
        public static final int sleepId = 0x0C;
        public static final int stepId = 0x0A;

        public static final int requestUnknownTag = 0x81;
        public static final int requestStartTag = 0x03;
        public static final int requestEndTag = 0x04;

        public static final int responseContainerTag = 0x81;
        public static final int responseContainerCountTag = 0x02;
    }

    public static class MessageData {
        public static final int seepId = 0x0D;
        public static final int stepId = 0x0B;

        public static final int requestContainerTag = 0x81;
        public static final int requestContainerNumberTag = 0x02;

        public static final int responseContainerTag = 0x81;
        public static final int responseContainerNumberTag = 0x02;

        public static final int sleepResponseContainerContainerTag = 0x83;
        public static final int sleepResponseContainerContainerDataTag = 0x04;
        public static final int sleepResponseContainerContainerTimestampTag = 0x05;

        public static final int stepResponseContainerTimestampTag = 0x03;
        public static final int stepResponseContainerContainerTag = 0x84;
        public static final int stepResponseContainerContainerTimeOffsetTag = 0x05;
        public static final int stepResponseContainerContainerDataTag = 0x06;
    }

    public static class TruSleep {
        public static final int id = 0x16;

        public static final int trusleepSwitch = 0x01;
    }
}
