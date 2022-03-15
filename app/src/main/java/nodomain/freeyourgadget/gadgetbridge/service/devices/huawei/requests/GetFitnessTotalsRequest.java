package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetFitnessTotalsRequest extends Request {

    public GetFitnessTotalsRequest(HuaweiSupport support) {
        super(support);

        this.serviceId = 0x07;
        this.commandId = 0x03;
    }

    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
                serviceId,
                commandId,
                new HuaweiTLV().put(0x01)
        ).encrypt(support.getSecretKey(), support.getIV());
        return requestedPacket.serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        int total_steps = 0;
        int total_calories = 0;
        int total_distance = 0;

        HuaweiTLV container = receivedPacket.tlv.getObject(0x81);
        List<HuaweiTLV> containers = container.getObjects(0x83);

        for (HuaweiTLV tlv : containers) {
            if (tlv.contains(0x05))
                total_steps += tlv.getInteger(0x05);
            if (tlv.contains(0x06))
                total_calories += tlv.getShort(0x06);
            if (tlv.contains(0x07))
                total_distance += tlv.getInteger(0x07);
        }

        support.addTotalFitnessData(total_steps, total_calories, total_distance);
    }
}
