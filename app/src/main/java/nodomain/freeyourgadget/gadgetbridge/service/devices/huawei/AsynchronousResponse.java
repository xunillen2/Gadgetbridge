package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;

/**
 * Handles responses that are not a reply to a request
 */
public class AsynchronousResponse {

    private final HuaweiSupport support;

    public AsynchronousResponse(HuaweiSupport support) {
        this.support = support;
    }

    public void handleResponse(HuaweiPacket response) {
        handleFindPhone(response);
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
}
