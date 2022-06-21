package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
            BaseActivitySummary previous = null;
            if (!duplicates.isEmpty())
                previous = duplicates.get(0);

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

            JSONObject status = new JSONObject();
            status.put("value", summary.getStatus() & 0xFF);
            status.put("unit", "");
            jsonObject.put("Status", status);

            JSONObject typeJson = new JSONObject();
            typeJson.put("value", summary.getType() & 0xFF);
            typeJson.put("unit", "");
            jsonObject.put("Type", typeJson);

            boolean unknownData = false;
            if (dataSamples.size() != 0) {
                int cadence = 0;
                int stepLength = 0;
                int groundContactTime = 0;
                int impact = 0;
                int maxImpact = 0;
                int swingAngle = 0;
                int foreFootLanding = 0;
                int midFootLanding = 0;
                int backFootLanding = 0;
                int eversionAngle = 0;
                int maxEversionAngle = 0;
                for (HuaweiWorkoutDataSample dataSample : dataSamples) {
                    cadence += dataSample.getCadence();
                    stepLength += dataSample.getStepLength();
                    groundContactTime += dataSample.getGroundContactTime();
                    impact += dataSample.getImpact();
                    if (dataSample.getImpact() > maxImpact)
                        maxImpact = dataSample.getImpact();
                    swingAngle += dataSample.getSwingAngle();
                    foreFootLanding += dataSample.getForeFootLanding();
                    midFootLanding += dataSample.getMidFootLanding();
                    backFootLanding += dataSample.getBackFootLanding();
                    eversionAngle += dataSample.getEversionAngle();
                    if (dataSample.getEversionAngle() > maxEversionAngle)
                        maxEversionAngle = dataSample.getEversionAngle();
                    if (dataSample.getDataErrorHex() != null)
                        unknownData = true;
                }
                // Average the things that should probably be averaged
                cadence = cadence / dataSamples.size();
                stepLength = stepLength / dataSamples.size();
                groundContactTime = groundContactTime / dataSamples.size();
                impact = impact / dataSamples.size();
                swingAngle = swingAngle / dataSamples.size();
                eversionAngle = eversionAngle / dataSamples.size();

                JSONObject cadenceJson = new JSONObject();
                cadenceJson.put("value", cadence);
                cadenceJson.put("unit", "steps/min");
                jsonObject.put("Cadence (avg)", cadenceJson);

                JSONObject stepLengthJson = new JSONObject();
                stepLengthJson.put("value", stepLength);
                stepLengthJson.put("unit", "cm");
                jsonObject.put("Step Length (avg)", stepLengthJson);

                JSONObject groundContactTimeJson = new JSONObject();
                groundContactTimeJson.put("value", groundContactTime);
                groundContactTimeJson.put("unit", "milliseconds");
                jsonObject.put("Ground contact time (avg)", groundContactTimeJson);

                JSONObject impactJson = new JSONObject();
                impactJson.put("value", impact);
                impactJson.put("unit", "g");
                jsonObject.put("Impact (avg)", impactJson);

                JSONObject maxImpactJson = new JSONObject();
                maxImpactJson.put("value", maxImpact);
                maxImpactJson.put("unit", "g");
                jsonObject.put("Impact (max)", maxImpactJson);

                JSONObject swingAngleJson = new JSONObject();
                swingAngleJson.put("value", swingAngle);
                swingAngleJson.put("unit", "degrees");
                jsonObject.put("Swing angle (avg)", swingAngleJson);

                JSONObject foreFootLandingJson = new JSONObject();
                foreFootLandingJson.put("value", foreFootLanding);
                foreFootLandingJson.put("unit", "");
                jsonObject.put("Fore foot landings", foreFootLandingJson);

                JSONObject midFootLandingJson = new JSONObject();
                midFootLandingJson.put("value", midFootLanding);
                midFootLandingJson.put("unit", "");
                jsonObject.put("Mid foot landings", midFootLandingJson);

                JSONObject backFootLandingJson = new JSONObject();
                backFootLandingJson.put("value", backFootLanding);
                backFootLandingJson.put("unit", "");
                jsonObject.put("Back foot landings", backFootLandingJson);

                JSONObject eversionAngleJson = new JSONObject();
                eversionAngleJson.put("value", eversionAngle);
                eversionAngleJson.put("unit", "degrees");
                jsonObject.put("Eversion angle (avg)", eversionAngleJson);

                JSONObject maxEversionAngleJson = new JSONObject();
                maxEversionAngleJson.put("value", maxEversionAngle);
                maxEversionAngleJson.put("unit", "degrees");
                jsonObject.put("Eversion angle (max)", maxEversionAngleJson);
            }

            ListIterator<HuaweiWorkoutPaceSample> it = qbPace.build().listIterator();
            int count = 0;
            int pace = 0;
            while (it.hasNext()) {
                int index = it.nextIndex();
                HuaweiWorkoutPaceSample sample = it.next();

                count += 1;
                pace += sample.getPace();

                JSONObject paceDistance = new JSONObject();
                paceDistance.put("value", sample.getDistance());
                paceDistance.put("unit", "kilometers");
                jsonObject.put(String.format("Pace %d distance", index), paceDistance);

                JSONObject paceType = new JSONObject();
                paceType.put("value", sample.getType());
                paceType.put("unit", ""); // TODO: not sure
                jsonObject.put(String.format("Pace %d type", index), paceType);

                JSONObject pacePace = new JSONObject();
                pacePace.put("value", sample.getPace());
                pacePace.put("unit", "seconds_km");
                jsonObject.put(String.format("Pace %d pace", index), pacePace);

                if (sample.getCorrection() != 0) {
                    JSONObject paceCorrection = new JSONObject();
                    paceCorrection.put("value", sample.getCorrection());
                    paceCorrection.put("unit", "m");
                    jsonObject.put(String.format("Pace %d correction", index), paceCorrection);
                }
            }

            if (count != 0) {
                JSONObject avgPace = new JSONObject();
                avgPace.put("value", pace / count);
                avgPace.put("unit", "seconds_km");
                jsonObject.put("Average pace", avgPace);
            }

            if (unknownData) {
                JSONObject unknownDataJson = new JSONObject();
                unknownDataJson.put("value", "YES");
                unknownDataJson.put("unit", "string");

                // TODO: make this a translatable string
                jsonObject.put("Unknown data encountered", unknownDataJson);
            }

            BaseActivitySummary baseSummary;
            if (previous == null) {
                baseSummary = new BaseActivitySummary(
                        null,
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
            } else {
                baseSummary = new BaseActivitySummary(
                        previous.getId(),
                        previous.getName(),
                        start,
                        end,
                        type,
                        previous.getBaseLongitude(),
                        previous.getBaseLatitude(),
                        previous.getBaseAltitude(),
                        previous.getGpxTrack(),
                        deviceId,
                        userId,
                        jsonObject.toString(),
                        null
                );
            }
            db.getDaoSession().getBaseActivitySummaryDao().insertOrReplace(baseSummary);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
