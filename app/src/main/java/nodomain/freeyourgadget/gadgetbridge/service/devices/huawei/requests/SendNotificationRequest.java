package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Notifications;

public class SendNotificationRequest extends Request {

    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationRequest.class);

    private HuaweiPacket packet;
    private boolean vibrate;

    public SendNotificationRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = Notifications.NotificationActionRequest.id;
        this.vibrate = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getBoolean(DeviceSettingsPreferenceConst.PREF_VIBRATION_ENABLE, false);
    }

    public static byte getNotificationType(NotificationType type) {
        switch (type.getGenericType()) {
            case "generic_social":
            case "generic_chat":
                return Notifications.NotificationType.weChat;
            case "generic_email":
                return Notifications.NotificationType.email;
            case "generic":
                return Notifications.NotificationType.generic;
            default:
                return Notifications.NotificationType.sms;
        }
    }


    public void buildNotificationTLVFromNotificationSpec(NotificationSpec notificationSpec) {
        String title;
        if (notificationSpec.title != null)
            title = notificationSpec.title;
        else
            title = notificationSpec.sourceName;

        this.packet = new Notifications.NotificationActionRequest(
                support.secretsProvider,
                (short) support.getNotificationId(),
                getNotificationType(notificationSpec.type),
                vibrate,
                Notifications.TextEncoding.standard,
                title,
                Notifications.TextEncoding.standard,
                notificationSpec.sender,
                Notifications.TextEncoding.standard,
                notificationSpec.body,
                notificationSpec.sourceAppId
        );
    }

    public void buildNotificationTLVFromCallSpec(CallSpec callSpec) {
        this.packet = new Notifications.NotificationActionRequest(
                support.secretsProvider,
                (short) support.getNotificationId(),
                Notifications.NotificationType.call,
                vibrate,
                Notifications.TextEncoding.standard,
                callSpec.name,
                Notifications.TextEncoding.standard,
                callSpec.name,
                Notifications.TextEncoding.standard,
                callSpec.name,
                null
        );
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return this.packet.serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Notification");
    }
}
