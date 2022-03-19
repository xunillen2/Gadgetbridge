package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetFitnessTotalsRequest extends Request {

    public GetFitnessTotalsRequest(HuaweiSupport support) {
        super(support);

        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.FitnessTotals.id;
    }

    @Override
    protected byte[] createRequest() {
        return new FitnessData.FitnessTotals.Request(support.secretsProvider).serialize();
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
