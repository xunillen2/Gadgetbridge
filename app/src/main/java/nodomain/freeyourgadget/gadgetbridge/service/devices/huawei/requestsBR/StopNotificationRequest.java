package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsBR;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Notifications;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiBRSupport;

public class StopNotificationRequest extends Request {
    public StopNotificationRequest(HuaweiBRSupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = Notifications.NotificationActionRequest.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new Notifications.NotificationActionRequest(
                    super.getSupport().secretsProvider,
                    (short) support.getNotificationId(),
                    Notifications.NotificationType.stopNotification,
                    false,
                    Notifications.TextEncoding.standard,
                    null,
                    Notifications.TextEncoding.standard,
                    null,
                    Notifications.TextEncoding.standard,
                    null,
                    null
            ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }
}
