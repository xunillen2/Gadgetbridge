package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData.MessageData;

public class GetSleepDataRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSleepDataRequest.class);

    short maxCount = 0;
    short count = 0;

    public GetSleepDataRequest(HuaweiSupport support, short maxCount, short count) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.MessageData.sleepId;

        this.maxCount = maxCount;
        this.count = count;
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                this.serviceId,
                this.commandId,
                new HuaweiTLV()
                    .put(
                            MessageData.requestContainerTag,
                            new HuaweiTLV()
                                .put(MessageData.requestContainerNumberTag, this.count)
                    )
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        HuaweiTLV parentContainer = receivedPacket.tlv.getObject(MessageData.responseContainerTag);
        short receivedCount = parentContainer.getShort(MessageData.responseContainerNumberTag);

        if (receivedCount != this.count) {
            LOG.warn("Counts do not match");
        }

        for (HuaweiTLV container : parentContainer.getObjects(FitnessData.MessageData.sleepResponseContainerContainerTag)) {
            byte type = container.getByte(FitnessData.MessageData.sleepResponseContainerContainerDataTag);

            byte[] timestampBytes = container.getBytes(FitnessData.MessageData.sleepResponseContainerContainerTimestampTag);

            int[] timestampInts = new int[6];

            for (int i = 0; i < 6; i++) {
                if (timestampBytes[i] >= 0)
                    timestampInts[i] = timestampBytes[i];
                else
                    timestampInts[i] = timestampBytes[i] & 0xFF;
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

            this.support.addSleepActivity(timestamp, duration, type);
        }

        if (count + 1 < maxCount) {
            GetSleepDataRequest nextRequest = new GetSleepDataRequest(this.support, this.maxCount, (short) (this.count + 1));
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
