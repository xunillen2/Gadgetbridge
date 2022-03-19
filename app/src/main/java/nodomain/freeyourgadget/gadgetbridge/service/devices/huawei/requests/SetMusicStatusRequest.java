package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.MusicControl;

public class SetMusicStatusRequest extends Request {
    private final int returnValue;

    public SetMusicStatusRequest(HuaweiSupport support, byte commandId, int returnValue) {
        super(support);
        this.serviceId = MusicControl.id;
        this.commandId = commandId;
        this.returnValue = returnValue;
    }

    @Override
    protected byte[] createRequest() {
        return new MusicControl.MusicStatusRequest(support.secretsProvider, (byte) commandId, returnValue).serialize();
    }
}
