package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

// TODO: complete responses

public class Alarms {

    public static final byte id = 0x08;

    public static class EventAlarmsRequest extends HuaweiPacket {
        public static final byte id = 0x01;

        byte count;
        HuaweiTLV alarms;

        public EventAlarmsRequest(ParamsProvider paramsProvider) {
            super(paramsProvider);

            this.serviceId = Alarms.id;
            this.commandId = id;

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

        @Override
        public byte[] serialize() throws CryptoException {
            // Finalize the tlv before serializing
            this.alarms.put(0x82, new HuaweiTLV()
                    .put(0x03, (byte) (count + 1))
            );
            this.tlv = new HuaweiTLV().put(0x81, this.alarms);
            this.complete = true;

            return super.serialize();
        }
    }

    public static class SmartAlarmRequest extends HuaweiPacket {
        public static final int id = 0x02;

        public SmartAlarmRequest(
                ParamsProvider paramsProvider,
                boolean status,
                short startTime,
                byte repeat,
                byte aheadTime
        ) {
            super(paramsProvider);

            this.serviceId = Alarms.id;
            this.commandId = id;
            this.tlv = new HuaweiTLV()
                    .put(0x81, new HuaweiTLV()
                            .put(0x82, new HuaweiTLV()
                                    .put(0x03, (byte) 0x01)
                                    .put(0x04, status)
                                    .put(0x05, startTime)
                                    .put(0x06, repeat)
                                    .put(0x07, aheadTime)
                            )
                    );
            this.complete = true;
        }
    }

    // TODO: refactor
    // getDeviceEventAlarm
    public static class EventAlarmsList {
        public static final int id = 0x03; // byte default value 0
        public static final int get = 0x01; // byte
    }

    // TODO: refactor
    // getDeviceSmartAlarm
    public static class SmartAlarmsList {
        public static final int id = 0x04; // byte default value 0
        public static final int get = 0x01; // byte
    }
}
