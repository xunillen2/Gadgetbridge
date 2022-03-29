package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class Notifications {
    public static final byte id = 0x02;

    public static class NotificationActionRequest extends HuaweiPacket {
        public static final byte id = 0x01;

        // TODO: support other types of notifications
        //        public static final int send = 0x01;
        //        public static final int notificationId = 0x01;
        //        public static final int notificationType = 0x02;
        //        public static final int vibrate = 0x03;
        //        public static final int payloadEmpty = 0x04;
        //        public static final int imageHeight = 0x08;
        //        public static final int imageWidth = 0x09;
        //        public static final int imageColor = 0x0A;
        //        public static final int imageData = 0x0B;
        //        public static final int textType = 0x0E;
        //        public static final int textEncoding = 0x0F;
        //        public static final int textContent = 0x10;
        //        public static final int sourceAppId = 0x11;
        //        public static final int payloadText = 0x84;
        //        public static final int payloadImage = 0x86;
        //        public static final int textList = 0x8C;
        //        public static final int textItem = 0x8D;

        public NotificationActionRequest(
                SecretsProvider secretsProvider,
                short notificationId,
                byte notificationType,
                boolean vibrate,
                byte titleEncoding,
                String titleContent,
                byte senderEncoding,
                String senderContent,
                byte bodyEncoding,
                String bodyContent,
                String sourceAppId
        ) {
            super(secretsProvider);

            this.serviceId = Notifications.id;
            this.commandId = id;

            // TODO: Add notification information per type if necessary

            this.tlv = new HuaweiTLV()
                    .put(0x01, notificationId)
                    .put(0x02, notificationType)
                    .put(0x03, vibrate);

            HuaweiTLV subTlv = new HuaweiTLV();
            if (titleContent != null)
                subTlv.put(0x8D, new HuaweiTLV()
                        .put(0x0E, (byte) 0x03)
                        .put(0x0F, titleEncoding)
                        .put(0x10, titleContent)
                );

            if (senderContent != null)
                subTlv.put(0x8D, new HuaweiTLV()
                        .put(0x0E, (byte) 0x02)
                        .put(0x0F, senderEncoding)
                        .put(0x10, senderContent)
                );

            if (bodyContent != null)
                subTlv.put(0x8D, new HuaweiTLV()
                        .put(0x0E, (byte) 0x01)
                        .put(0x0F, bodyEncoding)
                        .put(0x10, bodyContent)
                );

            if (subTlv.length() != 0) {
                this.tlv.put(0x84, new HuaweiTLV().put(0x8C, subTlv));
            } else {
                this.tlv.put(0x04);
            }

            if (sourceAppId != null)
                this.tlv.put(0x11, sourceAppId);

            this.complete = true;
        }
    }

    public static class NotificationType {
        // TODO: enum?

        public static final byte call = 0x01;
        public static final byte sms = 0x02;
        public static final byte weChat = 0x03;
        public static final byte qq = 0x0B;
        public static final byte missedCall = 0x0E;
        public static final byte email = 0x0F;
        public static final byte generic = 0x7F;
    }

    public static class TextType {
        // TODO: enum?

        public static final int text = 0x01;
        public static final int sender = 0x02;
        public static final int title = 0x03;
        public static final int yellowPage = 0x05;
        public static final int contentSign = 0x06;
        public static final int flight = 0x07;
        public static final int train = 0x08;
        public static final int warmRemind = 0x09;
        public static final int weather = 0x0A;
    }

    public static class TextEncoding {
        // TODO: enum?

        public static final byte unknown = 0x01;
        public static final byte standard = 0x02;
    }

    public static class SetNotificationRequest extends HuaweiPacket {
        public static final byte id = 0x04;

        public SetNotificationRequest(
                SecretsProvider secretsProvider,
                boolean activate
        ) {
            super(secretsProvider);

            this.serviceId = Notifications.id;
            this.commandId = id;

            this.tlv = new HuaweiTLV()
                    .put(0x81, new HuaweiTLV()
                            .put(0x02, activate)
                            .put(0x03, activate)
                    );

            this.complete = true;
        }
    }

    public static class SetWearMessagePushRequest extends HuaweiPacket {
        public static final byte id = 0x08;

        public SetWearMessagePushRequest(
                SecretsProvider secretsProvider,
                boolean activate
        ) {
            super(secretsProvider);

            this.serviceId = Notifications.id;
            this.commandId = id;

            this.tlv = new HuaweiTLV()
                    .put(0x01, activate);

            this.complete = true;
        }
    }
}
