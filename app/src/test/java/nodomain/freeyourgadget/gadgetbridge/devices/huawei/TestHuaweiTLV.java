package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class TestHuaweiTLV {

    @Test
    public void testSerializeEmpty() {
        byte[] expectedOutput = {};

        HuaweiTLV huaweiTLV = new HuaweiTLV();

        Assert.assertArrayEquals(expectedOutput, huaweiTLV.serialize());
    }

    @Test
    public void testSerialize() {
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {}));
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x42}));
        byte[] expectedOutput = {0x01, 0x00, 0x01, 0x01, 0x42};

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertArrayEquals(expectedOutput, huaweiTLV.serialize());
    }

    @Test
    public void testPutEmptyTag() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {}));

        HuaweiTLV huaweiTLV = new HuaweiTLV().put(tag);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutNullByteArray() {
        int tag = 0x01;
        byte[] input = null;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();

        //noinspection ConstantConditions
        HuaweiTLV huaweiTLV = new HuaweiTLV().put(tag, input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutByteArray() {
        int tag = 0x01;
        byte[] input = {0x01, 0x02, 0x03};
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01, 0x02, 0x03}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag, input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutByte() {
        int tag = 0x01;
        byte input = 0x13;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x13}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag, input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutBooleans() {
        int tag1 = 0x01;
        int tag2 = 0x02;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01}));
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x02, new byte[] {0x00}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag1, true)
                .put(tag2, false);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutInt() {
        int tag = 0x01;
        int input = 0xDEADBEEF;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag, input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutShort() {
        int tag = 0x01;
        short input = (short) 0xCAFE;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xCA, (byte) 0xFE}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag, input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutString() {
        int tag = 0x01;
        String input = "Hello world!";
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV(
                (byte) 0x01,
                new byte[] {0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64, 0x21}
        ));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag, input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutHuaweiTLV() {
        int tag = 0x01;
        HuaweiTLV input = new HuaweiTLV().put(0x01, (short) 0x1337);
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01, 0x02, 0x13, 0x37}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag, input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testPutMultipleEqualEmptyTags() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {}));
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .put(tag)
                .put(tag);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testParseEmpty() {
        byte[] input = {};
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .parse(input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testParseSingleByte() {
        byte[] input = {0x01, 0x01, 0x01};
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .parse(input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testParseBytes() {
        byte[] input = {0x01, 0x04, (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .parse(input);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testParseZeroOffsetLength() {
        byte[] input = {};
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .parse(input, 0, 0);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testParseMalformed() {
        byte[] input = {(byte) 0x01, (byte) 0x01};
        new HuaweiTLV()
                .parse(input);
        Assert.fail();
    }

    @Test
    public void testParseOffsetLength() {
        byte[] input = {(byte) 0x90, (byte) 0x90, (byte) 0x90, 0x01, 0x00};
        int offset = 3;
        int length = 2;
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {}));

        HuaweiTLV huaweiTLV = new HuaweiTLV()
                .parse(input, offset, length);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testParseWrongOffsetLength() {
        byte[] input = {};
        new HuaweiTLV()
                .parse(input, 1, 1);
        Assert.fail();
    }

    @Test
    public void testGetBytesEmpty() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {}));
        byte[] expectedOutput = new byte[] {};

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertArrayEquals(expectedOutput, huaweiTLV.getBytes(tag));
    }

    @Test
    public void testGetBytes() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01, 0x02, 0x03}));
        byte[] expectedOutput = new byte[] {0x01, 0x02, 0x03};

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertArrayEquals(expectedOutput, huaweiTLV.getBytes(tag));
    }

    @Test
    public void testGetByte() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x04}));
        Byte expectedOutput = 0x04;

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertEquals(expectedOutput, huaweiTLV.getByte(tag));
    }

    @Test
    public void testGetBooleans() {
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01}));
        input.add(new HuaweiTLV.TLV((byte) 0x02, new byte[] {0x00}));

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertEquals(true, huaweiTLV.getBoolean(0x01));
        Assert.assertEquals(false, huaweiTLV.getBoolean(0x02));
    }

    @Test
    public void testGetInteger() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}));
        Integer expectedOutput = 0xDEADBEEF;

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertEquals(expectedOutput, huaweiTLV.getInteger(tag));
    }

    @Test
    public void testGetShort() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xCA, (byte) 0xFE}));
        Short expectedOutput = (short) 0xCAFE;

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertEquals(expectedOutput, huaweiTLV.getShort(tag));
    }

    @Test
    public void testGetString() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64, 0x21}));
        String expectedOutput = "Hello world!";

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertEquals(expectedOutput, huaweiTLV.getString(tag));
    }

    @Test
    public void testGetObject() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01, 0x00}));
        HuaweiTLV expectedOutput = new HuaweiTLV().put(0x01);

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        // assertEquals currently tests if the objects are the same, thus this would fail
        // Assert.assertEquals(expectedOutput, huaweiTLV.getObject(tag));

        HuaweiTLV result = huaweiTLV.getObject(tag);

        Assert.assertEquals(expectedOutput.valueMap, result.valueMap);
    }

    @Test
    public void testContains() {
        int existingTag = 0x01;
        int nonExistingTag = 0x02;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {}));

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertTrue(huaweiTLV.contains(existingTag));
        Assert.assertFalse(huaweiTLV.contains(nonExistingTag));
    }

    @Test
    public void testRemoveExisting() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x13, 0x37}));
        input.add(new HuaweiTLV.TLV((byte) 0x02, new byte[] {}));
        byte[] expectedOutput = {0x13, 0x37};
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x02, new byte[] {}));

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertArrayEquals(expectedOutput, huaweiTLV.remove(tag));
        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    @Test
    public void testRemoveNonExisting() {
        int tag = 0x02;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x13, 0x37}));

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertNull(huaweiTLV.remove(tag));
        Assert.assertEquals(input, huaweiTLV.valueMap);
    }

    @Test
    public void testRemoveDouble() {
        int tag = 0x01;
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xCA}));
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xFE}));
        byte[] expectedOutput1 = {(byte) 0xFE};
        byte[] expectedOutput2 = {(byte) 0xCA};
        ArrayList<HuaweiTLV.TLV> expectedValueMap1 = new ArrayList<>();
        expectedValueMap1.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xCA}));
        ArrayList<HuaweiTLV.TLV> expectedValueMap2 = new ArrayList<>();

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertArrayEquals(expectedOutput1, huaweiTLV.remove(tag));
        Assert.assertEquals(expectedValueMap1, huaweiTLV.valueMap);
        Assert.assertArrayEquals(expectedOutput2, huaweiTLV.remove(tag));
        Assert.assertEquals(expectedValueMap2, huaweiTLV.valueMap);
    }

    @Test
    public void testToStringEmpty() {
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        String expectedOutput = "Empty";

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertEquals(expectedOutput, huaweiTLV.toString());
    }

    @Test
    public void testToString() {
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {0x01, 0x02}));
        input.add(new HuaweiTLV.TLV((byte) 0x02, new byte[] {0x03, 0x04}));
        String expectedOutput = "{tag: 1 - Value: 0102} - {tag: 2 - Value: 0304}";

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        Assert.assertEquals(expectedOutput, huaweiTLV.toString());
    }

    /**
     * Following test also depends on the HuaweiCrypto class functioning correctly
     */
    @Test
    public void testEncrypt() {
        byte[] key = new byte[16];
        byte[] iv = new byte[16];
        Arrays.fill(key, (byte) 0);
        Arrays.fill(iv, (byte) 0);

        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xCA, (byte) 0xFE}));

        byte[] expectedCiphertext = {(byte) 0x0E, (byte) 0xA0, (byte) 0x01, (byte) 0xBB, (byte) 0x1E, (byte) 0xDA, (byte) 0xCB, (byte) 0x09, (byte) 0x83, (byte) 0x20, (byte) 0x40, (byte) 0x7D, (byte) 0x97, (byte) 0x1B, (byte) 0xF6, (byte) 0xD0};
        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x7C, new byte[] {0x01}));
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x7D, iv));
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x7E, expectedCiphertext));

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        huaweiTLV.encrypt(key, iv);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }

    /**
     * Following test also depends on the HuaweiCrypto class functioning correctly
     */
    @Test
    public void testDecrypt() {
        byte[] key = new byte[16];
        byte[] iv = new byte[16];
        Arrays.fill(key, (byte) 0);
        Arrays.fill(iv, (byte) 0);

        byte[] ciphertext = {(byte) 0x0E, (byte) 0xA0, (byte) 0x01, (byte) 0xBB, (byte) 0x1E, (byte) 0xDA, (byte) 0xCB, (byte) 0x09, (byte) 0x83, (byte) 0x20, (byte) 0x40, (byte) 0x7D, (byte) 0x97, (byte) 0x1B, (byte) 0xF6, (byte) 0xD0};
        ArrayList<HuaweiTLV.TLV> input = new ArrayList<>();
        input.add(new HuaweiTLV.TLV((byte) 0x7C, new byte[] {0x01}));
        input.add(new HuaweiTLV.TLV((byte) 0x7D, iv));
        input.add(new HuaweiTLV.TLV((byte) 0x7E, ciphertext));

        ArrayList<HuaweiTLV.TLV> expectedValueMap = new ArrayList<>();
        expectedValueMap.add(new HuaweiTLV.TLV((byte) 0x01, new byte[] {(byte) 0xCA, (byte) 0xFE}));

        HuaweiTLV huaweiTLV = new HuaweiTLV();
        huaweiTLV.valueMap = input;

        huaweiTLV.decrypt(key);

        Assert.assertEquals(expectedValueMap, huaweiTLV.valueMap);
    }
}
