package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

public class SleepData {
    public static final int id = 0x07;

    public static class MessageCount {
        public static final int id = 0x0c;

        public static final int request_unknown_tag = 0x81;
        public static final int request_start_tag = 0x03;
        public static final int request_end_tag = 0x04;

        public static final int response_container_tag = 0x81;
        public static final int response_container_count_tag = 0x02;
    }

    public static class MessageData {
        public static final int id = 0x0d;

        public static final int request_container_tag = 0x81;
        public static final int request_container_number_tag = 0x02;

        public static final int response_container_tag = 0x81;
        public static final int response_container_number_tag = 0x02;
        public static final int response_container_container_tag = 0x83;
        public static final int response_container_container_data_tag = 0x04;
        public static final int response_container_container_timestamp_tag = 0x05;
    }
}
