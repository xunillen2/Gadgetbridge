package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData;

public class GetStepDataCountRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetStepDataCountRequest.class);

    private int start = 0;
    private int end = 0;

    public GetStepDataCountRequest(HuaweiSupport support, int start, int end) {
        super(support);

        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.MessageCount.stepId;

        this.start = start;
        this.end = end;
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                serviceId,
                commandId,
                new HuaweiTLV()
                    .put(FitnessData.MessageCount.requestUnknownTag)
                    .put(FitnessData.MessageCount.requestStartTag, this.start)
                    .put(FitnessData.MessageCount.requestEndTag, this.end)
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        short count = receivedPacket.tlv
                .getObject(FitnessData.MessageCount.responseContainerTag)
                .getShort(FitnessData.MessageCount.responseContainerCountTag);

        if (count > 0) {
            GetStepDataRequest nextRequest = new GetStepDataRequest(this.support, count, (short) 0);
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
