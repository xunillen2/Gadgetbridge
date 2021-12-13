package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class SendNotificationRequest extends Request {

    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationRequest.class);

    private HuaweiTLV notificationTLV;

    public SendNotificationRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = Notifications.NotificationAction.send;
    }

    public void buildNotificationTLVFromSpec(NotificationSpec notificationSpec) {
        HuaweiTLV notificationTitle = new HuaweiTLV()
                .put(Notifications.Tags.textType, (byte) Notifications.TextType.title)
                .put(Notifications.Tags.textEncoding, (byte) Notifications.TextEncoding.standard)
                .put(Notifications.Tags.textContent, notificationSpec.title);

        HuaweiTLV notificationSender = new HuaweiTLV()
                .put(Notifications.Tags.textType, (byte) Notifications.TextType.sender)
                .put(Notifications.Tags.textEncoding, (byte) Notifications.TextEncoding.standard)
                .put(Notifications.Tags.textContent, notificationSpec.sender);


        HuaweiTLV notificationText = new HuaweiTLV()
                .put(Notifications.Tags.textType, (byte) Notifications.TextType.text)
                .put(Notifications.Tags.textEncoding, (byte) Notifications.TextEncoding.standard)
                .put(Notifications.Tags.textContent, notificationSpec.body);

        HuaweiTLV notificationTLV = new HuaweiTLV()
                .put(Notifications.Tags.notificationId, (int) notificationSpec.getId())
                .put(Notifications.Tags.notificationType, (byte) Notifications.NotificationType.generic)
                .put(Notifications.Tags.vibrate, (byte) 1)
                .put(Notifications.Tags.payloadText, new HuaweiTLV().put(Notifications.Tags.textList, new HuaweiTLV()
                        .put(Notifications.Tags.textItem, notificationTitle)
                        .put(Notifications.Tags.textItem, notificationText)
                        .put(Notifications.Tags.textItem, notificationSender)
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
        LOG.debug("Notification Request: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Locale");
    }
}
