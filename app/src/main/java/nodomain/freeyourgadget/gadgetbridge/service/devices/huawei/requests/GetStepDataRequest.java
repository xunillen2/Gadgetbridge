package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packetobjects.FitnessData;

public class GetStepDataRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetStepDataRequest.class);

    short maxCount;
    short count;

    public GetStepDataRequest(HuaweiSupport support, short maxCount, short count) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.MessageData.stepId;
        this.maxCount = maxCount;
        this.count = count;
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                this.serviceId,
                this.commandId,
                FitnessData.MessageData.Request.toTlv(this.count)
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        FitnessData.MessageData.StepResponse response = FitnessData.MessageData.StepResponse.fromTlv(receivedPacket.tlv);

        if (response.number != this.count) {
            LOG.warn("Counts do not match");
        }

        for (FitnessData.MessageData.StepResponse.SubContainer subContainer : response.containers) {
            int dataTimestamp = subContainer.timestamp;

            if (subContainer.parsedData != null) {
                short steps = (short) subContainer.steps;
                short calories = (short) subContainer.calories;
                short distance = (short) subContainer.distance;

                if (steps == -1)
                    steps = 0;
                if (calories == -1)
                    calories = 0;
                if (distance == -1)
                    distance = 0;

                for (FitnessData.MessageData.StepResponse.SubContainer.TV tv : subContainer.unknownTVs) {
                    LOG.warn("Unknown tag in step data: " + tv);
                }

                this.support.addStepData(dataTimestamp, steps, calories, distance);
            } else {
                LOG.error(subContainer.parsedDataError);
            }

            if (count + 1 < maxCount) {
                GetStepDataRequest nextRequest = new GetStepDataRequest(this.support, this.maxCount, (short) (this.count + 1));
                nextRequest.setFinalizeReq(this.finalizeReq);
                this.support.addInProgressRequest(nextRequest);
                this.nextRequest(nextRequest);
            }
        }
    }
}
