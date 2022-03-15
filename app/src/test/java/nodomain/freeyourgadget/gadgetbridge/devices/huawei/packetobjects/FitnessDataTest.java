package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packetobjects;

import org.junit.Assert;
import org.junit.Test;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class FitnessDataTest {

    @Test
    public void testMessageCountRequest() {
        int start = 0;
        int end = 100;
        HuaweiTLV expectedOutput = new HuaweiTLV()
                .put(0x81)
                .put(0x03, start)
                .put(0x04, end);

        Assert.assertEquals(expectedOutput, FitnessData.MessageCount.Request.toTlv(start, end));
    }

    @Test
    public void testMessageCountResponse() {
        short count = 0x1337;
        HuaweiTLV input = new HuaweiTLV().put(0x81, new HuaweiTLV().put(0x02, count));

        Assert.assertEquals(count, FitnessData.MessageCount.Response.fromTlv(input).count);
    }

    @Test
    public void testMessageDataRequest() {
        short count = 0x1337;
        HuaweiTLV expectedOutput = new HuaweiTLV().put(0x81, new HuaweiTLV().put(0x02, count));

        Assert.assertEquals(expectedOutput, FitnessData.MessageData.Request.toTlv(count));
    }

    @Test
    public void testMessageDataSleepResponse() {
        HuaweiTLV input = new HuaweiTLV().put(0x81, new HuaweiTLV()
                .put(0x02, (short) 0x1337)
                .put(0x83, new HuaweiTLV()
                        .put(0x04, (byte) 0x00)
                        .put(0x05, new byte[] {})
                )
                .put(0x83, new HuaweiTLV()
                        .put(0x04, (byte) 0x01)
                        .put(0x05, new byte[] {0x01, 0x02})
                )
        );

        FitnessData.MessageData.SleepResponse sleepResponse = FitnessData.MessageData.SleepResponse.fromTlv(input);

        Assert.assertEquals(0x1337, sleepResponse.number);
        Assert.assertEquals(2, sleepResponse.containers.size());
        Assert.assertEquals(0x00, sleepResponse.containers.get(0).type);
        Assert.assertArrayEquals(new byte[] {}, sleepResponse.containers.get(0).timestamp);
        Assert.assertEquals(0x01, sleepResponse.containers.get(1).type);
        Assert.assertArrayEquals(new byte[] {0x01, 0x02}, sleepResponse.containers.get(1).timestamp);
    }

    @Test
    public void testMessageDataStepResponse() {
        HuaweiTLV input = new HuaweiTLV().put(0x81, new HuaweiTLV()
                .put(0x02, (short) 0x1337)
                .put(0x03, 0xCAFEBEEF)
                .put(0x084, new HuaweiTLV()
                        .put(0x05, (byte) 0x00)
                        .put(0x06, new byte[] {})
                )
                .put(0x84, new HuaweiTLV()
                        .put(0x05, (byte) 0x01)
                        .put(0x06, new byte[] {0x01, 0x02})
                )
        );

        FitnessData.MessageData.StepResponse stepResponse = FitnessData.MessageData.StepResponse.fromTlv(input);

        Assert.assertEquals(0x1337, stepResponse.number);
        Assert.assertEquals(0xCAFEBEEF, stepResponse.timestamp);
        Assert.assertEquals(2, stepResponse.containers.size());
        Assert.assertEquals(0x00, stepResponse.containers.get(0).timestampOffset);
        Assert.assertArrayEquals(new byte[] {}, stepResponse.containers.get(0).data);
        Assert.assertEquals(0x01, stepResponse.containers.get(1).timestampOffset);
        Assert.assertArrayEquals(new byte[] {0x01, 0x02}, stepResponse.containers.get(1).data);
    }
}
