package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class TestLocaleConfig {

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
    public void testSetLocaleRequest() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.CryptoException {
        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV()
                .put(0x01, new byte[] {0x45, 0x4e, 0x2d, 0x47, 0x42})
                .put(0x02, (byte) 0x00);

        byte[] serialized = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x0c, (byte) 0x01, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0x4e, (byte) 0xb0, (byte) 0x71, (byte) 0x05, (byte) 0x7b, (byte) 0xf1, (byte) 0x07, (byte) 0x31, (byte) 0xc4, (byte) 0x6c, (byte) 0x5b, (byte) 0x6d, (byte) 0xbf, (byte) 0x07, (byte) 0xf5, (byte) 0x55, (byte) 0x65, (byte) 0x06};

        LocaleConfig.SetLocaleRequest request = new LocaleConfig.SetLocaleRequest(
                secretsProvider,
                new byte[] {0x45, 0x4e, 0x2d, 0x47, 0x42},
                (byte) 0x00
        );

        Assert.assertEquals(0x0c, request.serviceId);
        Assert.assertEquals(0x01, request.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(request));
        Assert.assertTrue(request.complete);
        Assert.assertArrayEquals(serialized, request.serialize());
    }
}
