package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;

public class GetSleepDataRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSleepDataRequest.class);

    private final short maxCount;
    private final short count;

    public GetSleepDataRequest(HuaweiSupport support, short maxCount, short count) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.MessageData.sleepId;

        this.maxCount = maxCount;
        this.count = count;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new FitnessData.MessageData.Request(support.paramsProvider, this.commandId, this.count).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        // FitnessData.MessageData.SleepResponse response = FitnessData.MessageData.SleepResponse.fromTlv(receivedPacket.tlv);
        if (!(receivedPacket instanceof FitnessData.MessageData.SleepResponse)) {
            // TODO: exception
            return;
        }

        FitnessData.MessageData.SleepResponse response = (FitnessData.MessageData.SleepResponse) receivedPacket;

        short receivedCount = response.number;

        if (receivedCount != this.count) {
            LOG.warn("Counts do not match");
        }

        for (FitnessData.MessageData.SleepResponse.SubContainer subContainer : response.containers) {
            // TODO: it might make more sense to convert the timestamp in the FitnessData class
            int[] timestampInts = new int[6];

            for (int i = 0; i < 6; i++) {
                if (subContainer.timestamp[i] >= 0)
                    timestampInts[i] = subContainer.timestamp[i];
                else
                    timestampInts[i] = subContainer.timestamp[i] & 0xFF;
            }

            int timestamp =
                    (timestampInts[0] << 24) +
                            (timestampInts[1] << 16) +
                            (timestampInts[2] << 8) +
                            (timestampInts[3]);

            int durationInt =
                    (timestampInts[4] << 8L) +
                            (timestampInts[5]);
            short duration = (short) (durationInt * 60);

            this.support.addSleepActivity(timestamp, duration, subContainer.type);
        }

        if (count + 1 < maxCount) {
            GetSleepDataRequest nextRequest = new GetSleepDataRequest(this.support, this.maxCount, (short) (this.count + 1));
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
