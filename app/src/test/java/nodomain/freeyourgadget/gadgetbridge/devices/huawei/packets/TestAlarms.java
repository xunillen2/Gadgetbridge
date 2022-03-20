package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class TestAlarms {

    HuaweiPacket.SecretsProvider secretsProvider = new HuaweiPacket.SecretsProvider() {
        @Override
        public byte[] getSecretKey() {
            return new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        }

        @Override
        public byte[] getIv() {
            return new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        }
    };

    @Test
    public void testEventAlarmsRequest() throws NoSuchFieldException, IllegalAccessException {
        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        Field alarmField = Alarms.EventAlarmsRequest.class.getDeclaredField("alarms");
        alarmField.setAccessible(true);

        HuaweiTLV expectedAlarmsTlv = new HuaweiTLV();

        Alarms.EventAlarmsRequest request = new Alarms.EventAlarmsRequest(secretsProvider);

        expectedAlarmsTlv.put(0x82, new HuaweiTLV()
                .put(0x03, (byte) 1)
                .put(0x04, true)
                .put(0x05, (short) 0x1337)
                .put(0x06, (byte) 0)
                .put(0x07, "Alarm1")
        );

        request.addAlarm(
                (byte) 1,
                true,
                (short) 0x1337,
                (byte) 0,
                "Alarm1"
        );

        Assert.assertEquals(0x08, request.serviceId);
        Assert.assertEquals(0x01, request.commandId);
        Assert.assertEquals(1, request.count);
        Assert.assertEquals(expectedAlarmsTlv, alarmField.get(request));

        // A serialize will change the tlv, so we cannot test it here

        expectedAlarmsTlv.put(0x82,new HuaweiTLV()
                .put(0x03, (byte) 2)
                .put(0x04, false)
                .put(0x05, (short) 0xCAFE)
                .put(0x06, (byte) 1)
                .put(0x07, "Alarm2")
        );

        request.addAlarm(
                (byte) 2,
                false,
                (short) 0xCAFE,
                (byte) 1,
                "Alarm2"
        );

        Assert.assertEquals(2, request.count);
        Assert.assertEquals(expectedAlarmsTlv, alarmField.get(request));

        expectedAlarmsTlv.put(0x82, new HuaweiTLV()
                .put(0x03, (byte) 3)
        );
        HuaweiTLV expectedTlv = new HuaweiTLV().put(0x81, expectedAlarmsTlv);
        expectedTlv.encrypt(secretsProvider.getSecretKey(), secretsProvider.getIv());

        byte[] expectedOutput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x5a, (byte) 0x00, (byte) 0x08, (byte) 0x01, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x40, (byte) 0x02, (byte) 0x2d, (byte) 0x19, (byte) 0x47, (byte) 0xfa, (byte) 0x9d, (byte) 0x71, (byte) 0xb7, (byte) 0xc9, (byte) 0x16, (byte) 0xd0, (byte) 0x2b, (byte) 0x7f, (byte) 0x98, (byte) 0x35, (byte) 0xd1, (byte) 0x5c, (byte) 0x20, (byte) 0x42, (byte) 0xba, (byte) 0x62, (byte) 0xcd, (byte) 0x52, (byte) 0xde, (byte) 0xdb, (byte) 0x0c, (byte) 0x85, (byte) 0x58, (byte) 0xa3, (byte) 0x3c, (byte) 0x1f, (byte) 0x23, (byte) 0xf5, (byte) 0x6a, (byte) 0xaf, (byte) 0x55, (byte) 0xa2, (byte) 0xf6, (byte) 0x55, (byte) 0x0a, (byte) 0xe0, (byte) 0x08, (byte) 0x26, (byte) 0x19, (byte) 0x13, (byte) 0xd1, (byte) 0x7d, (byte) 0x5f, (byte) 0x4d, (byte) 0x4b, (byte) 0xa3, (byte) 0x36, (byte) 0x8b, (byte) 0xc3, (byte) 0xac, (byte) 0x50, (byte) 0xfd, (byte) 0x90, (byte) 0x73, (byte) 0xba, (byte) 0x52, (byte) 0x2b, (byte) 0x5c, (byte) 0x12, (byte) 0xc1, (byte) 0xe1};

        // Different order for better assertion messages in case of failure
        byte[] output = request.serialize();
        Assert.assertEquals(expectedAlarmsTlv, alarmField.get(request));
        Assert.assertEquals(expectedTlv, tlvField.get(request));
        Assert.assertArrayEquals(expectedOutput, output);
    }

    @Test
    public void testSmartAlarmRequest() throws NoSuchFieldException, IllegalAccessException {
        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        byte[] expectedOutput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x3a, (byte) 0x00, (byte) 0x08, (byte) 0x02, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x20, (byte) 0xcd, (byte) 0x7f, (byte) 0x80, (byte) 0x67, (byte) 0x02, (byte) 0x8d, (byte) 0x46, (byte) 0xfb, (byte) 0xc1, (byte) 0x0b, (byte) 0xed, (byte) 0x6c, (byte) 0x46, (byte) 0xb7, (byte) 0x59, (byte) 0xba, (byte) 0x08, (byte) 0xfd, (byte) 0xde, (byte) 0x3b, (byte) 0xee, (byte) 0x54, (byte) 0xbd, (byte) 0x4f, (byte) 0x27, (byte) 0xf6, (byte) 0x52, (byte) 0x9a, (byte) 0xae, (byte) 0xbf, (byte) 0x55, (byte) 0xd9, (byte) 0xe0, (byte) 0xa6};

        HuaweiTLV expectedTlv = new HuaweiTLV()
                .put(0x81, new HuaweiTLV()
                        .put(0x82, new HuaweiTLV()
                                .put(0x03, (byte) 0x01)
                                .put(0x04, true)
                                .put(0x05, (short) 0x1337)
                                .put(0x06, (byte) 1)
                                .put(0x07, (byte) 2)
                        )
                );
        expectedTlv.encrypt(secretsProvider.getSecretKey(), secretsProvider.getIv());

        Alarms.SmartAlarmRequest request = new Alarms.SmartAlarmRequest(
                secretsProvider,
                true,
                (short) 0x1337,
                (byte) 1,
                (byte) 2
        );

        Assert.assertEquals(0x08, request.serviceId);
        Assert.assertEquals(0x02, request.commandId);
        Assert.assertTrue(request.complete);
        Assert.assertEquals(expectedTlv, tlvField.get(request));
        Assert.assertArrayEquals(expectedOutput, request.serialize());
    }
}
