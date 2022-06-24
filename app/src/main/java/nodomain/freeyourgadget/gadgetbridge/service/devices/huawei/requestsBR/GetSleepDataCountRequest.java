package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsBR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.btbr.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiBRSupport;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;

public class GetSleepDataCountRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSleepDataCountRequest.class);
    private final int start;
    private final int end;

    public GetSleepDataCountRequest(HuaweiBRSupport support, TransactionBuilder builder, int start, int end) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.MessageCount.sleepId;
        this.builder = builder;

        this.start = start;
        this.end = end;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new FitnessData.MessageCount.Request(support.secretsProvider, this.commandId, this.start, this.end).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        if (!(receivedPacket instanceof FitnessData.MessageCount.Response)) {
            // TODO: exception
            return;
        }

        short count = ((FitnessData.MessageCount.Response) receivedPacket).count;

        if (count > 0) {
            GetSleepDataRequest nextRequest = new GetSleepDataRequest(this.support, count, (short) 0);
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
