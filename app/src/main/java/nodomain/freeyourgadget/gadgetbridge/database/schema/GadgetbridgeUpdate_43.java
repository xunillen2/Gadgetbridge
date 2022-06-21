package nodomain.freeyourgadget.gadgetbridge.database.schema;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.database.DBUpdateScript;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySampleDao;

public class GadgetbridgeUpdate_43 implements DBUpdateScript {

    // TODO: This class is for the Huawei development version of Gadgetbridge,
    //       and should never be present in the main branch of Gadgetbridge

    @Override
    public void upgradeSchema(SQLiteDatabase db) {
        if (!DBHelper.existsColumn(HuaweiWorkoutSummarySampleDao.TABLENAME, "RAW_DATA", db)) {
            String MOVE_DATA_TO_TEMP_TABLE = "ALTER TABLE HUAWEI_WORKOUT_SUMMARY_SAMPLE RENAME TO HUAWEI_WORKOUT_SUMMARY_SAMPLE_TEMP";
            db.execSQL(MOVE_DATA_TO_TEMP_TABLE);

            HuaweiWorkoutSummarySampleDao.createTable(db, false);

            String MIGRATE_DATA = "INSERT INTO HUAWEI_WORKOUT_SUMMARY_SAMPLE SELECT *, '' FROM HUAWEI_WORKOUT_SUMMARY_SAMPLE_TEMP";
            db.execSQL(MIGRATE_DATA);

            String DROP_TEMP_TABLE = "DROP TABLE IF EXISTS HUAWEI_WORKOUT_SUMMARY_SAMPLE_TEMP";
            db.execSQL(DROP_TEMP_TABLE);
        }
    }

    @Override
    public void downgradeSchema(SQLiteDatabase db) {
        if (DBHelper.existsColumn(HuaweiWorkoutSummarySampleDao.TABLENAME, "RAW_DATA", db)) {
            String MOVE_DATA_TO_TEMP_TABLE = "ALTER TABLE HUAWEI_WORKOUT_SUMMARY_SAMPLE RENAME TO HUAWEI_WORKOUT_SUMMARY_SAMPLE_TEMP";
            db.execSQL(MOVE_DATA_TO_TEMP_TABLE);

            db.execSQL("CREATE TABLE \"HUAWEI_WORKOUT_SUMMARY_SAMPLE\" (" + //
                    "\"WORKOUT_ID\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: workoutId
                    "\"DEVICE_ID\" INTEGER NOT NULL ," + // 1: deviceId
                    "\"USER_ID\" INTEGER NOT NULL ," + // 2: userId
                    "\"WORKOUT_NUMBER\" INTEGER NOT NULL ," + // 3: workoutNumber
                    "\"STATUS\" INTEGER NOT NULL ," + // 4: status
                    "\"START_TIMESTAMP\" INTEGER NOT NULL ," + // 5: startTimestamp
                    "\"END_TIMESTAMP\" INTEGER NOT NULL ," + // 6: endTimestamp
                    "\"CALORIES\" INTEGER NOT NULL ," + // 7: calories
                    "\"DISTANCE\" INTEGER NOT NULL ," + // 8: distance
                    "\"STEP_COUNT\" INTEGER NOT NULL ," + // 9: stepCount
                    "\"TOTAL_TIME\" INTEGER NOT NULL ," + // 10: totalTime
                    "\"DURATION\" INTEGER NOT NULL ," + // 11: duration
                    "\"TYPE\" INTEGER NOT NULL);"); // 12: type

            String MIGRATE_DATA = "INSERT INTO HUAWEI_WORKOUT_SUMMARY_SAMPLE SELECT WORKOUT_ID, DEVICE_ID, USER_ID, WORKOUT_NUMBER, STATUS, START_TIMESTAMP, END_TIMESTAMP, CALORIES, DISTANCE, STEP_COUNT, TOTAL_TIME, DURATION, TYPE FROM HUAWEI_WORKOUT_SUMMARY_SAMPLE_TEMP";
            db.execSQL(MIGRATE_DATA);

            String DROP_TEMP_TABLE = "DROP TABLE IF EXISTS HUAWEI_WORKOUT_SUMMARY_SAMPLE_TEMP";
            db.execSQL(DROP_TEMP_TABLE);
        }
    }
}
