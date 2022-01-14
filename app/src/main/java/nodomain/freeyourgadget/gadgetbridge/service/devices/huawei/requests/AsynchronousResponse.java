package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class AsynchronousResponse extends Request {

    public AsynchronousResponse(HuaweiSupport support) {
        super(support);
    }

    @Override
    public void handleResponse() throws GBException {
        handleFindPhone();
    }

    private void handleFindPhone() {
        if (this.receivedPacket.serviceId == 11 && this.receivedPacket.commandId == 1 && this.receivedPacket.tlv.contains(1)) {
            GBDeviceEventFindPhone findPhoneEvent = new GBDeviceEventFindPhone();
            if (receivedPacket.tlv.getByte(1) == 1)
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.START;
            else
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.STOP;
            support.evaluateGBDeviceEvent(findPhoneEvent);
        }
    }
}
