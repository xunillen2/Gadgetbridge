package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventCallControl;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventMusicControl;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Calls;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FindPhoneResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetMusicStatusRequest;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.MusicControl;

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
        handleFindPhone(response);
        handleMusicControls(response);
        handleCallControls(response);
    }

    private void handleFindPhone(HuaweiPacket response) {
        if (response.serviceId == FindPhoneResponse.id && response.commandId == FindPhoneResponse.responseId) {
            if (!(response instanceof FindPhoneResponse)) {
                // TODO: exception
                return;
            }

            GBDeviceEventFindPhone findPhoneEvent = new GBDeviceEventFindPhone();
            if (((FindPhoneResponse) response).start)
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

            if (response.commandId == MusicControl.MusicStatusResponse.id) {
                LOG.debug("Music information requested, sending acknowledgement and music info.");
                SetMusicStatusRequest setMusicStatusRequest = new SetMusicStatusRequest(this.support, MusicControl.MusicStatusResponse.id, MusicControl.successValue);
                try {
                    setMusicStatusRequest.perform();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Send Music Info
                this.support.sendSetMusic();
            } else if (response.commandId == MusicControl.Control.id) {
                MusicControl.Control.Response resp = (MusicControl.Control.Response) response;

                if (resp.buttonPresent) {
                    if (resp.button != MusicControl.Control.Response.Button.Unknown) {
                        GBDeviceEventMusicControl musicControl = new GBDeviceEventMusicControl();
                        switch (resp.button) {
                            case PlayPause:
                                LOG.debug("Music - Play/Pause button event received");
                                musicControl.event = GBDeviceEventMusicControl.Event.PLAYPAUSE;
                                break;
                            case Previous:
                                LOG.debug("Music - Previous button event received");
                                musicControl.event = GBDeviceEventMusicControl.Event.PREVIOUS;
                                break;
                            case Next:
                                LOG.debug("Music - Next button event received");
                                musicControl.event = GBDeviceEventMusicControl.Event.NEXT;
                            default:
                        }
                        this.support.evaluateGBDeviceEvent(musicControl);
                    }
                }
                if (resp.volumePresent) {
                    byte volume = resp.volume;
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

    private void handleCallControls(HuaweiPacket response) {
        if (response.serviceId == Calls.id && response.commandId == Calls.AnswerCallResponse.id) {
            if (!(response instanceof Calls.AnswerCallResponse)) {
                // TODO: exception
                return;
            }

            SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(support.getDevice().getAddress());

            GBDeviceEventCallControl callControlEvent = new GBDeviceEventCallControl();
            switch (((Calls.AnswerCallResponse) response).action) {
                case UNKNOWN:
                    LOG.info("Unknown action for call");
                    return;
                case CALL_ACCEPT:
                    callControlEvent.event = GBDeviceEventCallControl.Event.ACCEPT;
                    LOG.info("Accepted call");

                    if (!prefs.getBoolean("enable_call_accept", true)) {
                        LOG.info("Disabled accepting calls, ignoring");
                        return;
                    }

                    break;
                case CALL_REJECT:
                    callControlEvent.event = GBDeviceEventCallControl.Event.REJECT;
                    LOG.info("Rejected call");

                    if (!prefs.getBoolean("enable_call_reject", true)) {
                        LOG.info("Disabled rejecting calls, ignoring");
                        return;
                    }

                    break;
            }
            support.evaluateGBDeviceEvent(callControlEvent);
        }
    }
}
