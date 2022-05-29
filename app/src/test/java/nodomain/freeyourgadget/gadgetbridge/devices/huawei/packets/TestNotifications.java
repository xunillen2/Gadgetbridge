package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class TestNotifications {

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

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testNotificationActionRequest() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.CryptoException {
        short notificationId = 0x01;
        byte notificationType = 0x02;
        boolean vibrate = false;
        byte titleEncoding = 0x02;
        String titleContent = "Title";
        byte senderEncoding = 0x02;
        String senderContent = "Sender";
        byte bodyEncoding = 0x02;
        String bodyContent = "Body";
        String sourceAppId = "SourceApp";

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV()
                .put(0x01, notificationId)
                .put(0x02, notificationType)
                .put(0x03, vibrate)
                .put(0x84, new HuaweiTLV()
                        .put(0x8C, new HuaweiTLV()
                                .put(0x8D, new HuaweiTLV()
                                        .put(0x0E, (byte) 0x03)
                                        .put(0x0F, titleEncoding)
                                        .put(0x10, titleContent)
                                )
                                .put(0x8D, new HuaweiTLV()
                                        .put(0x0E, (byte) 0x02)
                                        .put(0x0F, senderEncoding)
                                        .put(0x10, senderContent)
                                )
                                .put(0x8D, new HuaweiTLV()
                                        .put(0x0E, (byte) 0x01)
                                        .put(0x0F, bodyEncoding)
                                        .put(0x10, bodyContent)
                                )
                        )
                )
                .put(0x11, sourceAppId);

        byte[] expectedOutput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x6a, (byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x50, (byte) 0x47, (byte) 0x4d, (byte) 0x65, (byte) 0xa1, (byte) 0xd9, (byte) 0x54, (byte) 0x5d, (byte) 0xa9, (byte) 0x39, (byte) 0x90, (byte) 0xba, (byte) 0x6e, (byte) 0x59, (byte) 0x61, (byte) 0x9b, (byte) 0x85, (byte) 0x5d, (byte) 0x69, (byte) 0xb1, (byte) 0x56, (byte) 0x69, (byte) 0x4e, (byte) 0x5e, (byte) 0xb5, (byte) 0x79, (byte) 0x29, (byte) 0xb3, (byte) 0xb7, (byte) 0xa2, (byte) 0xfd, (byte) 0x2c, (byte) 0x19, (byte) 0xc8, (byte) 0x09, (byte) 0x1a, (byte) 0x52, (byte) 0xba, (byte) 0x36, (byte) 0x7b, (byte) 0xab, (byte) 0x59, (byte) 0x5b, (byte) 0xaf, (byte) 0xdb, (byte) 0xb1, (byte) 0x90, (byte) 0xf5, (byte) 0x81, (byte) 0xd1, (byte) 0xb5, (byte) 0xda, (byte) 0x3d, (byte) 0x9e, (byte) 0xfa, (byte) 0x1d, (byte) 0x02, (byte) 0x09, (byte) 0xb9, (byte) 0xa8, (byte) 0xd0, (byte) 0x5c, (byte) 0x1a, (byte) 0xf7, (byte) 0x63, (byte) 0x73, (byte) 0xd5, (byte) 0xfd, (byte) 0xaf, (byte) 0x1e, (byte) 0x38, (byte) 0x5a, (byte) 0xb4, (byte) 0xfc, (byte) 0xac, (byte) 0x78, (byte) 0x75, (byte) 0x06, (byte) 0x8d, (byte) 0xef, (byte) 0x92, (byte) 0x29, (byte) 0xb6};

        Notifications.NotificationActionRequest request = new Notifications.NotificationActionRequest(
                secretsProvider,
                notificationId,
                notificationType,
                vibrate,
                titleEncoding,
                titleContent,
                senderEncoding,
                senderContent,
                bodyEncoding,
                bodyContent,
                sourceAppId
        );

        Assert.assertEquals(0x02, request.serviceId);
        Assert.assertEquals(0x01, request.commandId);
        Assert.assertTrue(request.complete);
        Assert.assertEquals(expectedTlv, tlvField.get(request));
        Assert.assertArrayEquals(expectedOutput, request.serialize());
    }

    @Test
    public void testSetNotificationRequest() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.CryptoException {
        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlvTrue = new HuaweiTLV()
                .put(0x81, new HuaweiTLV()
                        .put(0x02, true)
                        .put(0x03, true)
                );

        HuaweiTLV expectedTlvFalse = new HuaweiTLV()
                .put(0x81, new HuaweiTLV()
                        .put(0x02, false)
                        .put(0x03, false)
                );

        byte[] expectedOutputTrue = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x02, (byte) 0x04, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0xd9, (byte) 0xc4, (byte) 0xaa, (byte) 0x7d, (byte) 0xa3, (byte) 0x5c, (byte) 0x42, (byte) 0xab, (byte) 0x2d, (byte) 0xc2, (byte) 0xe7, (byte) 0x73, (byte) 0xc0, (byte) 0x4c, (byte) 0x97, (byte) 0x5a, (byte) 0x41, (byte) 0x23};
        byte[] expectedOutputFalse = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x02, (byte) 0x04, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0xeb, (byte) 0x1f, (byte) 0x20, (byte) 0x0a, (byte) 0x7d, (byte) 0xe2, (byte) 0x25, (byte) 0x45, (byte) 0x01, (byte) 0x5b, (byte) 0xe8, (byte) 0x24, (byte) 0xe3, (byte) 0x7e, (byte) 0x1d, (byte) 0x9c, (byte) 0x47, (byte) 0x31};

        Notifications.SetNotificationRequest requestTrue = new Notifications.SetNotificationRequest(secretsProvider, true);
        Notifications.SetNotificationRequest requestFalse = new Notifications.SetNotificationRequest(secretsProvider, false);

        Assert.assertEquals(0x02, requestTrue.serviceId);
        Assert.assertEquals(0x04, requestTrue.commandId);
        Assert.assertTrue(requestTrue.complete);
        Assert.assertEquals(expectedTlvTrue, tlvField.get(requestTrue));
        Assert.assertArrayEquals(expectedOutputTrue, requestTrue.serialize());

        Assert.assertEquals(0x02, requestFalse.serviceId);
        Assert.assertEquals(0x04, requestFalse.commandId);
        Assert.assertTrue(requestFalse.complete);
        Assert.assertEquals(expectedTlvFalse, tlvField.get(requestFalse));
        Assert.assertArrayEquals(expectedOutputFalse, requestFalse.serialize());
    }

    @Test
    public void testSetWearMessagePushRequest() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.CryptoException {
        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlvTrue = new HuaweiTLV()
                .put(0x01, true);

        HuaweiTLV expectedTlvFalse = new HuaweiTLV()
                .put(0x01, false);

        byte[] expectedOutputTrue = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x02, (byte) 0x08, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0xcd, (byte) 0x97, (byte) 0x7e, (byte) 0x01, (byte) 0x48, (byte) 0x34, (byte) 0x2a, (byte) 0x48, (byte) 0x58, (byte) 0x0d, (byte) 0x30, (byte) 0xc7, (byte) 0xbc, (byte) 0x2e, (byte) 0x40, (byte) 0xd4, (byte) 0x29, (byte) 0xe0};
        byte[] expectedOutputFalse = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x02, (byte) 0x08, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0x28, (byte) 0x00, (byte) 0x99, (byte) 0x6f, (byte) 0x2a, (byte) 0xcb, (byte) 0x62, (byte) 0x3a, (byte) 0xe6, (byte) 0x54, (byte) 0x28, (byte) 0x54, (byte) 0xf8, (byte) 0xab, (byte) 0x54, (byte) 0x83, (byte) 0x30, (byte) 0xd2};

        Notifications.SetWearMessagePushRequest requestTrue = new Notifications.SetWearMessagePushRequest(secretsProvider, true);
        Notifications.SetWearMessagePushRequest requestFalse = new Notifications.SetWearMessagePushRequest(secretsProvider, false);

        Assert.assertEquals(0x02, requestTrue.serviceId);
        Assert.assertEquals(0x08, requestTrue.commandId);
        Assert.assertTrue(requestTrue.complete);
        Assert.assertEquals(expectedTlvTrue, tlvField.get(requestTrue));
        Assert.assertArrayEquals(expectedOutputTrue, requestTrue.serialize());

        Assert.assertEquals(0x02, requestFalse.serviceId);
        Assert.assertEquals(0x08, requestFalse.commandId);
        Assert.assertTrue(requestFalse.complete);
        Assert.assertEquals(expectedTlvFalse, tlvField.get(requestFalse));
        Assert.assertArrayEquals(expectedOutputFalse, requestFalse.serialize());
    }
}
