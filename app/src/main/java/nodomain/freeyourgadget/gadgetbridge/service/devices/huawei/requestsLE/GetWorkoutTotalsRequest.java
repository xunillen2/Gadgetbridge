package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiLESupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiWorkoutGbParser;

public class GetWorkoutTotalsRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetWorkoutTotalsRequest.class);

    Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers;
    List<Workout.WorkoutCount.Response.WorkoutNumbers> remainder;

    /**
     * Request to get workout totals
     * @param support The support
     * @param workoutNumbers The numbers of the current workout
     * @param remainder The numbers of the remainder of the workouts to get
     */
    public GetWorkoutTotalsRequest(HuaweiLESupport support, Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers, List<Workout.WorkoutCount.Response.WorkoutNumbers> remainder) {
        super(support);

        this.serviceId = Workout.id;
        this.commandId = Workout.WorkoutTotals.id;

        this.workoutNumbers = workoutNumbers;
        this.remainder = remainder;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new Workout.WorkoutTotals.Request(support.secretsProvider, workoutNumbers.workoutNumber).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Workout.WorkoutTotals.Response)) {
            // TODO: exception
            return;
        }

        if (((Workout.WorkoutTotals.Response) receivedPacket).number != this.workoutNumbers.workoutNumber) {
            // TODO: exception or something?
            LOG.error("Incorrect workout number!");
        }

        LOG.info("Workout {} totals:", this.workoutNumbers.workoutNumber);
        LOG.info("Number  : " + ((Workout.WorkoutTotals.Response) receivedPacket).number);
        LOG.info("Status  : " + ((Workout.WorkoutTotals.Response) receivedPacket).status);
        LOG.info("Start   : " + ((Workout.WorkoutTotals.Response) receivedPacket).startTime);
        LOG.info("End     : " + ((Workout.WorkoutTotals.Response) receivedPacket).endTime);
        LOG.info("Calories: " + ((Workout.WorkoutTotals.Response) receivedPacket).calories);
        LOG.info("Distance: " + ((Workout.WorkoutTotals.Response) receivedPacket).distance);
        LOG.info("Steps   : " + ((Workout.WorkoutTotals.Response) receivedPacket).stepCount);
        LOG.info("Time    : " + ((Workout.WorkoutTotals.Response) receivedPacket).totalTime);
        LOG.info("Duration: " + ((Workout.WorkoutTotals.Response) receivedPacket).duration);
        LOG.info("Type    : " + ((Workout.WorkoutTotals.Response) receivedPacket).type);

        Long databaseId = this.support.addWorkoutTotalsData((Workout.WorkoutTotals.Response) receivedPacket);

        // Create the next request
        if (this.workoutNumbers.dataCount > 0) {
            GetWorkoutDataRequest nextRequest = new GetWorkoutDataRequest(
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
            GetWorkoutPaceRequest nextRequest = new GetWorkoutPaceRequest(
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
            HuaweiWorkoutGbParser.parseWorkout(databaseId);

            if (remainder.size() > 0) {
                GetWorkoutTotalsRequest nextRequest = new GetWorkoutTotalsRequest(
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
