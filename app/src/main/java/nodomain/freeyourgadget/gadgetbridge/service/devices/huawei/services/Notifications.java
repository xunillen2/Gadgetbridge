package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

public class Notifications {
    public static final int id = 2;

    public static class NotificationAction {
        public static final int send = 1;
        public static final int notificationId = 1;
        public static final int notificationType = 2;
        public static final int vibrate = 3;
        public static final int payloadEmpty = 4;
        public static final int imageHeight = 8;
        public static final int imageWidth = 9;
        public static final int imageColor = 10;
        public static final int imageData = 11;
        public static final int textType = 14;
        public static final int textEncoding = 15;
        public static final int textContent = 16;
        public static final int payloadText = 132;
        public static final int payloadImage = 134;
        public static final int textList = 140;
        public static final int textItem = 141;
    }

    public static class NotificationType {
        public static final int call = 1;
        public static final int sms = 2;
        public static final int weChat = 3;
        public static final int qq = 11;
        public static final int missedCall = 14;
        public static final int email = 15;
        public static final int generic = 127;
    }

    public static class TextType {
        public static final int text = 1;
        public static final int sender = 2;
        public static final int title = 3;
        public static final int yellowPage = 5;
        public static final int contentSign = 6;
        public static final int flight = 7;
        public static final int train = 8;
        public static final int warmRemind = 9;
        public static final int weather = 10;
    }

    public static class TextEncoding {
        public static final int unknown = 1;
        public static final int standard = 2;
    }
}
