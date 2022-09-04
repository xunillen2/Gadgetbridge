package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;

public class GetSecurityNegotiationRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSecurityNegotiationRequest.class);
    private final byte authMode;

    public GetSecurityNegotiationRequest(HuaweiSupport support, byte authMode) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.SecurityNegotiationRequest.id;
        this.authMode = authMode;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new DeviceConfig.SecurityNegotiationRequest(
                    support.paramsProvider,
                    this.authMode,
                    support.getAndroidId(),
                    Build.MODEL
                ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Security and Negotiation");
    }
}
