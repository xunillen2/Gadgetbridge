package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBEnvironment;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;

public class HuaweiSampleProvider extends AbstractSampleProvider<HuaweiActivitySample> {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiSampleProvider.class);

    /*
     * We save all data by saving a marker at the begin and end.
     * Meaning of fields that are not self-explanatory:
     *  - `otherTimestamp`
     *    The timestamp of the other marker, if it's larger this is the begin, otherwise the end
     *  - `source`
     *    The source of the data, which Huawei Band message the data came from
     */

    private static class RawTypes {
        public static final int NOT_MEASURED = -1;

        public static final int UNKNOWN = 1;

        public static final int DEEP_SLEEP = 0x07;
        public static final int LIGHT_SLEEP = 0x06;
    }

    public HuaweiSampleProvider(GBDevice device, DaoSession session) {
        super(device, session);
    }

    @Override
    public int normalizeType(int rawType) {
        switch (rawType) {
            case RawTypes.DEEP_SLEEP:
                return ActivityKind.TYPE_DEEP_SLEEP;
            case RawTypes.LIGHT_SLEEP:
                return ActivityKind.TYPE_LIGHT_SLEEP;
            default:
                return ActivityKind.TYPE_UNKNOWN;
        }
    }

    @Override
    public int toRawActivityKind(int activityKind) {
        switch (activityKind) {
            case ActivityKind.TYPE_DEEP_SLEEP:
                return RawTypes.DEEP_SLEEP;
            case ActivityKind.TYPE_LIGHT_SLEEP:
                return RawTypes.LIGHT_SLEEP;
            default:
                return RawTypes.NOT_MEASURED;
        }
    }

    @Override
    public float normalizeIntensity(int rawIntensity) {
        return rawIntensity;
    }

    @Override
    public AbstractDao<HuaweiActivitySample, ?> getSampleDao() {
        return getSession().getHuaweiActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return HuaweiActivitySampleDao.Properties.RawKind;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return HuaweiActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return HuaweiActivitySampleDao.Properties.DeviceId;
    }

    @Override
    public HuaweiActivitySample createActivitySample() {
        return new HuaweiActivitySample();
    }

    private int getLastFetchTimestamp(QueryBuilder<HuaweiActivitySample> qb) {
        Device dbDevice = DBHelper.findDevice(getDevice(), getSession());
        if (dbDevice == null)
            return 0;
        Property deviceProperty = HuaweiActivitySampleDao.Properties.DeviceId;
        Property timestampProperty = HuaweiActivitySampleDao.Properties.Timestamp;

        qb.where(deviceProperty.eq(dbDevice.getId()))
                .orderDesc(timestampProperty)
                .limit(1);

        List<HuaweiActivitySample> samples = qb.build().list();
        if (samples.isEmpty())
            return 0;

        HuaweiActivitySample sample = samples.get(0);
        return sample.getTimestamp();
    }

    /**
     * Gets last timestamp where the sleep data has been fully synchronized
     * @return Last fully synchronized timestamp for sleep data
     */
    public int getLastSleepFetchTimestamp() {
        QueryBuilder<HuaweiActivitySample> qb = getSampleDao().queryBuilder();
        Property sourceProperty = HuaweiActivitySampleDao.Properties.Source;
        Property activityTypeProperty = HuaweiActivitySampleDao.Properties.RawKind;

        qb.where(sourceProperty.eq(0x0d), activityTypeProperty.eq(0x01));

        return getLastFetchTimestamp(qb);
    }

    /**
     * Gets last timestamp where the step data has been fully synchronized
     * @return Last fully synchronized timestamp for step data
     */
    public int getLastStepFetchTimestamp() {
        QueryBuilder<HuaweiActivitySample> qb = getSampleDao().queryBuilder();
        Property sourceProperty = HuaweiActivitySampleDao.Properties.Source;

        qb.where(sourceProperty.eq(0x0b));

        return getLastFetchTimestamp(qb);
    }

    /**
     * Makes a copy of a sample
     * @param sample The sample to copy
     * @return The copy of the sample
     */
    private HuaweiActivitySample copySample(HuaweiActivitySample sample) {
        HuaweiActivitySample sampleCopy = new HuaweiActivitySample(
                sample.getTimestamp(),
                sample.getDeviceId(),
                sample.getUserId(),
                sample.getOtherTimestamp(),
                sample.getSource(),
                sample.getRawKind(),
                sample.getRawIntensity(),
                sample.getSteps(),
                sample.getCalories(),
                sample.getDistance(),
                sample.getSpo(),
                sample.getHeartRate()
        );
        sampleCopy.setProvider(sample.getProvider());
        return sampleCopy;
    }

    @Override
    public void addGBActivitySample(HuaweiActivitySample activitySample) {
        HuaweiActivitySample start = copySample(activitySample);
        HuaweiActivitySample end = copySample(activitySample);
        end.setTimestamp(start.getOtherTimestamp());
        end.setSteps(ActivitySample.NOT_MEASURED);
        end.setCalories(ActivitySample.NOT_MEASURED);
        end.setDistance(ActivitySample.NOT_MEASURED);
        end.setSpo(ActivitySample.NOT_MEASURED);
        end.setHeartRate(ActivitySample.NOT_MEASURED);
        end.setOtherTimestamp(start.getTimestamp());

        getSampleDao().insertOrReplace(start);
        getSampleDao().insertOrReplace(end);
    }

    @Override
    public void addGBActivitySamples(HuaweiActivitySample[] activitySamples) {
        List<HuaweiActivitySample> newSamples = new ArrayList<>();
        for (HuaweiActivitySample sample : activitySamples) {
            HuaweiActivitySample start = copySample(sample);
            HuaweiActivitySample end = copySample(sample);
            end.setTimestamp(start.getOtherTimestamp());
            end.setSteps(ActivitySample.NOT_MEASURED);
            end.setCalories(ActivitySample.NOT_MEASURED);
            end.setDistance(ActivitySample.NOT_MEASURED);
            end.setSpo(ActivitySample.NOT_MEASURED);
            end.setHeartRate(ActivitySample.NOT_MEASURED);
            end.setOtherTimestamp(start.getTimestamp());

            newSamples.add(start);
            newSamples.add(end);
        }
        getSampleDao().insertOrReplaceInTx((HuaweiActivitySample[]) newSamples.toArray());
    }

    /**
     * Gets the activity samples, ordered by timestamp
     * @param timestampFrom Start timestamp
     * @param timestampTo End timestamp
     * @return List of activities between the timestamps, ordered by timestamp
     */
    private List<HuaweiActivitySample> getRawOrderedActivitySamples(int timestampFrom, int timestampTo) {
        QueryBuilder<HuaweiActivitySample> qb = getSampleDao().queryBuilder();
        Property timestampProperty = getTimestampSampleProperty();
        Device dbDevice = DBHelper.findDevice(getDevice(), getSession());
        if (dbDevice == null) {
            // no device, no samples
            return Collections.emptyList();
        }
        Property deviceProperty = getDeviceIdentifierSampleProperty();
        qb.where(deviceProperty.eq(dbDevice.getId()), timestampProperty.ge(timestampFrom))
                .where(timestampProperty.le(timestampTo))
                .orderAsc(timestampProperty);
        List<HuaweiActivitySample> samples = qb.build().list();
        for (HuaweiActivitySample sample : samples) {
            sample.setProvider(this);
        }
        detachFromSession();
        return samples;
    }

    private List<HuaweiWorkoutDataSample> getRawOrderedWorkoutSamplesWithHeartRate(int timestampFrom, int timestampTo) {
        Device dbDevice = DBHelper.findDevice(getDevice(), getSession());
        if (dbDevice == null)
            return Collections.emptyList();

        QueryBuilder<HuaweiWorkoutDataSample> qb = getSession().getHuaweiWorkoutDataSampleDao().queryBuilder();
        Property timestampProperty = HuaweiWorkoutDataSampleDao.Properties.Timestamp;
        Property heartRateProperty = HuaweiWorkoutDataSampleDao.Properties.HeartRate;
        Property deviceProperty = HuaweiWorkoutSummarySampleDao.Properties.DeviceId;
        qb.join(HuaweiWorkoutDataSampleDao.Properties.WorkoutId, HuaweiWorkoutSummarySample.class, HuaweiWorkoutSummarySampleDao.Properties.WorkoutId)
                .where(deviceProperty.eq(dbDevice.getId()));
        qb.where(
                timestampProperty.ge(timestampFrom),
                timestampProperty.le(timestampTo),
                heartRateProperty.notEq(ActivitySample.NOT_MEASURED)
        ).orderAsc(timestampProperty);
        List<HuaweiWorkoutDataSample> samples = qb.build().list();
        getSession().getHuaweiWorkoutSummarySampleDao().detachAll();
        return samples;
    }

    private class SampleLoopState {
        public long deviceId = 0;
        public long userId = 0;

        int[] activityTypes = {};

        public int sleepModifier = 0;
        public boolean insideStepMarker = false;
    }

    /*
     * Note that this does a lot more than the normal implementation, as it takes care of everything
     * that is necessary for proper displaying of data.
     *
     * This essentially boils down to four things:
     *  - It adds in the workout heart rate data
     *  - It adds a sample with intensity zero before start markers (start of block)
     *  - It adds a sample with intensity zero after end markers (end of block)
     *  - It modifies some blocks so the sleep data gets handled correctly
     * The second and fourth are necessary for proper stats calculation, the third is mostly for
     * nicer graphs.
     *
     * Note that the data in the database isn't changed, as the samples are detached.
     */
    @Override
    protected List<HuaweiActivitySample> getGBActivitySamples(int timestamp_from, int timestamp_to, int activityType) {
        // Note that the result of this function has to be sorted by timestamp!

        List<HuaweiActivitySample> rawSamples = getRawOrderedActivitySamples(timestamp_from, timestamp_to);
        List<HuaweiWorkoutDataSample> workoutSamples = getRawOrderedWorkoutSamplesWithHeartRate(timestamp_from, timestamp_to);

        /*
         * This is a list of all samples handled so far
         * The last sample can either be:
         *  - The last handled sample, or
         *  - A post sample
         * A last handled sample will always have an equal or smaller timestamp.
         * A post sample can have a smaller, equal, or one-greater timestamp.
         */
        List<HuaweiActivitySample> processedSamples = new ArrayList<>();

        Iterator<HuaweiActivitySample> itRawSamples = rawSamples.iterator();
        Iterator<HuaweiWorkoutDataSample> itWorkoutSamples = workoutSamples.iterator();

        HuaweiActivitySample nextRawSample = null;
        if (itRawSamples.hasNext())
            nextRawSample = itRawSamples.next();
        HuaweiWorkoutDataSample nextWorkoutSample = null;
        if (itWorkoutSamples.hasNext())
            nextWorkoutSample = itWorkoutSamples.next();

        SampleLoopState state = new SampleLoopState();
        if (nextRawSample != null) {
            state.deviceId = nextRawSample.getDeviceId();
            state.userId = nextRawSample.getUserId();
        }
        state.activityTypes = ActivityKind.mapToDBActivityTypes(activityType, this);

        while (nextRawSample != null || nextWorkoutSample != null) {
            if (nextRawSample == null) {
                processWorkoutSample(processedSamples, state, nextWorkoutSample);

                nextWorkoutSample = null;
                if (itWorkoutSamples.hasNext())
                    nextWorkoutSample = itWorkoutSamples.next();
            } else if (nextWorkoutSample == null) {
                processRawSample(processedSamples, state, nextRawSample);

                nextRawSample = null;
                if (itRawSamples.hasNext())
                    nextRawSample = itRawSamples.next();
            } else if (nextRawSample.getTimestamp() > nextWorkoutSample.getTimestamp()) {
                processWorkoutSample(processedSamples, state, nextWorkoutSample);

                nextWorkoutSample = null;
                if (itWorkoutSamples.hasNext())
                    nextWorkoutSample = itWorkoutSamples.next();
            } else {
                processRawSample(processedSamples, state, nextRawSample);

                nextRawSample = null;
                if (itRawSamples.hasNext())
                    nextRawSample = itRawSamples.next();
            }
        }

        int lastTime = 0;
        for (HuaweiActivitySample sample : processedSamples) {
            if (sample.getTimestamp() < lastTime)
                LOG.error("Out of order timestamp!");
            lastTime = sample.getTimestamp();
        }

        LOG.info("Raw samples: " + rawSamples.size() + ", Workout samples: " + workoutSamples.size() + ", Processed samples: " + processedSamples.size());

        return processedSamples;
    }

    /**
     * Process raw activity sample
     * @param processedSamples Resulting list, see {@link getGBActivitySamples} for requirements on list
     * @param state Current state
     * @param sample Sample to process
     * @param activityTypes Activity types to filer on
     */
    private void processRawSample(List<HuaweiActivitySample> processedSamples, SampleLoopState state, HuaweiActivitySample sample) {
        // Filter on Source 0x0d, Type 0x01, until we know what it is and how we should handle them.
        // Just showing them currently has some issues.
        if (sample.getSource() == FitnessData.MessageData.sleepId && sample.getRawKind() == RawTypes.UNKNOWN)
            return;

        // Filter samples on activity type
        if (state.activityTypes.length != 0) {
            boolean typeFound = false;
            for (int type : state.activityTypes) {
                if (type == sample.getRawKind()) {
                    typeFound = true;
                    break;
                }
            }
            if (!typeFound)
                return;
        }

        boolean isStartSample = sample.getTimestamp() <= sample.getOtherTimestamp();
        boolean isEndSample = sample.getTimestamp() >= sample.getOtherTimestamp();

        int processedSize = processedSamples.size();
        int lastTimestamp = 0;
        if (processedSize > 0)
            lastTimestamp = processedSamples.get(processedSize - 1).getTimestamp();
        if (lastTimestamp > sample.getTimestamp()) {
            // Last must be post sample, one before must have an equal timestamp
            // Remove post sample, if necessary it will be added again later
            processedSamples.remove(processedSize - 1);
            // The one before will have the same timestamp, move it back one
            // While technically a collision is possible, it practically won't happen
            processedSamples.get(processedSize - 2).setTimestamp(sample.getTimestamp() - 1);
            if (state.sleepModifier != 0)
                sample.setRawKind(state.sleepModifier);
            sample.setProvider(this);
            processedSamples.add(sample);
        } else if (lastTimestamp == sample.getTimestamp()) {
            // Equal timestamps, move previous one back
            // While technically a collision is possible, it practically won't happen
            processedSamples.get(processedSize - 1).setTimestamp(sample.getTimestamp() - 1);
            if (state.sleepModifier != 0)
                sample.setRawKind(state.sleepModifier);
            sample.setProvider(this);
            processedSamples.add(sample);
        } else {
            if (isStartSample && state.sleepModifier == 0 && lastTimestamp < sample.getTimestamp() - 1) {
                // Sample is start sample, add presample if nothing is present and sleep modifier is zero
                HuaweiActivitySample preSample = new HuaweiActivitySample(
                        sample.getTimestamp() - 1,
                        sample.getDeviceId(),
                        sample.getUserId(),
                        0,
                        (byte) 0x00,
                        ActivitySample.NOT_MEASURED,
                        0,
                        ActivitySample.NOT_MEASURED,
                        ActivitySample.NOT_MEASURED,
                        ActivitySample.NOT_MEASURED,
                        ActivitySample.NOT_MEASURED,
                        ActivitySample.NOT_MEASURED
                );
                preSample.setProvider(this);
                processedSamples.add(preSample);
            }

            // Handle modifiers
            if (state.sleepModifier != 0)
                sample.setRawKind(state.sleepModifier);
            if (isStartSample && state.insideStepMarker)
                LOG.info("Start marker inside step marker.");

            // Add sample
            sample.setProvider(this);
            processedSamples.add(sample);
        }

        // Change state
        if (sample.getSource() == FitnessData.MessageData.sleepId && (sample.getRawKind() == RawTypes.LIGHT_SLEEP || sample.getRawKind() == RawTypes.DEEP_SLEEP)) {
            if (isStartSample)
                state.sleepModifier = sample.getRawKind();
            if (isEndSample)
                state.sleepModifier = 0;
        }
        if (sample.getSource() == FitnessData.MessageData.stepId) {
            if (isStartSample)
                state.insideStepMarker = true;
            if (isEndSample)
                state.insideStepMarker = false;
        }

        if (isEndSample && state.sleepModifier == 0) {
            // Sample is end sample, add post sample
            HuaweiActivitySample postSample = new HuaweiActivitySample(
                    sample.getTimestamp() + 1,
                    sample.getDeviceId(),
                    sample.getUserId(),
                    0,
                    (byte) 0x00,
                    ActivitySample.NOT_MEASURED,
                    0,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED
            );
            postSample.setProvider(this);
            processedSamples.add(postSample);
        }

        if (isEndSample && state.insideStepMarker)
            LOG.info("End marker inside step marker.");
    }

    /**
     * Process workout sample
     * Raw activity samples with the same timestamp must be handled first!
     * @param processedSamples Resulting list, see {@link getGBActivitySamples} for requirements on list
     * @param state Current state
     * @param workoutSample Sample to process
     */
    private void processWorkoutSample(List<HuaweiActivitySample> processedSamples, SampleLoopState state, HuaweiWorkoutDataSample workoutSample) {
        int processedSize = processedSamples.size();
        if (processedSize > 1 && processedSamples.get(processedSize - 2).getTimestamp() == workoutSample.getTimestamp()) {
            processedSamples.get(processedSize - 2).setHeartRate(workoutSample.getHeartRate());
        } else if (processedSize > 0 && processedSamples.get(processedSize - 1).getTimestamp() == workoutSample.getTimestamp()) {
            processedSamples.get(processedSize - 1).setHeartRate(workoutSample.getHeartRate());
        } else {
            HuaweiActivitySample newSample = new HuaweiActivitySample(
                    workoutSample.getTimestamp(),
                    state.deviceId,
                    state.userId,
                    0,
                    (byte) 0x00,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    workoutSample.getHeartRate()
            );
            newSample.setProvider(this);
            processedSamples.add(newSample);
        }
    }
}
