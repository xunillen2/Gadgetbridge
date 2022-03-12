package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class SetMusicStatusRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetMusicStatusRequest.class);

    private final int returnValue;

    public SetMusicStatusRequest(HuaweiSupport support, int commandId,  int returnValue) {
        super(support);
        this.serviceId = 37; // TODO
        this.commandId = commandId;
        this.returnValue = returnValue;
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                this.serviceId,
                this.commandId,
                new HuaweiTLV()
                    .put(0x7F, this.returnValue)
        );
        return requestedPacket.serialize();
    }
}
