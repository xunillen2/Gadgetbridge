package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

import org.junit.Assert;
import org.junit.Test;

public class TestAlarms {

    @Test
    public void EventAlarmsRequest() {
        HuaweiTLV expectedAlarms = new HuaweiTLV();
        Alarms.EventAlarms.Request eventAlarmRequest = new Alarms.EventAlarms.Request();

        Assert.assertEquals(0, eventAlarmRequest.count);
        Assert.assertEquals(expectedAlarms, eventAlarmRequest.alarms);

        expectedAlarms.put(0x82,new HuaweiTLV()
                .put(0x03, (byte) 1)
                .put(0x04, true)
                .put(0x05, (short) 0x1337)
                .put(0x06, (byte) 0)
                .put(0x07, "Alarm1")
        );

        eventAlarmRequest.addAlarm(
                (byte) 1,
                true,
                (short) 0x1337,
                (byte) 0,
                "Alarm1"
        );

        Assert.assertEquals(1, eventAlarmRequest.count);
        Assert.assertEquals(expectedAlarms, eventAlarmRequest.alarms);

        expectedAlarms.put(0x82,new HuaweiTLV()
                .put(0x03, (byte) 2)
                .put(0x04, false)
                .put(0x05, (short) 0xCAFE)
                .put(0x06, (byte) 1)
                .put(0x07, "Alarm2")
        );

        eventAlarmRequest.addAlarm(
                (byte) 2,
                false,
                (short) 0xCAFE,
                (byte) 1,
                "Alarm2"
        );

        Assert.assertEquals(2, eventAlarmRequest.count);
        Assert.assertEquals(expectedAlarms, eventAlarmRequest.alarms);

        expectedAlarms.put(0x82, new HuaweiTLV()
                .put(0x03, (byte) 3)
        );
        HuaweiTLV expectedOutput = new HuaweiTLV()
                .put(0x81, expectedAlarms);

        HuaweiTLV output = eventAlarmRequest.toTlv();

        Assert.assertEquals(expectedOutput, output);
    }
}
