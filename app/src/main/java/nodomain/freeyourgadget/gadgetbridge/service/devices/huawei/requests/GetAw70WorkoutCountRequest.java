package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Aw70Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetAw70WorkoutCountRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetAw70WorkoutCountRequest.class);

    private int start = 0;
    private int end = 0;

    public GetAw70WorkoutCountRequest(HuaweiSupport support, int start, int end) {
        super(support);

        this.serviceId = Aw70Workout.id;
        this.commandId = Aw70Workout.WorkoutCount.id;

        this.start = start;
        this.end = end;
    }

    @Override
    protected byte[] createRequest() {
        return new Aw70Workout.WorkoutCount.Request(support.secretsProvider, this.start, this.end).serialize();
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Aw70Workout.WorkoutCount.Response)) {
            // TODO: exception
            return;
        }

        Aw70Workout.WorkoutCount.Response packet = (Aw70Workout.WorkoutCount.Response) receivedPacket;

        if (packet.count != packet.workoutNumbers.size()) {
            // TODO: exception
            return;
        }

        if (packet.count > 0) {
            GetAw70WorkoutTotalsRequest nextRequest = new GetAw70WorkoutTotalsRequest(
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
