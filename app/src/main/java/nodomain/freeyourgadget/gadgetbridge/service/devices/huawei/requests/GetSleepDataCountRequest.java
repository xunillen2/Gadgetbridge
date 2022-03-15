package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packetobjects.FitnessData;

public class GetSleepDataCountRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSleepDataCountRequest.class);
    private int start = 0;
    private int end = 0;

    public GetSleepDataCountRequest(HuaweiSupport support, TransactionBuilder builder, int start, int end) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.MessageCount.sleepId;
        this.builder = builder;

        this.start = start;
        this.end = end;
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                serviceId,
                commandId,
                FitnessData.MessageCount.Request.toTlv(this.start, this.end)
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        short count = FitnessData.MessageCount.Response.fromTlv(receivedPacket.tlv).count;

        if (count > 0) {
            GetSleepDataRequest nextRequest = new GetSleepDataRequest(this.support, count, (short) 0);
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
