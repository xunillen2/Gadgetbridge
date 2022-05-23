package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Aw70Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetAw70WorkoutPaceRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetAw70WorkoutPaceRequest.class);

    Aw70Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers;
    List<Aw70Workout.WorkoutCount.Response.WorkoutNumbers> remainder;
    short number;

    public GetAw70WorkoutPaceRequest(HuaweiSupport support, Aw70Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers, List<Aw70Workout.WorkoutCount.Response.WorkoutNumbers> remainder, short number) {
        super(support);

        this.serviceId = Aw70Workout.id;
        this.commandId = Aw70Workout.WorkoutPace.id;

        this.workoutNumbers = workoutNumbers;
        this.remainder = remainder;
        this.number = number;
    }

    @Override
    protected byte[] createRequest() {
        return new Aw70Workout.WorkoutPace.Request(this.support.secretsProvider ,this.workoutNumbers.workoutNumber, this.number).serialize();
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Aw70Workout.WorkoutPace.Response)) {
            // TODO: exception
            return;
        }

        if (((Aw70Workout.WorkoutPace.Response) receivedPacket).workoutNumber != this.workoutNumbers.workoutNumber) {
            // TODO: exception or something?
            LOG.error("Incorrect workout number!");
        }

        if (((Aw70Workout.WorkoutPace.Response) receivedPacket).paceNumber != this.number) {
            // TODO: exception or something?
            LOG.error("Incorrect pace number!");
        }

        // TODO: handle data
        LOG.info("Workout {} pace {}:", this.workoutNumbers.workoutNumber, this.number);
        LOG.info("Workout  : " + ((Aw70Workout.WorkoutPace.Response) receivedPacket).workoutNumber);
        LOG.info("Pace     : " + ((Aw70Workout.WorkoutPace.Response) receivedPacket).paceNumber);
        LOG.info("Block num: " + ((Aw70Workout.WorkoutPace.Response) receivedPacket).blocks.size());
        LOG.info("Blocks   : " + Arrays.toString(((Aw70Workout.WorkoutPace.Response) receivedPacket).blocks.toArray()));

        if (this.workoutNumbers.paceCount > this.number + 1) {
            GetAw70WorkoutPaceRequest nextRequest = new GetAw70WorkoutPaceRequest(
                    this.support,
                    this.workoutNumbers,
                    this.remainder,
                    (short) (this.number + 1)
            );
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        } else {
            if (remainder.size() > 0) {
                GetAw70WorkoutTotalsRequest nextRequest = new GetAw70WorkoutTotalsRequest(
                        this.support,
                        remainder.remove(0),
                        remainder
                );
                nextRequest.setFinalizeReq(this.finalizeReq);
                this.support.addInProgressRequest(nextRequest);
                this.nextRequest(nextRequest);
            }
        }
    }
}
