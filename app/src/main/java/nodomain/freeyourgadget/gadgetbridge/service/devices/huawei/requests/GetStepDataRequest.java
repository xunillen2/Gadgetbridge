package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData;

public class GetStepDataRequest extends Request {

    private static class TV {
        public final byte tag;
        public final short value;

        public TV(byte tag, short value) {
            this.tag = tag;
            this.value = value;
        }

        @Override
        public String toString() {
            return "TV{" +
                    "tag=" + tag +
                    ", value=" + value +
                    '}';
        }
    }

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
                new HuaweiTLV()
                    .put(
                            FitnessData.MessageData.requestContainerTag,
                            new HuaweiTLV()
                                .put(FitnessData.MessageData.requestContainerNumberTag, this.count)
                    )
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        HuaweiTLV container = receivedPacket.tlv.getObject(FitnessData.MessageData.responseContainerTag);
        short receivedCount = container.getShort(FitnessData.MessageData.responseContainerNumberTag);
        int timestamp = container.getInteger(FitnessData.MessageData.stepResponseContainerTimestampTag);

        if (receivedCount != this.count) {
            LOG.warn("Counts do not match");
        }

        List<HuaweiTLV> containers = container.getObjects(FitnessData.MessageData.stepResponseContainerContainerTag);
        for (HuaweiTLV dataContainer : containers) {
            byte timestampOffset = dataContainer.getByte(FitnessData.MessageData.stepResponseContainerContainerTimeOffsetTag);
            int dataTimestamp = timestamp + 60 * timestampOffset;

            byte[] data = dataContainer.getBytes(FitnessData.MessageData.stepResponseContainerContainerDataTag);
            List<TV> tagValuePairs = parseData(data);

            if (tagValuePairs != null) {
                short steps = 0;
                short calories = 0;
                short distance = 0;
                for (TV tv : tagValuePairs) {
                    if (tv.tag == 0x02)
                        steps = tv.value;
                    else if (tv.tag == 0x04)
                        calories = tv.value;
                    else if (tv.tag == 0x08)
                        distance = tv.value;
                    else
                        LOG.warn("Unknown tag in step data: " + tv);
                }
                this.support.addStepData(dataTimestamp, steps, calories, distance);
            }

            if (count + 1 < maxCount) {
                GetStepDataRequest nextRequest = new GetStepDataRequest(this.support, this.maxCount, (short) (this.count + 1));
                nextRequest.setFinalizeReq(this.finalizeReq);
                this.support.addInProgressRequest(nextRequest);
                this.nextRequest(nextRequest);
            }
        }
    }

    private List<TV> parseData(byte[] data) {
        int i = 0;

        if (data.length <= 0) {
            LOG.error("Data is missing feature bitmap.");
            return null;
        }
        byte featureBitmap1 = data[i++];

        byte featureBitmap2 = 0;
        if ((featureBitmap1 & 128) != 0) {
            if (data.length <= i) {
                LOG.error("Data is missing second feature bitmap.");
                return null;
            }
            featureBitmap2 = data[i++];
        }

        List<TV> tagValuePairs = new ArrayList<>();

        // The greater than zero check is because Java is always signed, so we only check 7 bits
        for (byte bitToCheck = 1; bitToCheck > 0; bitToCheck <<= 1) {
            if ((featureBitmap1 & bitToCheck) != 0) {
                if (data.length + 2 < i) {
                    LOG.error("Data is too short for selected features.");
                    return null;
                }

                if (bitToCheck == 0x40 || bitToCheck == 0x20) {
                    // TODO: support data from 0x40 and 0x20
                    LOG.warn("Data announced by 0x40 or 0x20 is currently not supported.");
                    i++;
                } else {
                    short value = (short) ((data[i++] & 0xFF) << 8 | (data[i++] & 0xFF));

                    // TODO: maybe not use the checked bit as tag?
                    tagValuePairs.add(new TV(bitToCheck, value));
                }

                /*
                 * 2 is steps
                 * 4 is calorie count
                 * 8 is distance
                 */
            }
        }

        // TODO: second bitmap?

        return tagValuePairs;
    }
}
