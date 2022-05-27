package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class TestCalls {

    @Test
    public void testAnswerCallResponseAccept() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] raw = new byte[] {(byte) 0x5A, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x04, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x99, (byte) 0x6B};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV().put(0x01, (byte) 0x02);

        HuaweiPacket packet = new HuaweiPacket(null).parse(raw);

        Assert.assertEquals(0x04, packet.serviceId);
        Assert.assertEquals(0x01, packet.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(packet));
        Assert.assertTrue(packet.complete);
        Assert.assertTrue(packet instanceof Calls.AnswerCallResponse);
        Assert.assertEquals(Calls.AnswerCallResponse.Action.CALL_ACCEPT, ((Calls.AnswerCallResponse) packet).action);
    }

    @Test
    public void testAnswerCallResponseReject() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] raw = new byte[] {(byte) 0x5A, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x04, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xA9, (byte) 0x08};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV().put(0x01, (byte) 0x01);

        HuaweiPacket packet = new HuaweiPacket(null).parse(raw);

        Assert.assertEquals(0x04, packet.serviceId);
        Assert.assertEquals(0x01, packet.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(packet));
        Assert.assertTrue(packet.complete);
        Assert.assertTrue(packet instanceof Calls.AnswerCallResponse);
        Assert.assertEquals(Calls.AnswerCallResponse.Action.CALL_REJECT, ((Calls.AnswerCallResponse) packet).action);
    }
}
