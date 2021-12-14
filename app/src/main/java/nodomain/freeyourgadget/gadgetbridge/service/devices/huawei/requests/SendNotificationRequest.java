package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications.NotificationAction;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications.NotificationType;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications.TextType;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications.TextEncoding;

public class SendNotificationRequest extends Request {

    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationRequest.class);

    private HuaweiTLV notificationTLV;
    private boolean vibrate;

    public SendNotificationRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = NotificationAction.send;
        this.vibrate = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getBoolean(DeviceSettingsPreferenceConst.PREF_VIBRATION_ENABLE, false);
    }

    public void buildNotificationTLVFromNotificationSpec(NotificationSpec notificationSpec) {
        HuaweiTLV notificationTitle = new HuaweiTLV()
                .put(NotificationAction.textType, (byte) TextType.title)
                .put(NotificationAction.textEncoding, (byte) TextEncoding.standard)
                .put(NotificationAction.textContent, notificationSpec.title);

        HuaweiTLV notificationSender = new HuaweiTLV()
                .put(NotificationAction.textType, (byte) TextType.sender)
                .put(NotificationAction.textEncoding, (byte) TextEncoding.standard)
                .put(NotificationAction.textContent, notificationSpec.sender);

        HuaweiTLV notificationText = new HuaweiTLV()
                .put(NotificationAction.textType, (byte) TextType.text)
                .put(NotificationAction.textEncoding, (byte) TextEncoding.standard)
                .put(NotificationAction.textContent, notificationSpec.body);

        HuaweiTLV notificationTLV = new HuaweiTLV()
                .put(NotificationAction.notificationId, (int) notificationSpec.getId())
                .put(NotificationAction.notificationType, (byte) NotificationType.generic)
                .put(NotificationAction.vibrate, vibrate)
                .put(NotificationAction.payloadText, new HuaweiTLV().put(NotificationAction.textList, new HuaweiTLV()
                        .put(NotificationAction.textItem, notificationTitle)
                        .put(NotificationAction.textItem, notificationText)
                        .put(NotificationAction.textItem, notificationSender)
                ));

        this.notificationTLV = notificationTLV;
    }

    public void buildNotificationTLVFromCallSpec(CallSpec callSpec) {
        HuaweiTLV notificationTitle = new HuaweiTLV()
                .put(NotificationAction.textType, (byte) TextType.title)
                .put(NotificationAction.textEncoding, (byte) TextEncoding.standard)
                .put(NotificationAction.textContent, callSpec.name);

        HuaweiTLV notificationSender = new HuaweiTLV()
                .put(NotificationAction.textType, (byte) TextType.sender)
                .put(NotificationAction.textEncoding, (byte) TextEncoding.standard)
                .put(NotificationAction.textContent, callSpec.name);

        HuaweiTLV notificationText = new HuaweiTLV()
                .put(NotificationAction.textType, (byte) TextType.text)
                .put(NotificationAction.textEncoding, (byte) TextEncoding.standard)
                .put(NotificationAction.textContent, callSpec.name);

        HuaweiTLV notificationTLV = new HuaweiTLV()
                .put(NotificationAction.notificationId, (int) 1)
                .put(NotificationAction.notificationType, (byte) NotificationType.call)
                .put(NotificationAction.vibrate, vibrate)
                .put(NotificationAction.payloadText, new HuaweiTLV().put(NotificationAction.textList, new HuaweiTLV()
                        .put(NotificationAction.textItem, notificationSender)
                        .put(NotificationAction.textItem, notificationTitle)
                        .put(NotificationAction.textItem, notificationText)
                ));

        this.notificationTLV = notificationTLV;
    }

    @Override
    protected byte[] createRequest() {
        if (notificationTLV != null) {
            requestedPacket = new HuaweiPacket(
                    serviceId,
                    commandId,
                    notificationTLV
            ).encrypt(support.getSecretKey(), support.getIV());
        }
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Send Notification Request: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Notification");
    }
}
