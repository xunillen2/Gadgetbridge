package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventMusicControl;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetMusicStatusRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.MusicControl;

/**
 * Handles responses that are not a reply to a request
 */
public class AsynchronousResponse {
    private static final Logger LOG = LoggerFactory.getLogger(AsynchronousResponse.class);

    private final HuaweiSupport support;

    public AsynchronousResponse(HuaweiSupport support) {
        this.support = support;
    }

    public void handleResponse(HuaweiPacket response) {
        if (response.tlv.contains(HuaweiConstants.CryptoTags.encryption))
            response.decrypt(support.getSecretKey());

        handleFindPhone(response);
        handleMusicControls(response);
    }

    private void handleFindPhone(HuaweiPacket response) {
        if (response.serviceId == 11 && response.commandId == 1 && response.tlv.contains(1)) {
            GBDeviceEventFindPhone findPhoneEvent = new GBDeviceEventFindPhone();
            if (response.tlv.getByte(1) == 1)
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.START;
            else
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.STOP;
            support.evaluateGBDeviceEvent(findPhoneEvent);
        }
    }

    /**
     * Handles asynchronous music packet, for the following events:
     *  - The app is opened on the band (sends back music info)
     *  - A button is clicked
     *    - Play/pause
     *    - Previous
     *    - Next
     *  - The volume is adjusted
     * @param response Packet to be handled
     */
    private void handleMusicControls(HuaweiPacket response) {
        if (response.serviceId == MusicControl.id) {
            AudioManager audioManager = (AudioManager) this.support.getContext().getSystemService(Context.AUDIO_SERVICE);

            if (response.commandId == MusicControl.MusicInfoResponse.id) {
                LOG.debug("Music information requested, sending acknowledgement and music info.");
                SetMusicStatusRequest setMusicStatusRequest = new SetMusicStatusRequest(this.support, MusicControl.MusicInfoResponse.id, MusicControl.successValue);
                try {
                    setMusicStatusRequest.perform();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Send Music Info
                this.support.sendSetMusic();
            } else if (response.commandId == MusicControl.Control.id) {
                if (response.tlv.contains(MusicControl.Control.buttonTag)) {
                    GBDeviceEventMusicControl musicControl = new GBDeviceEventMusicControl();
                    switch (response.tlv.getByte(MusicControl.Control.buttonTag)) {
                        case 1:
                            LOG.debug("Music - Play/Pause button event received");
                            musicControl.event = GBDeviceEventMusicControl.Event.PLAYPAUSE;
                            break;
                        case 3:
                            LOG.debug("Music - Previous button event received");
                            musicControl.event = GBDeviceEventMusicControl.Event.PREVIOUS;
                            break;
                        case 4:
                            LOG.debug("Music - Next button event received");
                            musicControl.event = GBDeviceEventMusicControl.Event.NEXT;
                        default:
                    }
                    this.support.evaluateGBDeviceEvent(musicControl);
                }
                if (response.tlv.contains(MusicControl.Control.volumeTag)) {
                    byte volume = response.tlv.getByte(MusicControl.Control.volumeTag);
                    if (volume > audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                        LOG.warn("Music - Received volume is too high: 0x"
                                + Integer.toHexString(volume)
                                + " > 0x"
                                + Integer.toHexString(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
                        );
                        // TODO: probably best to send back an error code, though I wouldn't know which
                        return;
                    }
                    if (Build.VERSION.SDK_INT > 28) {
                        if (volume < audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)) {
                            LOG.warn("Music - Received volume is too low: 0x"
                                    + Integer.toHexString(volume)
                                    + " < 0x"
                                    + audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
                            );
                            // TODO: probably best to send back an error code, though I wouldn't know which
                            return;
                        }
                    }
                    LOG.debug("Music - Setting volume to: 0x" + Integer.toHexString(volume));
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                }
                SetMusicStatusRequest setMusicStatusRequest = new SetMusicStatusRequest(this.support, MusicControl.Control.id, MusicControl.successValue);
                try {
                    setMusicStatusRequest.perform();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
