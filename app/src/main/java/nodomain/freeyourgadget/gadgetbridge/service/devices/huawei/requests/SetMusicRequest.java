package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import android.content.Context;
import android.media.AudioManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.MusicControl;

public class SetMusicRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetMusicRequest.class);

    private final MusicStateSpec musicStateSpec;
    private final MusicSpec musicSpec;

    public SetMusicRequest(HuaweiSupport support, MusicStateSpec musicStateSpec, MusicSpec musicSpec) {
        super(support);
        this.serviceId = MusicControl.id;
        this.commandId = MusicControl.MusicInfo.id;
        this.musicStateSpec = musicStateSpec;
        this.musicSpec = musicSpec;
    }

    @Override
    protected byte[] createRequest() {
        String artistName = "";
        String songName = "";
        byte playState = MusicStateSpec.STATE_UNKNOWN;
        if (this.musicSpec != null) {
            artistName = this.musicSpec.artist;
            songName = this.musicSpec.track;
        }
        if (this.musicStateSpec != null)
            playState = this.musicStateSpec.state;
        AudioManager audioManager = (AudioManager) this.support.getContext().getSystemService(Context.AUDIO_SERVICE);
        byte maxVolume = (byte) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        byte currentVolume = (byte) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        requestedPacket = new HuaweiPacket(
                this.serviceId,
                this.commandId,
                new HuaweiTLV()
                        .put(MusicControl.MusicInfo.artistNameTag, artistName)
                        .put(MusicControl.MusicInfo.songNameTag, songName)
                        .put(MusicControl.MusicInfo.playStateTag, playState)
                        .put(MusicControl.MusicInfo.maxVolumeTag, maxVolume)
                        .put(MusicControl.MusicInfo.currentVolumeTag, currentVolume)
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        if (receivedPacket.tlv.contains(MusicControl.statusTag)) {
            if (receivedPacket.tlv.getShort(MusicControl.statusTag) == MusicControl.successValue) {
                LOG.debug("Music information acknowledged by band");
            } else {
                LOG.debug("Music information error code: " + Integer.toHexString(receivedPacket.tlv.getShort(MusicControl.statusTag)));
            }
        } else {
            LOG.warn("Music information response no status tag");
        }
    }
}
