package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TestHuaweiCrypto {

    private void printByteArrayAsHex(String name, byte[] array) {
        System.out.print(name + ": [");
        for (int i = 0; i < array.length - 1; i++) {
            System.out.print(Integer.toHexString(array[i] & 0xFF) + ", ");
        }
        System.out.println(Integer.toHexString(array[array.length - 1] & 0xFF) + "]");
    }

    private void printIntArray(String name, int[] array) {
        System.out.print(name + ": [");
        for (int i = 0; i < array.length - 1; i++) {
            System.out.print(array[i] + ", ");
        }
        System.out.println(array[array.length - 1] + "]");
    }

    @Test
    public void testGenerateNonce() {
        // The function output contains randomness, so we test multiple times

        // We also check how often each byte is present, and that the difference isn't too high
        // Note that this may fail due to the randomness
        int[] bytePresent = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Assert.assertEquals(256, bytePresent.length);

        for (int i = 0; i < 1000; i++) {
            byte[] output = HuaweiCrypto.generateNonce();

            printByteArrayAsHex("Output", output);

            Assert.assertEquals(16, output.length);

            for (byte b : output) {
                bytePresent[b & 0xFF] += 1;
            }
        }

        printIntArray("Bytes present", bytePresent);

        int minCount = Integer.MAX_VALUE;
        int maxCount = Integer.MIN_VALUE;
        for (int c : bytePresent) {
            minCount = Math.min(c, minCount);
            maxCount = Math.max(c, maxCount);
        }

        // The limit here is quite arbitrary, erring on the high side
        if (maxCount - minCount > 60) {
            Assert.fail("Difference in byte counts is suspiciously high, check the randomness of the nonce.");
        }
    }
}
