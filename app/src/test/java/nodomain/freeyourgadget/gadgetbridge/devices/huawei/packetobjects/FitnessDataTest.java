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
                .put(0x84, new HuaweiTLV()
                        .put(0x05, (byte) 0x02)
                        .put(0x06, new byte[] {0x0e, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03})
                )
                .put(0x84, new HuaweiTLV()
                        .put(0x05, (byte) 0x02)
                        .put(0x06, new byte[] {0x01, 0x00, 0x01})
                )
        );

        FitnessData.MessageData.StepResponse stepResponse = FitnessData.MessageData.StepResponse.fromTlv(input);

        Assert.assertEquals(0x1337, stepResponse.number);
        Assert.assertEquals(0xCAFEBEEF, stepResponse.timestamp);
        Assert.assertEquals(4, stepResponse.containers.size());

        Assert.assertEquals(0x00, stepResponse.containers.get(0).timestampOffset);
        Assert.assertArrayEquals(new byte[] {}, stepResponse.containers.get(0).data);
        Assert.assertEquals(0xCAFEBEEF, stepResponse.containers.get(0).timestamp);
        Assert.assertNull(stepResponse.containers.get(0).parsedData);
        Assert.assertEquals("Data is missing feature bitmap.", stepResponse.containers.get(0).parsedDataError);
        Assert.assertEquals(-1, stepResponse.containers.get(0).steps);
        Assert.assertEquals(-1, stepResponse.containers.get(0).calories);
        Assert.assertEquals(-1, stepResponse.containers.get(0).distance);
        Assert.assertNull(stepResponse.containers.get(0).unknownTVs);

        Assert.assertEquals(0x01, stepResponse.containers.get(1).timestampOffset);
        Assert.assertArrayEquals(new byte[] {0x01, 0x02}, stepResponse.containers.get(1).data);
        Assert.assertEquals(0xCAFEBF2B, stepResponse.containers.get(1).timestamp);
        Assert.assertNull(stepResponse.containers.get(1).parsedData);
        Assert.assertEquals("Data is too short for selected features.", stepResponse.containers.get(1).parsedDataError);
        Assert.assertEquals(-1, stepResponse.containers.get(1).steps);
        Assert.assertEquals(-1, stepResponse.containers.get(1).calories);
        Assert.assertEquals(-1, stepResponse.containers.get(1).distance);
        Assert.assertEquals(0, stepResponse.containers.get(1).unknownTVs.size());

        Assert.assertEquals(0x02, stepResponse.containers.get(2).timestampOffset);
        Assert.assertArrayEquals(new byte[] {0x0e, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03}, stepResponse.containers.get(2).data);
        Assert.assertEquals(0xCAFEBF67, stepResponse.containers.get(2).timestamp);
        Assert.assertEquals(3, stepResponse.containers.get(2).parsedData.size());
        Assert.assertEquals(0x02, stepResponse.containers.get(2).parsedData.get(0).tag);
        Assert.assertEquals(0x01, stepResponse.containers.get(2).parsedData.get(0).value);
        Assert.assertEquals(0x04, stepResponse.containers.get(2).parsedData.get(1).tag);
        Assert.assertEquals(0x02, stepResponse.containers.get(2).parsedData.get(1).value);
        Assert.assertEquals(0x08, stepResponse.containers.get(2).parsedData.get(2).tag);
        Assert.assertEquals(0x03, stepResponse.containers.get(2).parsedData.get(2).value);
        Assert.assertEquals("", stepResponse.containers.get(2).parsedDataError);
        Assert.assertEquals(0x01, stepResponse.containers.get(2).steps);
        Assert.assertEquals(0x02, stepResponse.containers.get(2).calories);
        Assert.assertEquals(0x03, stepResponse.containers.get(2).distance);
        Assert.assertEquals(0, stepResponse.containers.get(2).unknownTVs.size());

        Assert.assertEquals(0x02, stepResponse.containers.get(3).timestampOffset);
        Assert.assertArrayEquals(new byte[] {0x01, 0x00, 0x01}, stepResponse.containers.get(3).data);
        Assert.assertEquals(0xCAFEBF67, stepResponse.containers.get(3).timestamp);
        Assert.assertEquals(1, stepResponse.containers.get(3).parsedData.size());
        Assert.assertEquals(0x01, stepResponse.containers.get(3).parsedData.get(0).tag);
        Assert.assertEquals(0x01, stepResponse.containers.get(3).parsedData.get(0).value);
        Assert.assertEquals("", stepResponse.containers.get(3).parsedDataError);
        Assert.assertEquals(-1, stepResponse.containers.get(3).steps);
        Assert.assertEquals(-1, stepResponse.containers.get(3).calories);
        Assert.assertEquals(-1, stepResponse.containers.get(3).distance);
        Assert.assertEquals(1, stepResponse.containers.get(3).unknownTVs.size());
        Assert.assertEquals(0x01, stepResponse.containers.get(3).unknownTVs.get(0).tag);
        Assert.assertEquals(0x01, stepResponse.containers.get(3).unknownTVs.get(0).value);
    }
}
