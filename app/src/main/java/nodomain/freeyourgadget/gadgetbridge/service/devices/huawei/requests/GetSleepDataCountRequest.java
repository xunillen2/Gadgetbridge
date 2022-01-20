package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceBusyAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.SleepData;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class GetSleepDataCountRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSleepDataCountRequest.class);

    public GetSleepDataCountRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = SleepData.id;
        this.commandId = SleepData.MessageCount.id;

        // TODO: sleep data specific?
        this.builder.add(new SetDeviceBusyAction(getDevice(), getContext().getString(R.string.busy_task_fetch_activity_data), getContext()));
    }

    @Override
    protected byte[] createRequest() {
        int start = 1642392000; // TODO: last update time? Or last couple of days?
        int end = (int) (System.currentTimeMillis() / 1000);

        requestedPacket = new HuaweiPacket(
                serviceId,
                commandId,
                new HuaweiTLV()
                    .put(SleepData.MessageCount.request_unknown_tag)
                    .put(SleepData.MessageCount.request_start_tag, start)
                    .put(SleepData.MessageCount.request_end_tag, end)
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request fitness time data count: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        // TODO: proper debugging print?
        LOG.debug("Handle StatusFrame Time Data count");

        short count = receivedPacket.tlv
                .getObject(SleepData.MessageCount.response_container_tag)
                .getShort(SleepData.MessageCount.response_container_count_tag);

        if (count > 0) {
            GetSleepDataRequest nextRequest = new GetSleepDataRequest(this.support, this.builder, count, (short) 0);
            nextRequest.setFinalizeReq(new RequestCallback() {
                @Override
                public void call() {
                    try {
                        operationFinished();
                        unsetBusy();
                        GB.signalActivityDataFinish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
