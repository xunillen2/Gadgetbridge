package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packetobjects;

import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packetobjects.MusicControl.Control.Response.Button;

import org.junit.Assert;
import org.junit.Test;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class TestMusicControl {

    @Test
    public void testMusicInfoRequest() {
        String artistName = "Artist";
        String songName = "Song";
        byte playState = 0x01;
        byte maxVolume = 0x03;
        byte currentVolume = 0x02;
        HuaweiTLV expectedOutput = new HuaweiTLV()
                .put(0x01, artistName)
                .put(0x02, songName)
                .put(0x03, playState)
                .put(0x04, maxVolume)
                .put(0x05, currentVolume);

        Assert.assertEquals(
                expectedOutput,
                MusicControl.MusicInfo.Request.toTlv(
                    artistName,
                    songName,
                    playState,
                    maxVolume,
                    currentVolume
                )
        );
    }

    @Test
    public void testMusicInfoResponse() {
        HuaweiTLV okInput = new HuaweiTLV()
                .put(0x7F, 0x000186A0);
        HuaweiTLV errInput = new HuaweiTLV()
                .put(0x7F, 0x00000000);
        HuaweiTLV missingInput = new HuaweiTLV();

        MusicControl.MusicInfo.Response okResponse = MusicControl.MusicInfo.Response.fromTlv(okInput);
        MusicControl.MusicInfo.Response errResponse = MusicControl.MusicInfo.Response.fromTlv(errInput);
        MusicControl.MusicInfo.Response missingResponse = MusicControl.MusicInfo.Response.fromTlv(missingInput);

        Assert.assertTrue(okResponse.ok);
        Assert.assertEquals("", okResponse.error);
        Assert.assertFalse(errResponse.ok);
        Assert.assertEquals("Music information error code: 0", errResponse.error);
        Assert.assertFalse(missingResponse.ok);
        Assert.assertEquals("Music information response no status tag", missingResponse.error);
    }

    @Test
    public void testControlResponse() {
        HuaweiTLV playPauseInput = new HuaweiTLV()
                .put(0x01, (byte) 0x01);
        HuaweiTLV previousInput = new HuaweiTLV()
                .put(0x01,  (byte) 0x03);
        HuaweiTLV nextInput = new HuaweiTLV()
                .put(0x01, (byte) 0x04);
        HuaweiTLV unknownButtonInput = new HuaweiTLV()
                .put(0x01, (byte) 0xFF);
        HuaweiTLV volumeInput = new HuaweiTLV()
                .put(0x02, (byte) 0x42);
        HuaweiTLV combinedInput = new HuaweiTLV()
                .put(0x01, (byte) 0x01)
                .put(0x02, (byte) 0x42);

        MusicControl.Control.Response playPauseResponse = MusicControl.Control.Response.fromTlv(playPauseInput);
        MusicControl.Control.Response previousResponse = MusicControl.Control.Response.fromTlv(previousInput);
        MusicControl.Control.Response nextResponse = MusicControl.Control.Response.fromTlv(nextInput);
        MusicControl.Control.Response unknownButtonResponse = MusicControl.Control.Response.fromTlv(unknownButtonInput);
        MusicControl.Control.Response volumeResponse = MusicControl.Control.Response.fromTlv(volumeInput);
        MusicControl.Control.Response combinedResponse = MusicControl.Control.Response.fromTlv(combinedInput);

        Assert.assertTrue(playPauseResponse.buttonPresent);
        Assert.assertEquals(0x01, playPauseResponse.rawButton);
        Assert.assertEquals(Button.PlayPause, playPauseResponse.button);
        Assert.assertFalse(playPauseResponse.volumePresent);

        Assert.assertTrue(previousResponse.buttonPresent);
        Assert.assertEquals(0x03, previousResponse.rawButton);
        Assert.assertEquals(Button.Previous, previousResponse.button);
        Assert.assertFalse(previousResponse.volumePresent);

        Assert.assertTrue(nextResponse.buttonPresent);
        Assert.assertEquals(0x04, nextResponse.rawButton);
        Assert.assertEquals(Button.Next, nextResponse.button);
        Assert.assertFalse(nextResponse.volumePresent);

        Assert.assertTrue(unknownButtonResponse.buttonPresent);
        Assert.assertEquals((byte) 0xFF, unknownButtonResponse.rawButton);
        Assert.assertEquals(Button.Unknown, unknownButtonResponse.button);
        Assert.assertFalse(unknownButtonResponse.volumePresent);

        Assert.assertFalse(volumeResponse.buttonPresent);
        Assert.assertTrue(volumeResponse.volumePresent);
        Assert.assertEquals(0x42, volumeResponse.volume);

        Assert.assertTrue(combinedResponse.buttonPresent);
        Assert.assertEquals(0x01, combinedResponse.rawButton);
        Assert.assertEquals(Button.PlayPause, combinedResponse.button);
        Assert.assertTrue(combinedResponse.volumePresent);
        Assert.assertEquals(0x42, combinedResponse.volume);
    }
}
