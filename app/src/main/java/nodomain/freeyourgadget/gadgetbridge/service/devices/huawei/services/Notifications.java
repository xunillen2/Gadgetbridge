package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

public class Notifications {
    public static final int id = 0x02;

    public static class NotificationAction {
        public static final int send = 0x01;
        public static final int notificationId = 0x01;
        public static final int notificationType = 0x02;
        public static final int vibrate = 0x03;
        public static final int payloadEmpty = 0x04;
        public static final int imageHeight = 0x08;
        public static final int imageWidth = 0x09;
        public static final int imageColor = 0x0A;
        public static final int imageData = 0x0B;
        public static final int textType = 0x0E;
        public static final int textEncoding = 0x0F;
        public static final int textContent = 0x10;
        public static final int sourceAppId = 0x11;
        public static final int payloadText = 0x84;
        public static final int payloadImage = 0x86;
        public static final int textList = 0x8C;
        public static final int textItem = 0x8D;
    }

    public static class NotificationType {
        public static final int call = 0x01;
        public static final int sms = 0x02;
        public static final int weChat = 0x03;
        public static final int qq = 0x0B;
        public static final int missedCall = 0x0E;
        public static final int email = 0x0F;
        public static final int generic = 0x7F;
    }

    public static class TextType {
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
        public static final int unknown = 0x01;
        public static final int standard = 0x02;
    }

    public static class SetNotification {
        public static final int id = 0x04;
        public static final int container = 0x81;
        public static final int setStatus = 0x02;
        public static final int setStatus2 = 0x03;
    }

    public static class SetWearMessagePush {
        public static final int id = 0x08;
        public static final int setStatus = 0x01;
    }

}
