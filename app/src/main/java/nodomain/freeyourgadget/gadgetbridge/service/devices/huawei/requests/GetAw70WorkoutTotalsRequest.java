package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Aw70Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetAw70WorkoutTotalsRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetAw70WorkoutTotalsRequest.class);

    Aw70Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers;
    List<Aw70Workout.WorkoutCount.Response.WorkoutNumbers> remainder;

    /**
     * Request to get workout totals
     * @param support The support
     * @param workoutNumbers The numbers of the current workout
     * @param remainder The numbers of the remainder of the workouts to get
     */
    public GetAw70WorkoutTotalsRequest(HuaweiSupport support, Aw70Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers, List<Aw70Workout.WorkoutCount.Response.WorkoutNumbers> remainder) {
        super(support);

        this.serviceId = Aw70Workout.id;
        this.commandId = Aw70Workout.WorkoutTotals.id;

        this.workoutNumbers = workoutNumbers;
        this.remainder = remainder;
    }

    @Override
    protected byte[] createRequest() {
        return new Aw70Workout.WorkoutTotals.Request(support.secretsProvider, workoutNumbers.workoutNumber).serialize();
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Aw70Workout.WorkoutTotals.Response)) {
            // TODO: exception
            return;
        }

        if (((Aw70Workout.WorkoutTotals.Response) receivedPacket).number != this.workoutNumbers.workoutNumber) {
            // TODO: exception or something?
            LOG.error("Incorrect workout number!");
        }

        LOG.info("Workout {} totals:", this.workoutNumbers.workoutNumber);
        LOG.info("Number  : " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).number);
        LOG.info("Status  : " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).status);
        LOG.info("Start   : " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).startTime);
        LOG.info("End     : " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).endTime);
        LOG.info("Calories: " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).calories);
        LOG.info("Distance: " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).distance);
        LOG.info("Steps   : " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).stepCount);
        LOG.info("Time    : " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).totalTime);
        LOG.info("Duration: " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).duration);
        LOG.info("Type    : " + ((Aw70Workout.WorkoutTotals.Response) receivedPacket).type);

        Long databaseId = this.support.addWorkoutTotalsData((Aw70Workout.WorkoutTotals.Response) receivedPacket);

        // Create the next request
        if (this.workoutNumbers.dataCount > 0) {
            GetAw70WorkoutDataRequest nextRequest = new GetAw70WorkoutDataRequest(
                    this.support,
                    this.workoutNumbers,
                    this.remainder,
                    (short) 0,
                    databaseId
            );
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        } else if (this.workoutNumbers.paceCount > 0) {
            GetAw70WorkoutPaceRequest nextRequest = new GetAw70WorkoutPaceRequest(
                    this.support,
                    this.workoutNumbers,
                    this.remainder,
                    (short) 0,
                    databaseId
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
