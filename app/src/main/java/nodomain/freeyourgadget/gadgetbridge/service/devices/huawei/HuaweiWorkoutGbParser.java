package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummary;
import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummaryDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutPaceSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutPaceSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySampleDao;

/**
 * This class parses the Huawei workouts into the table GB uses to show the workouts
 * It is a separate class so it can easily be used to re-parse the data without database migrations
 */
public class HuaweiWorkoutGbParser {

    public static void parseAllWorkouts() {
        try (DBHandler db = GBApplication.acquireDB()) {
            QueryBuilder<HuaweiWorkoutSummarySample> qb = db.getDaoSession().getHuaweiWorkoutSummarySampleDao().queryBuilder();
            for (HuaweiWorkoutSummarySample summary : qb.listLazy()) {
                parseWorkout(summary.getWorkoutId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int huaweiTypeToGbType(byte huaweiType) {
        // TODO: create a mapping
        return (int) huaweiType;
    }

    public static void parseWorkout(Long workoutId) {
        if (workoutId == null)
            return;

        try (DBHandler db = GBApplication.acquireDB()) {
            QueryBuilder qbSummary = db.getDaoSession().getHuaweiWorkoutSummarySampleDao().queryBuilder().where(
                    HuaweiWorkoutSummarySampleDao.Properties.WorkoutId.eq(workoutId)
            );
            List<HuaweiWorkoutSummarySample> summarySamples = qbSummary.build().list();
            if (summarySamples.size() != 1)
                return;
            HuaweiWorkoutSummarySample summary = summarySamples.get(0);

            QueryBuilder qbData = db.getDaoSession().getHuaweiWorkoutDataSampleDao().queryBuilder().where(
                    HuaweiWorkoutDataSampleDao.Properties.WorkoutId.eq(workoutId)
            );
            List<HuaweiWorkoutDataSample> dataSamples = qbData.build().list();

            QueryBuilder qbPace = db.getDaoSession().getHuaweiWorkoutPaceSampleDao().queryBuilder().where(
                    HuaweiWorkoutPaceSampleDao.Properties.WorkoutId.eq(workoutId)
            );
            List<HuaweiWorkoutPaceSample> paceSamples = qbPace.build().list();

            Long userId = summary.getUserId();
            Long deviceId = summary.getDeviceId();
            Date start = new Date(summary.getStartTimestamp() * 1000L);
            Date end = new Date(summary.getEndTimestamp() * 1000L);

            // Avoid duplicates
            QueryBuilder qb = db.getDaoSession().getBaseActivitySummaryDao().queryBuilder().where(
                    BaseActivitySummaryDao.Properties.UserId.eq(userId),
                    BaseActivitySummaryDao.Properties.DeviceId.eq(deviceId),
                    BaseActivitySummaryDao.Properties.StartTime.eq(start),
                    BaseActivitySummaryDao.Properties.EndTime.eq(end)
            );
            List<BaseActivitySummary> duplicates = qb.build().list();
            Long id = null;
            if (!duplicates.isEmpty())
                id = duplicates.get(0).getId();

            int type = huaweiTypeToGbType(summary.getType());

            JSONObject jsonObject = new JSONObject();

            JSONObject calories = new JSONObject();
            calories.put("value", summary.getCalories());
            calories.put("unit", "calories_unit");
            jsonObject.put("caloriesBurnt", calories);

            JSONObject distance = new JSONObject();
            distance.put("value", summary.getDistance());
            distance.put("unit", "meters");
            jsonObject.put("distanceMeters", distance);

            JSONObject steps = new JSONObject();
            steps.put("value", summary.getStepCount());
            steps.put("unit", "steps_unit");
            jsonObject.put("steps", steps);

            JSONObject time = new JSONObject();
            time.put("value", summary.getDuration());
            time.put("unit", "seconds");
            jsonObject.put("activeSeconds", time);

            BaseActivitySummary baseSummary = new BaseActivitySummary(
                    id,
                    "Workout " + summary.getWorkoutNumber(),
                    start,
                    end,
                    type,
                    null,
                    null,
                    null,
                    null,
                    deviceId,
                    userId,
                    jsonObject.toString(),
                    null
            );
            db.getDaoSession().getBaseActivitySummaryDao().insertOrReplace(baseSummary);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
