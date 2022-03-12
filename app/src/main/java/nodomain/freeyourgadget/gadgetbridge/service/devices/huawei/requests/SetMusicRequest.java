package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import android.content.Context;
import android.media.AudioManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class SetMusicRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetMusicRequest.class);

    private final MusicStateSpec musicStateSpec;
    private final MusicSpec musicSpec;

    public SetMusicRequest(HuaweiSupport support, MusicStateSpec musicStateSpec, MusicSpec musicSpec) {
        super(support);
        this.serviceId = 37; // TODO
        this.commandId = 2; // TODO
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
                        .put(0x01, artistName)
                        .put(0x02, songName)
                        .put(0x03, playState)
                        .put(0x04, maxVolume)
                        .put(0x05, currentVolume)
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    // TODO: response?
}
