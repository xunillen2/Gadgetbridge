package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;

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
        return new FitnessData.MessageCount.Request(support.secretsProvider, this.commandId, this.start, this.end).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        if (!(receivedPacket instanceof FitnessData.MessageCount.Response)) {
            // TODO: exception
            return;
        }

        short count = ((FitnessData.MessageCount.Response) receivedPacket).count;

        if (count > 0) {
            GetStepDataRequest nextRequest = new GetStepDataRequest(this.support, count, (short) 0);
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
