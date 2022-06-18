package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetWorkoutDataRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetWorkoutDataRequest.class);

    Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers;
    List<Workout.WorkoutCount.Response.WorkoutNumbers> remainder;
    short number;
    Long databaseId;

    /**
     * Request to get workout totals
     * @param support The support
     * @param workoutNumbers The numbers of the current workout
     * @param remainder The numbers of the remainder if the workouts to get
     * @param number The number of this data request
     */
    public GetWorkoutDataRequest(HuaweiSupport support, Workout.WorkoutCount.Response.WorkoutNumbers workoutNumbers, List<Workout.WorkoutCount.Response.WorkoutNumbers> remainder, short number, Long databaseId) {
        super(support);

        this.serviceId = Workout.id;
        this.commandId = Workout.WorkoutData.id;

        this.workoutNumbers = workoutNumbers;
        this.remainder = remainder;
        this.number = number;

        this.databaseId = databaseId;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new Workout.WorkoutData.Request(support.secretsProvider, workoutNumbers.workoutNumber, this.number).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws Exception {
        if (!(receivedPacket instanceof Workout.WorkoutData.Response)) {
            // TODO: exception
            return;
        }

        if (((Workout.WorkoutData.Response) receivedPacket).workoutNumber != this.workoutNumbers.workoutNumber) {
            // TODO: exception or something?
            LOG.error("Incorrect workout number!");
        }

        if (((Workout.WorkoutData.Response) receivedPacket).dataNumber != this.number) {
            // TODO: exception or something?
            LOG.error("Incorrect data number!");
        }

        LOG.info("Workout {} data {}:", this.workoutNumbers.workoutNumber, this.number);
        LOG.info("Workout : " + ((Workout.WorkoutData.Response) receivedPacket).workoutNumber);
        LOG.info("Data num: " + ((Workout.WorkoutData.Response) receivedPacket).dataNumber);
        LOG.info("Header  : " + Arrays.toString(((Workout.WorkoutData.Response) receivedPacket).rawHeader));
        LOG.info("Header  : " + ((Workout.WorkoutData.Response) receivedPacket).header);
        LOG.info("Data    : " + Arrays.toString(((Workout.WorkoutData.Response) receivedPacket).rawData));
        LOG.info("Data    : " + Arrays.toString(((Workout.WorkoutData.Response) receivedPacket).dataList.toArray()));
        LOG.info("Bitmap  : " + ((Workout.WorkoutData.Response) receivedPacket).innerBitmap);

        this.support.addWorkoutSampleData(
                this.databaseId,
                ((Workout.WorkoutData.Response) receivedPacket).dataList
        );

        if (this.workoutNumbers.dataCount > this.number + 1) {
            GetWorkoutDataRequest nextRequest = new GetWorkoutDataRequest(
                    this.support,
                    this.workoutNumbers,
                    this.remainder,
                    (short) (this.number + 1),
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
                    this.databaseId
            );
            nextRequest.setFinalizeReq(this.finalizeReq);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
        } else {
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
