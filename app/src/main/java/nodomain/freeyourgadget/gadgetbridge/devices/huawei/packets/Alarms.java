package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class Alarms {

    public static final int id = 0x08;

    public static class EventAlarms {
        public static final int id = 0x01;

        public static class Request {
            int count;
            HuaweiTLV alarms;

            public Request() {
                count = 0;
                alarms = new HuaweiTLV();
            }

            public void addAlarm(
                        byte index,
                        boolean status,
                        short startTime,
                        byte repeat,
                        String name
            ) {
                // TODO: If 4 is a real maximum, we may want to check for that here as well
                //       Then we should define and throw an exception

                alarms.put(0x82, new HuaweiTLV()
                        .put(0x03, index)
                        .put(0x04, status)
                        .put(0x05, startTime)
                        .put(0x06, repeat)
                        .put(0x07, name)
                );
                count += 1;
            }

            public HuaweiTLV toTlv() {
                // TODO: If 4 is a real maximum, we may want to check for that here as well
                //       Then we should define and throw an exception

                alarms.put(0x82, new HuaweiTLV()
                        .put(0x03, (byte) (count + 1))
                );
                return new HuaweiTLV()
                        .put(0x81, alarms);
            }
        }
    }

    public static class SmartAlarms {
        public static final int id = 0x02;
        public static final int index = 0x03; // byte
        public static final int setStatus = 0x04; // byte
        public static final int setStartTime = 0x05; //short
        public static final int repeat = 0x06; // byte
        public static final int aheadTime = 0x07; // byte default value 5 min

        public static final int start = 0x81;
        public static final int separator = 0x82;
    }

    // getDeviceEventAlarm
    public static class EventAlarmsList {
        public static final int id = 0x03; // byte default value 0
        public static final int get = 0x01; // byte
    }

    // getDeviceSmartAlarm
    public static class SmartAlarmsList {
        public static final int id = 0x04; // byte default value 0
        public static final int get = 0x01; // byte
    }
}
