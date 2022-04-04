package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class TestFindPhoneResponse {

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
    public void testStartFindPhone() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] raw = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x0b, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xcc, (byte) 0xf1};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV().put(0x01, true);

        HuaweiPacket packet = new HuaweiPacket(secretsProvider).parse(raw);

        Assert.assertEquals(0x0b, packet.serviceId);
        Assert.assertEquals(0x01, packet.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(packet));
        Assert.assertTrue(packet.complete);
        Assert.assertTrue(packet instanceof FindPhoneResponse);
        Assert.assertTrue(((FindPhoneResponse) packet).start);
    }

    @Test
    public void testStopFindPhone() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] raw = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x0b, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0xdc, (byte) 0xd0};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV().put(0x01, false);

        HuaweiPacket packet = new HuaweiPacket(secretsProvider).parse(raw);

        Assert.assertEquals(0x0b, packet.serviceId);
        Assert.assertEquals(0x01, packet.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(packet));
        Assert.assertTrue(packet.complete);
        Assert.assertTrue(packet instanceof FindPhoneResponse);
        Assert.assertFalse(((FindPhoneResponse) packet).start);
    }

    @Test
    public void testFindPhoneMissingTag() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] raw = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x0b, (byte) 0x01, (byte) 0xa1, (byte) 0x91};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV();

        HuaweiPacket packet = new HuaweiPacket(secretsProvider).parse(raw);

        Assert.assertEquals(0x0b, packet.serviceId);
        Assert.assertEquals(0x01, packet.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(packet));
        Assert.assertTrue(packet.complete);
        Assert.assertTrue(packet instanceof FindPhoneResponse);
        Assert.assertFalse(((FindPhoneResponse) packet).start);
    }
}
