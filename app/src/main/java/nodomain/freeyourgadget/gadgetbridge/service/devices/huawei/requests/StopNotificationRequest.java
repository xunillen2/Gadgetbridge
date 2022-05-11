package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Notifications;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class StopNotificationRequest extends Request {
    public StopNotificationRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = Notifications.NotificationActionRequest.id;
    }

    @Override
    protected byte[] createRequest() {
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
    }
}
