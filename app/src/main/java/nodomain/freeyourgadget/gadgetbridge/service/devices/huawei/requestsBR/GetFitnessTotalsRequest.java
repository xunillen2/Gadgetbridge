package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsBR;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiBRSupport;

public class GetFitnessTotalsRequest extends Request {

    public GetFitnessTotalsRequest(HuaweiBRSupport support) {
        super(support);

        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.FitnessTotals.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new FitnessData.FitnessTotals.Request(support.secretsProvider).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        if (!(receivedPacket instanceof FitnessData.FitnessTotals.Response)) {
            // TODO: exception
            return;
        }

        int totalSteps = ((FitnessData.FitnessTotals.Response) receivedPacket).totalSteps;
        int totalCalories = ((FitnessData.FitnessTotals.Response) receivedPacket).totalCalories;
        int totalDistance = ((FitnessData.FitnessTotals.Response) receivedPacket).totalDistance;

        support.addTotalFitnessData(totalSteps, totalCalories, totalDistance);
    }
}