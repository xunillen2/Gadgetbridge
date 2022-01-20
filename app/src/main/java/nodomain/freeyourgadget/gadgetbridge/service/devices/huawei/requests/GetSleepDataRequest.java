package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.SleepData;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class GetSleepDataRequest extends Request {
private static final Logger LOG = LoggerFactory.getLogger(GetSleepDataRequest.class);

    short maxCount = 0;
    short count = 0;

    public GetSleepDataRequest(HuaweiSupport support, TransactionBuilder builder, short maxCount, short count) {
        // super(support, builder);
        super(support);
        this.serviceId = SleepData.id;
        this.commandId = SleepData.MessageData.id;
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
                            SleepData.MessageData.request_container_tag,
                            new HuaweiTLV()
                                .put(SleepData.MessageData.request_container_number_tag, this.count)
                    )
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request fitness time data: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        // TODO: proper debugging print?

        LOG.debug("Received packet: " + receivedPacket.tlv.toString());

        HuaweiTLV container = receivedPacket.tlv.getObject(SleepData.MessageData.response_container_tag);
        short receivedCount = container.getShort(SleepData.MessageData.response_container_number_tag);

        if (receivedCount != this.count) {
            LOG.warn("Counts do not match");
        }

        container = container.getObject(SleepData.MessageData.response_container_container_tag);

        byte data = container.getByte(SleepData.MessageData.response_container_container_data_tag);

        byte[] timestampBytes = container.getBytes(SleepData.MessageData.response_container_container_timestamp_tag);
        long[] timestampLongs = new long[6];

        for (int i = 0; i < 6; i++) {
            if (timestampBytes[i] >= 0)
                timestampLongs[i] = timestampBytes[i];
            else
                timestampLongs[i] = timestampBytes[i] & 0xFF;
        }

        long timestampLong =
              (timestampLongs[0] << 24L) +
              (timestampLongs[1] << 16L) +
              (timestampLongs[2] << 8L) +
              (timestampLongs[3]);
        long durationLong =
                (timestampLongs[4] << 8L) +
                (timestampLongs[5]);
        durationLong = durationLong * 60;

        Date startTime = new Date(timestampLong * 1000);
        Date stopTime = new Date((timestampLong + durationLong) * 1000);

        LOG.debug("Start: " + startTime + ", stop: " + stopTime + ", data: " + data);

        // TODO: save the data

        if (count + 1 < maxCount) {
            GetSleepDataRequest nextRequest = new GetSleepDataRequest(this.support, this.builder, this.maxCount, (short) (this.count + 1));
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
