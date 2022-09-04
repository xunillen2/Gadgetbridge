package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;

public class Calls {

    // This doesn't include the initial calling notification, as that is handled
    // by the Notifications class.

    public static final byte id = 0x04;

    // TODO: tests

    public static class AnswerCallResponse extends HuaweiPacket {
        public static final byte id = 0x01;

        public enum Action {
            CALL_ACCEPT,
            CALL_REJECT,
            UNKNOWN
        }

        public Action action = Action.UNKNOWN;

        public AnswerCallResponse(ParamsProvider paramsProvider) {
            super(paramsProvider);

            this.serviceId = Calls.id;
            this.commandId = id;

            this.isEncrypted = false;
        }

        @Override
        public void parseTlv() throws MissingTagException {
            if (this.tlv.contains(0x01)) {
                if (this.tlv.getByte(0x01) == 0x01) {
                    this.action = Action.CALL_REJECT;
                } else if (this.tlv.getByte(0x01) == 0x02) {
                    this.action = Action.CALL_ACCEPT;
                }
                // TODO: find more values, if there are any
            } else {
                throw new MissingTagException(0x01);
            }
        }
    }
}
