package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Aw70Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetAw70WorkoutDataRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetAw70WorkoutDataRequest.class);

    Aw70Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers;
    List<Aw70Workout.WorkoutCount.Response.WorkoutNumbers> remainder;
    short number;

    /**
     * Request to get workout totals
     * @param support The support
     * @param workoutNumbers The numbers of the current workout
     * @param remainder The numbers of the remainder if the workouts to get
     * @param number The number of this data request
     */
    public GetAw70WorkoutDataRequest(HuaweiSupport support, Aw70Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers, List<Aw70Workout.WorkoutCount.Response.WorkoutNumbers> remainder, short number) {
        super(support);

        this.serviceId = Aw70Workout.id;
        this.commandId = Aw70Workout.WorkoutData.id;

        this.workoutNumbers = workoutNumbers;
        this.remainder = remainder;
        this.number = number;
    }

    @Override
    protected byte[] createRequest() {
        return new Aw70Workout.WorkoutData.Request(support.secretsProvider, workoutNumbers.workoutNumber, this.number).serialize();
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Aw70Workout.WorkoutData.Response)) {
            // TODO: exception
            return;
        }

        if (((Aw70Workout.WorkoutData.Response) receivedPacket).workoutNumber != this.workoutNumbers.workoutNumber) {
            // TODO: exception or something?
            LOG.error("Incorrect workout number!");
        }

        if (((Aw70Workout.WorkoutData.Response) receivedPacket).dataNumber != this.number) {
            // TODO: exception or something?
            LOG.error("Incorrect data number!");
        }

        // TODO: handle data
        LOG.info("Workout {} data {}:", this.workoutNumbers.workoutNumber, this.number);
        LOG.info("Workout : " + ((Aw70Workout.WorkoutData.Response) receivedPacket).workoutNumber);
        LOG.info("Data num: " + ((Aw70Workout.WorkoutData.Response) receivedPacket).dataNumber);
        LOG.info("Header  : " + Arrays.toString(((Aw70Workout.WorkoutData.Response) receivedPacket).rawHeader));
        LOG.info("Data    : " + Arrays.toString(((Aw70Workout.WorkoutData.Response) receivedPacket).rawData));
        LOG.info("Bitmap  : " + ((Aw70Workout.WorkoutData.Response) receivedPacket).innerBitmap);

        if (this.workoutNumbers.dataCount > this.number + 1) {
            GetAw70WorkoutDataRequest nextRequest = new GetAw70WorkoutDataRequest(
                    this.support,
                    this.workoutNumbers,
                    this.remainder,
                    (short) (this.number + 1)
            );
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        } else if (this.workoutNumbers.paceCount > 0) {
            GetAw70WorkoutPaceRequest nextRequest = new GetAw70WorkoutPaceRequest(
                    this.support,
                    this.workoutNumbers,
                    this.remainder,
                    (short) 0
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
