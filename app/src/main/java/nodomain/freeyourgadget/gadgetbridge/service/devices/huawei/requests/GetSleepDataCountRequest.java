package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceBusyAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class GetSleepDataCountRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSleepDataCountRequest.class);
    private int start = 0;
    private int end = 0;

    public GetSleepDataCountRequest(HuaweiSupport support, int start, int end) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.MessageCount.id;

        this.start = start;
        this.end = end;

        this.builder.add(new SetDeviceBusyAction(getDevice(), getContext().getString(R.string.busy_task_fetch_activity_data), getContext()));
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                serviceId,
                commandId,
                new HuaweiTLV()
                    .put(FitnessData.MessageCount.request_unknown_tag)
                    .put(FitnessData.MessageCount.request_start_tag, this.start)
                    .put(FitnessData.MessageCount.request_end_tag, this.end)
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        short count = receivedPacket.tlv
                .getObject(FitnessData.MessageCount.response_container_tag)
                .getShort(FitnessData.MessageCount.response_container_count_tag);

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
        } else {
            try {
                operationFinished();
                unsetBusy();
                GB.signalActivityDataFinish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
