package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsBR;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiBRSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.MusicControl;

public class SetMusicStatusRequest extends Request {
    private final int returnValue;

    public SetMusicStatusRequest(HuaweiBRSupport support, byte commandId, int returnValue) {
        super(support);
        this.serviceId = MusicControl.id;
        this.commandId = commandId;
        this.returnValue = returnValue;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new MusicControl.MusicStatusRequest(support.secretsProvider, (byte) commandId, returnValue).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }
}
