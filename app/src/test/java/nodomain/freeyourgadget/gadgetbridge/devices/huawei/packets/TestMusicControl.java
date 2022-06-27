package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.MusicControl.Control.Response.Button;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class TestMusicControl {

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
    public void testMusicStatusRequest() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.CryptoException {
        int okInput = 0x000186a0;
        int errInput = 0x00000000;

        byte commandId1 = 0x01;
        byte commandId2 = 0x02;

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV okExpectedTlv = new HuaweiTLV()
                .put(0x7f, okInput);
        HuaweiTLV errExpectedTlv = new HuaweiTLV()
                .put(0x7f, errInput);

        byte[] okExpectedSerialized = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x25, (byte) 0x01, (byte) 0x7f, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x86, (byte) 0xa0, (byte) 0x63, (byte) 0x96};
        byte[] errExpectedSerialized = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x25, (byte) 0x02, (byte) 0x7f, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x88, (byte) 0xf0};

        MusicControl.MusicStatusRequest okRequest = new MusicControl.MusicStatusRequest(secretsProvider, commandId1, okInput);
        MusicControl.MusicStatusRequest errRequest = new MusicControl.MusicStatusRequest(secretsProvider, commandId2, errInput);

        Assert.assertEquals(0x25, okRequest.serviceId);
        Assert.assertEquals(commandId1, okRequest.commandId);
        Assert.assertEquals(okExpectedTlv, tlvField.get(okRequest));
        Assert.assertTrue(okRequest.complete);
        Assert.assertArrayEquals(okExpectedSerialized, okRequest.serialize());

        Assert.assertEquals(0x25, errRequest.serviceId);
        Assert.assertEquals(commandId2, errRequest.commandId);
        Assert.assertEquals(errExpectedTlv, tlvField.get(errRequest));
        Assert.assertTrue(errRequest.complete);
        Assert.assertArrayEquals(errExpectedSerialized, errRequest.serialize());
    }

    @Test
    public void testMusicStatusResponse() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] raw = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x2a, (byte) 0x00, (byte) 0x25, (byte) 0x01, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x10, (byte) 0x01, (byte) 0x43, (byte) 0xdb, (byte) 0x63, (byte) 0xee, (byte) 0x66, (byte) 0xb0, (byte) 0xcd, (byte) 0xff, (byte) 0x9f, (byte) 0x69, (byte) 0x91, (byte) 0x76, (byte) 0x80, (byte) 0x15, (byte) 0x1e, (byte) 0x52, (byte) 0x46};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV expectedTlv = new HuaweiTLV();

        HuaweiPacket packet = new HuaweiPacket(secretsProvider).parse(raw);
        packet.parseTlv();

        Assert.assertEquals(0x25, packet.serviceId);
        Assert.assertEquals(0x01, packet.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(packet));
        Assert.assertTrue(packet instanceof MusicControl.MusicStatusResponse);

        // TODO: complete test when more is known about packet contents
    }

    @Test
    public void testMusicInfoRequest() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.CryptoException {
        String artistName = "Artist";
        String songName = "Song";
        byte playState = 0x01;
        byte maxVolume = 0x03;
        byte currentVolume = 0x02;
        HuaweiTLV expectedTlv = new HuaweiTLV()
                .put(0x01, artistName)
                .put(0x02, songName)
                .put(0x03, playState)
                .put(0x04, maxVolume)
                .put(0x05, currentVolume);
        byte[] expectedSerializedPacket = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x3a, (byte) 0x00, (byte) 0x25, (byte) 0x02, (byte) 0x7c, (byte) 0x01, (byte) 0x01, (byte) 0x7d, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7e, (byte) 0x20, (byte) 0x21, (byte) 0x8b, (byte) 0xe1, (byte) 0x3d, (byte) 0x9f, (byte) 0x85, (byte) 0xd2, (byte) 0x2e, (byte) 0x64, (byte) 0x87, (byte) 0x3f, (byte) 0x1d, (byte) 0xab, (byte) 0x3f, (byte) 0xc7, (byte) 0x39, (byte) 0xb6, (byte) 0x34, (byte) 0x89, (byte) 0x60, (byte) 0xa0, (byte) 0x36, (byte) 0x4a, (byte) 0x08, (byte) 0x7a, (byte) 0x16, (byte) 0xed, (byte) 0xc9, (byte) 0x9e, (byte) 0xf3, (byte) 0xbf, (byte) 0x44, (byte) 0xac, (byte) 0x58};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        MusicControl.MusicInfo.Request musicInfoRequest = new MusicControl.MusicInfo.Request(
                secretsProvider,
                artistName,
                songName,
                playState,
                maxVolume,
                currentVolume
        );

        Assert.assertEquals(0x25, musicInfoRequest.serviceId);
        Assert.assertEquals(0x02, musicInfoRequest.commandId);
        Assert.assertEquals(expectedTlv, tlvField.get(musicInfoRequest));
        Assert.assertTrue(musicInfoRequest.complete);
        Assert.assertArrayEquals(expectedSerializedPacket, musicInfoRequest.serialize());
    }

    @Test
    public void testMusicInfoResponse() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] rawOk = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x25, (byte) 0x02, (byte) 0x7f, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x86, (byte) 0xA0, (byte) 0xbb, (byte) 0x14};
        byte[] rawErr = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x25, (byte) 0x02, (byte) 0x7f, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x88, (byte) 0xf0};
        byte[] rawMissing = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x25, (byte) 0x02, (byte) 0xb4, (byte) 0x1b};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV okExpectedTlv = new HuaweiTLV()
                .put(0x7f, 0x000186a0);
        HuaweiTLV errExpectedTlv = new HuaweiTLV()
                .put(0x7f, 0x00000000);
        HuaweiTLV missingExpectedTlv = new HuaweiTLV();

        HuaweiPacket packetOk = new HuaweiPacket(secretsProvider).parse(rawOk);
        HuaweiPacket packetErr = new HuaweiPacket(secretsProvider).parse(rawErr);
        HuaweiPacket packetMissing = new HuaweiPacket(secretsProvider).parse(rawMissing);

        packetOk.parseTlv();
        packetErr.parseTlv();
        packetMissing.parseTlv();

        Assert.assertEquals(0x25, packetOk.serviceId);
        Assert.assertEquals(0x02, packetOk.commandId);
        Assert.assertEquals(okExpectedTlv, tlvField.get(packetOk));
        Assert.assertTrue(packetOk instanceof MusicControl.MusicInfo.Response);
        Assert.assertTrue(((MusicControl.MusicInfo.Response) packetOk).ok);
        Assert.assertEquals("", ((MusicControl.MusicInfo.Response) packetOk).error);

        Assert.assertEquals(0x25, packetErr.serviceId);
        Assert.assertEquals(0x02, packetErr.commandId);
        Assert.assertEquals(errExpectedTlv, tlvField.get(packetErr));
        Assert.assertTrue(packetErr instanceof MusicControl.MusicInfo.Response);
        Assert.assertFalse(((MusicControl.MusicInfo.Response) packetErr).ok);
        Assert.assertEquals("Music information error code: 0", ((MusicControl.MusicInfo.Response) packetErr).error);

        Assert.assertEquals(0x25, packetMissing.serviceId);
        Assert.assertEquals(0x02, packetMissing.commandId);
        Assert.assertEquals(missingExpectedTlv, tlvField.get(packetMissing));
        Assert.assertTrue(packetMissing instanceof MusicControl.MusicInfo.Response);
        Assert.assertFalse(((MusicControl.MusicInfo.Response) packetMissing).ok);
        Assert.assertEquals("Music information response no status tag", ((MusicControl.MusicInfo.Response) packetMissing).error);
    }

    @Test
    public void testControlResponse() throws NoSuchFieldException, IllegalAccessException, HuaweiPacket.ParseException {
        byte[] emptyInput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x25, (byte) 0x03, (byte) 0xa4, (byte) 0x3a, };
        byte[] playPauseInput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x25, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0xe6, (byte) 0x85};
        byte[] previousInput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x25, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x03, (byte) 0xc6, (byte) 0xc7};
        byte[] nextInput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x25, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x04, (byte) 0xb6, (byte) 0x20};
        byte[] unknownButtonInput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x25, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0xFF, (byte) 0xe8, (byte) 0x54};
        byte[] volumeInput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x25, (byte) 0x03, (byte) 0x02, (byte) 0x01, (byte) 0x42, (byte) 0xc7, (byte) 0x72};
        byte[] combinedInput = new byte[] {(byte) 0x5a, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0x25, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x42, (byte) 0x95, (byte) 0x9a};

        Field tlvField = HuaweiPacket.class.getDeclaredField("tlv");
        tlvField.setAccessible(true);

        HuaweiTLV emptyExpectedTlv = new HuaweiTLV();
        HuaweiTLV playPauseExpectedTlv = new HuaweiTLV()
                .put(0x01, (byte) 0x01);
        HuaweiTLV previousExpectedTlv = new HuaweiTLV()
                .put(0x01,  (byte) 0x03);
        HuaweiTLV nextExpectedTlv = new HuaweiTLV()
                .put(0x01, (byte) 0x04);
        HuaweiTLV unknownButtonExpectedTlv = new HuaweiTLV()
                .put(0x01, (byte) 0xFF);
        HuaweiTLV volumeExpectedTlv = new HuaweiTLV()
                .put(0x02, (byte) 0x42);
        HuaweiTLV combinedExpectedTlv = new HuaweiTLV()
                .put(0x01, (byte) 0x01)
                .put(0x02, (byte) 0x42);

        HuaweiPacket emptyResponse = new HuaweiPacket(secretsProvider).parse(emptyInput);
        HuaweiPacket playPauseResponse = new HuaweiPacket(secretsProvider).parse(playPauseInput);
        HuaweiPacket previousResponse = new HuaweiPacket(secretsProvider).parse(previousInput);
        HuaweiPacket nextResponse = new HuaweiPacket(secretsProvider).parse(nextInput);
        HuaweiPacket unknownButtonResponse = new HuaweiPacket(secretsProvider).parse(unknownButtonInput);
        HuaweiPacket volumeResponse = new HuaweiPacket(secretsProvider).parse(volumeInput);
        HuaweiPacket combinedResponse = new HuaweiPacket(secretsProvider).parse(combinedInput);

        emptyResponse.parseTlv();
        playPauseResponse.parseTlv();
        previousResponse.parseTlv();
        nextResponse.parseTlv();
        unknownButtonResponse.parseTlv();
        volumeResponse.parseTlv();
        combinedResponse.parseTlv();

        Assert.assertEquals(0x25, emptyResponse.serviceId);
        Assert.assertEquals(0x03, emptyResponse.commandId);
        Assert.assertEquals(emptyExpectedTlv, tlvField.get(emptyResponse));
        Assert.assertTrue(emptyResponse instanceof MusicControl.Control.Response);
        Assert.assertFalse(((MusicControl.Control.Response) emptyResponse).buttonPresent);
        Assert.assertFalse(((MusicControl.Control.Response) emptyResponse).volumePresent);

        Assert.assertEquals(0x25, playPauseResponse.serviceId);
        Assert.assertEquals(0x03, playPauseResponse.commandId);
        Assert.assertEquals(playPauseExpectedTlv, tlvField.get(playPauseResponse));
        Assert.assertTrue(playPauseResponse instanceof MusicControl.Control.Response);
        Assert.assertTrue(((MusicControl.Control.Response) playPauseResponse).buttonPresent);
        Assert.assertEquals(0x01, ((MusicControl.Control.Response) playPauseResponse).rawButton);
        Assert.assertEquals(Button.PlayPause, ((MusicControl.Control.Response) playPauseResponse).button);
        Assert.assertFalse(((MusicControl.Control.Response) playPauseResponse).volumePresent);

        Assert.assertEquals(0x25, previousResponse.serviceId);
        Assert.assertEquals(0x03, previousResponse.commandId);
        Assert.assertEquals(previousExpectedTlv, tlvField.get(previousResponse));
        Assert.assertTrue(previousResponse instanceof MusicControl.Control.Response);
        Assert.assertTrue(((MusicControl.Control.Response) previousResponse).buttonPresent);
        Assert.assertEquals(0x03, ((MusicControl.Control.Response) previousResponse).rawButton);
        Assert.assertEquals(Button.Previous, ((MusicControl.Control.Response) previousResponse).button);
        Assert.assertFalse(((MusicControl.Control.Response) previousResponse).volumePresent);

        Assert.assertEquals(0x25, nextResponse.serviceId);
        Assert.assertEquals(0x03, nextResponse.commandId);
        Assert.assertEquals(nextExpectedTlv, tlvField.get(nextResponse));
        Assert.assertTrue(nextResponse instanceof MusicControl.Control.Response);
        Assert.assertTrue(((MusicControl.Control.Response) nextResponse).buttonPresent);
        Assert.assertEquals(0x04, ((MusicControl.Control.Response) nextResponse).rawButton);
        Assert.assertEquals(Button.Next, ((MusicControl.Control.Response) nextResponse).button);
        Assert.assertFalse(((MusicControl.Control.Response) nextResponse).volumePresent);

        Assert.assertEquals(0x25, unknownButtonResponse.serviceId);
        Assert.assertEquals(0x03, unknownButtonResponse.commandId);
        Assert.assertEquals(unknownButtonExpectedTlv, tlvField.get(unknownButtonResponse));
        Assert.assertTrue(unknownButtonResponse instanceof MusicControl.Control.Response);
        Assert.assertTrue(((MusicControl.Control.Response) unknownButtonResponse).buttonPresent);
        Assert.assertEquals((byte) 0xFF, ((MusicControl.Control.Response) unknownButtonResponse).rawButton);
        Assert.assertEquals(Button.Unknown, ((MusicControl.Control.Response) unknownButtonResponse).button);
        Assert.assertFalse(((MusicControl.Control.Response) unknownButtonResponse).volumePresent);

        Assert.assertEquals(0x25, volumeResponse.serviceId);
        Assert.assertEquals(0x03, volumeResponse.commandId);
        Assert.assertEquals(volumeExpectedTlv, tlvField.get(volumeResponse));
        Assert.assertTrue(volumeResponse instanceof MusicControl.Control.Response);
        Assert.assertFalse(((MusicControl.Control.Response) volumeResponse).buttonPresent);
        Assert.assertTrue(((MusicControl.Control.Response) volumeResponse).volumePresent);
        Assert.assertEquals(0x42, ((MusicControl.Control.Response) volumeResponse).volume);

        Assert.assertEquals(0x25, combinedResponse.serviceId);
        Assert.assertEquals(0x03, combinedResponse.commandId);
        Assert.assertEquals(combinedExpectedTlv, tlvField.get(combinedResponse));
        Assert.assertTrue(combinedResponse instanceof MusicControl.Control.Response);
        Assert.assertTrue(((MusicControl.Control.Response) combinedResponse).buttonPresent);
        Assert.assertEquals(0x01, ((MusicControl.Control.Response) combinedResponse).rawButton);
        Assert.assertEquals(Button.PlayPause, ((MusicControl.Control.Response) combinedResponse).button);
        Assert.assertTrue(((MusicControl.Control.Response) combinedResponse).volumePresent);
        Assert.assertEquals(0x42, ((MusicControl.Control.Response) combinedResponse).volume);
    }
}
