package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;

public class FindPhoneResponse extends HuaweiPacket {

    public static final byte id = 0x0b;
    public static final byte responseId = 0x01;

    public boolean start = false;

    public FindPhoneResponse(SecretsProvider secretsProvider) {
        super(secretsProvider);

        this.serviceId = id;
        this.commandId = responseId;
    }

    @Override
    protected void parseTlv() {
        if (this.tlv.contains(0x01)) {
            this.start = this.tlv.getBoolean(0x01);
        }
        // No missing tag exception so it will stop by default
    }
}
