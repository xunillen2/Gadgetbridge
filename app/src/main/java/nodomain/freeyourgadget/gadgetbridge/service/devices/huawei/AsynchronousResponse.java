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

    private void handleMusicControls(HuaweiPacket response) {
        if (response.serviceId == 37) {
            AudioManager audioManager = (AudioManager) this.support.getContext().getSystemService(Context.AUDIO_SERVICE);

            if (response.commandId == 1) {
                SetMusicStatusRequest setMusicStatusRequest = new SetMusicStatusRequest(this.support, 1, 100000);
                try {
                    setMusicStatusRequest.perform();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Send Music Info
                this.support.sendSetMusic();
            } else if (response.commandId == 3) {
                if (response.tlv.contains(1)) {
                    GBDeviceEventMusicControl musicControl = new GBDeviceEventMusicControl();
                    switch (response.tlv.getByte(1)) {
                        case 1:
                            musicControl.event = GBDeviceEventMusicControl.Event.PLAYPAUSE;
                            break;
                        case 3:
                            musicControl.event = GBDeviceEventMusicControl.Event.PREVIOUS;
                            break;
                        case 4:
                            musicControl.event = GBDeviceEventMusicControl.Event.NEXT;
                        default:
                    }
                    this.support.evaluateGBDeviceEvent(musicControl);
                }
                if (response.tlv.contains(2)) {
                    byte volume = response.tlv.getByte(2);
                    if (volume > audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                        // Not possible
                        // TODO: handle nicely
                        return;
                    }
                    if (Build.VERSION.SDK_INT > 28) {
                        if (volume < audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)) {
                            // Not possible
                            // TODO: handle nicely
                            return;
                        }
                    }
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                }
                SetMusicStatusRequest setMusicStatusRequest = new SetMusicStatusRequest(this.support, 3, 100000);
                try {
                    setMusicStatusRequest.perform();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
