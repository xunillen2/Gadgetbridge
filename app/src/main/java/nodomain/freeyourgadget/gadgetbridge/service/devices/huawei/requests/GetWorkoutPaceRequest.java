package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiWorkoutGbParser;

public class GetWorkoutPaceRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetWorkoutPaceRequest.class);

    Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers;
    List<Workout.WorkoutCount.Response.WorkoutNumbers> remainder;
    short number;
    Long databaseId;

    public GetWorkoutPaceRequest(HuaweiSupport support, Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers, List<Workout.WorkoutCount.Response.WorkoutNumbers> remainder, short number, Long databaseId) {
        super(support);

        this.serviceId = Workout.id;
        this.commandId = Workout.WorkoutPace.id;

        this.workoutNumbers = workoutNumbers;
        this.remainder = remainder;
        this.number = number;

        this.databaseId = databaseId;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new Workout.WorkoutPace.Request(this.support.paramsProvider ,this.workoutNumbers.workoutNumber, this.number).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Workout.WorkoutPace.Response)) {
            // TODO: exception
            return;
        }

        if (((Workout.WorkoutPace.Response) receivedPacket).workoutNumber != this.workoutNumbers.workoutNumber) {
            // TODO: exception or something?
            LOG.error("Incorrect workout number!");
        }

        if (((Workout.WorkoutPace.Response) receivedPacket).paceNumber != this.number) {
            // TODO: exception or something?
            LOG.error("Incorrect pace number!");
        }

        LOG.info("Workout {} pace {}:", this.workoutNumbers.workoutNumber, this.number);
        LOG.info("Workout  : " + ((Workout.WorkoutPace.Response) receivedPacket).workoutNumber);
        LOG.info("Pace     : " + ((Workout.WorkoutPace.Response) receivedPacket).paceNumber);
        LOG.info("Block num: " + ((Workout.WorkoutPace.Response) receivedPacket).blocks.size());
        LOG.info("Blocks   : " + Arrays.toString(((Workout.WorkoutPace.Response) receivedPacket).blocks.toArray()));

        support.addWorkoutPaceData(this.databaseId, ((Workout.WorkoutPace.Response) receivedPacket).blocks);

        if (this.workoutNumbers.paceCount > this.number + 1) {
            GetWorkoutPaceRequest nextRequest = new GetWorkoutPaceRequest(
                    this.support,
                    this.workoutNumbers,
                    this.remainder,
                    (short) (this.number + 1),
                    this.databaseId
            );
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        } else {
            HuaweiWorkoutGbParser.parseWorkout(this.databaseId);

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
