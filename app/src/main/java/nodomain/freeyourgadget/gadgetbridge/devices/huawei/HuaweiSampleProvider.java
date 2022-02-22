package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData;

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
                sample.getDistance()
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

    /*
     * Note that this does a lot more than the normal implementation, as it takes care of everything
     * that is necessary for proper displaying of data.
     *
     * This essentially boils down to three things:
     *  - It adds a sample with intensity zero before start markers (start of block)
     *  - It adds a sample with intensity zero after end markers (end of block)
     *  - It modifies some blocks so the sleep data gets handled correctly
     * The first and third are necessary for proper stats calculation, the second is mostly for
     * nicer graphs.
     *
     * Note that the data in the database isn't changed, as the samples are detached.
     */
    @Override
    protected List<HuaweiActivitySample> getGBActivitySamples(int timestamp_from, int timestamp_to, int activityType) {
        // Note that the result of this function has to be sorted by timestamp!

        List<HuaweiActivitySample> rawSamples = getRawOrderedActivitySamples(timestamp_from, timestamp_to);
        List<HuaweiActivitySample> processedSamples = new ArrayList<>();

        int[] activityTypes = ActivityKind.mapToDBActivityTypes(activityType, this);

        // TODO: make sure no data can have the same timestamp
        //       This seems to sometimes happen when zooming in on a graph, switching to another
        //       tab, and then switching back

        int lastHandledTimestamp = 0;
        HuaweiActivitySample postSample = null;
        HuaweiActivitySample endSample = null;

        ArrayList<HuaweiActivitySample> endOutOfBoundsSamples = new ArrayList<>();

        int sleepModifier = 0;
        boolean insideStepMarker = false;

        for (HuaweiActivitySample sample : rawSamples) {
            // Filter on Source 0x0d, Type 0x01, until we know what it is and how we should
            // handle them. Just showing them currently has some issues.
            if (sample.getSource() == FitnessData.MessageData.sleepId && sample.getRawKind() == RawTypes.UNKNOWN)
                continue;

            // Filter samples on activity type
            if (activityTypes.length != 0) {
                boolean typeFound = false;
                for (int type : activityTypes) {
                    if (type == sample.getRawKind()) {
                        typeFound = true;
                        break;
                    }
                }
                if (!typeFound)
                    continue;
            }

            if (endSample != null) {
                if (endSample.getTimestamp() == sample.getTimestamp()) {
                    int offset = 1;
                    for (int i = processedSamples.size() - 1; i > 0; i--) {
                        if (processedSamples.get(i).getTimestamp() == endSample.getTimestamp() - offset)
                            offset -= 1;
                        else
                            break;
                    }
                    endSample.setTimestamp(endSample.getTimestamp() - offset);
                }
                processedSamples.add(endSample);
                endSample = null;
            }

            if (postSample != null) {
                if (postSample.getTimestamp() < sample.getTimestamp())
                    processedSamples.add(postSample);
                postSample = null;
            }

            if (sample.getTimestamp() < sample.getOtherTimestamp()) {
                // Start mark

                // Handle if the end marker is after the end timestamp
                if (sample.getOtherTimestamp() > timestamp_to) {
                    HuaweiActivitySample endOutOfBoundsSample = copySample(sample);
                    endOutOfBoundsSample.setTimestamp(timestamp_to);
                    endOutOfBoundsSample.setSteps(ActivitySample.NOT_MEASURED);
                    endOutOfBoundsSample.setCalories(ActivitySample.NOT_MEASURED);
                    endOutOfBoundsSample.setDistance(ActivitySample.NOT_MEASURED);
                    endOutOfBoundsSample.setOtherTimestamp(sample.getTimestamp());
                    endOutOfBoundsSamples.add(endOutOfBoundsSample);
                }

                // Add pre-sample for better stats if needed
                if (lastHandledTimestamp < sample.getTimestamp() - 1) {
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
                            ActivitySample.NOT_MEASURED
                    );
                    preSample.setProvider(this);
                    // Handle modifiers
                    if (sleepModifier == 0)
                        processedSamples.add(preSample);
                }

                // Handle modifiers
                if (sleepModifier != 0)
                    sample.setRawKind(sleepModifier);
                if (insideStepMarker)
                    LOG.info("Start marker inside step marker.");

                // Add data
                processedSamples.add(sample);

                // Set modifiers
                if (sample.getSource() == FitnessData.MessageData.sleepId && (sample.getRawKind() == RawTypes.LIGHT_SLEEP || sample.getRawKind() == RawTypes.DEEP_SLEEP))
                    sleepModifier = sample.getRawKind();
                if (sample.getSource() == FitnessData.MessageData.stepId)
                    insideStepMarker = true;
            } else {
                // End mark

                // Handle if the start marker is before the start timestamp
                if (sample.getOtherTimestamp() < timestamp_from) {
                    // This is only really useful for sleep data
                    if (sample.getSource() == FitnessData.MessageData.sleepId && (sample.getRawKind() == RawTypes.LIGHT_SLEEP || sample.getRawKind() == RawTypes.DEEP_SLEEP)) {
                        // Change the type of the previous markers so the sleep registers correctly
                        for (HuaweiActivitySample processedSample : processedSamples)
                            processedSample.setRawKind(sample.getRawKind());

                        // Place a replacement start marker, may technically be out of timeframe
                        HuaweiActivitySample preSample = copySample(sample);
                        if (processedSamples.size() > 0)
                            preSample.setTimestamp(Math.min(timestamp_from, processedSamples.get(0).getTimestamp() - 1));
                        else
                            preSample.setTimestamp(timestamp_from);
                        preSample.setSteps(ActivitySample.NOT_MEASURED);
                        preSample.setCalories(ActivitySample.NOT_MEASURED);
                        preSample.setDistance(ActivitySample.NOT_MEASURED);
                        preSample.setOtherTimestamp(sample.getTimestamp());
                        processedSamples.add(0, preSample);
                    } else {
                        // These end markers are not useful
                        // We may lose some steps here, but there is no way to know which count
                        // we should use for them anyway
                        continue;
                    }
                }

                // Set modifiers
                if (sample.getSource() == FitnessData.MessageData.sleepId && sample.getRawKind() == sleepModifier)
                    sleepModifier = 0;
                if (sample.getSource() == FitnessData.MessageData.stepId)
                    insideStepMarker = false;

                // These are not added yet as they may need to be moved
                postSample = new HuaweiActivitySample(
                        sample.getTimestamp() + 1,
                        sample.getDeviceId(),
                        sample.getUserId(),
                        0,
                        (byte) 0x00,
                        ActivitySample.NOT_MEASURED,
                        0,
                        ActivitySample.NOT_MEASURED,
                        ActivitySample.NOT_MEASURED,
                        ActivitySample.NOT_MEASURED
                );
                postSample.setProvider(this);

                // Handle modifiers
                if (sleepModifier != 0) {
                    postSample = null;
                    sample.setRawKind(sleepModifier);
                }
                if (insideStepMarker)
                    LOG.info("End marker inside step marker.");

                endSample = sample;
            }

            lastHandledTimestamp = sample.getTimestamp();
        }

        if (endSample != null)
            processedSamples.add(endSample);

        if (postSample != null)
            processedSamples.add(postSample);

        for (HuaweiActivitySample sample : endOutOfBoundsSamples) {
            sample.setTimestamp(Math.max(timestamp_to, processedSamples.get(processedSamples.size() - 1).getTimestamp() + 1));
            processedSamples.add(sample);
        }

        int lastTime = 0;
        for (HuaweiActivitySample sample : processedSamples) {
            if (sample.getTimestamp() < lastTime)
                LOG.error("Out of order timestamp!");
            lastTime = sample.getTimestamp();
        }

        LOG.info("Raw samples: " + rawSamples.size() + ", Processed samples: " + processedSamples.size());

        return processedSamples;
    }
}
