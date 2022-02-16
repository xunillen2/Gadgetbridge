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

        public static final int request_unknown_tag = 0x81;
        public static final int request_start_tag = 0x03;
        public static final int request_end_tag = 0x04;

        public static final int response_container_tag = 0x81;
        public static final int response_container_count_tag = 0x02;
    }

    public static class MessageData {
        public static final int id = 0x0D;

        public static final int request_container_tag = 0x81;
        public static final int request_container_number_tag = 0x02;

        public static final int response_container_tag = 0x81;
        public static final int response_container_number_tag = 0x02;
        public static final int response_container_container_tag = 0x83;
        public static final int response_container_container_data_tag = 0x04;
        public static final int response_container_container_timestamp_tag = 0x05;
    }

    public static class TruSleep {
        public static final int id = 0x16;

        public static final int TrusleepSwitch = 0x01;
    }
}
