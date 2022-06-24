package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsBR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.service.btbr.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiBRSupport;

public class GetWorkoutCountRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetWorkoutCountRequest.class);

    private int start = 0;
    private int end = 0;

    public GetWorkoutCountRequest(HuaweiBRSupport support, TransactionBuilder builder, int start, int end) {
        super(support);

        this.serviceId = Workout.id;
        this.commandId = Workout.WorkoutCount.id;
        this.builder = builder;

        this.start = start;
        this.end = end;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new Workout.WorkoutCount.Request(support.secretsProvider, this.start, this.end).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Workout.WorkoutCount.Response)) {
            // TODO: exception
            return;
        }

        Workout.WorkoutCount.Response packet = (Workout.WorkoutCount.Response) receivedPacket;

        if (packet.count != packet.workoutNumbers.size()) {
            // TODO: exception
            return;
        }

        if (packet.count > 0) {
            GetWorkoutTotalsRequest nextRequest = new GetWorkoutTotalsRequest(
                    this.support,
                    packet.workoutNumbers.remove(0),
                    packet.workoutNumbers
            );
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        }
    }
}
