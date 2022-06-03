package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class TestWorkMode {

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
    public void testSwitchStatusRequest() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.CryptoException {
        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlvTrue = new HuaweiTLV()
                .put(0x01, true);
        HuaweiTLV expectedTlvFalse = new HuaweiTLV()
                .put(0x01, false);

        byte[] serializedTrue = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x26, (byte) 0x02, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0xcd, (byte) 0x97, (byte) 0x7e, (byte) 0x01, (byte) 0x48, (byte) 0x34, (byte) 0x2a, (byte) 0x48, (byte) 0x58, (byte) 0x0d, (byte) 0x30, (byte) 0xc7, (byte) 0xbc, (byte) 0x2e, (byte) 0x40, (byte) 0xd4, (byte) 0x5c, (byte) 0x5a};
        byte[] serializedFalse = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x26, (byte) 0x02, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0x28, (byte) 0x00, (byte) 0x99, (byte) 0x6f, (byte) 0x2a, (byte) 0xcb, (byte) 0x62, (byte) 0x3a, (byte) 0xe6, (byte) 0x54, (byte) 0x28, (byte) 0x54, (byte) 0xf8, (byte) 0xab, (byte) 0x54, (byte) 0x83, (byte) 0x45, (byte) 0x68};

        WorkMode.SwitchStatusRequest requestTrue = new WorkMode.SwitchStatusRequest(secretsProvider, true);
        WorkMode.SwitchStatusRequest requestFalse = new WorkMode.SwitchStatusRequest(secretsProvider, false);

        Assert.assertEquals(0x26, requestTrue.serviceId);
        Assert.assertEquals(0x02, requestTrue.commandId);
        Assert.assertEquals(expectedTlvTrue, tlvField.get(requestTrue));
        Assert.assertTrue(requestTrue.complete);
        Assert.assertArrayEquals(serializedTrue, requestTrue.serialize());

        Assert.assertEquals(0x26, requestFalse.serviceId);
        Assert.assertEquals(0x02, requestFalse.commandId);
        Assert.assertEquals(expectedTlvFalse, tlvField.get(requestFalse));
        Assert.assertTrue(requestFalse.complete);
        Assert.assertArrayEquals(serializedFalse, requestFalse.serialize());
    }
}
