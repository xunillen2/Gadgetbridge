package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.MusicControl;

public class SetMusicStatusRequest extends Request {
    private final int returnValue;

    public SetMusicStatusRequest(HuaweiSupport support, int commandId,  int returnValue) {
        super(support);
        this.serviceId = MusicControl.id;
        this.commandId = commandId;
        this.returnValue = returnValue;
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                this.serviceId,
                this.commandId,
                new HuaweiTLV()
                    .put(MusicControl.statusTag, this.returnValue)
        );
        return requestedPacket.serialize();
    }
}
