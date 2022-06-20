package nodomain.freeyourgadget.gadgetbridge.database.schema;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import org.apache.commons.lang3.NotImplementedException;

import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.database.DBUpdateScript;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySampleDao;

public class GadgetbridgeUpdate_44 implements DBUpdateScript {

    // TODO: This class is for the Huawei development version of Gadgetbridge,
    //       and should never be present in the main branch of Gadgetbridge

    @Override
    public void upgradeSchema(SQLiteDatabase db) {
        if (!DBHelper.existsColumn(HuaweiWorkoutDataSampleDao.TABLENAME, "STEP_LENGTH", db)) {
            String MOVE_DATA_TO_TEMP_TABLE = "ALTER TABLE HUAWEI_WORKOUT_DATA_SAMPLE RENAME TO HUAWEI_WORKOUT_DATA_SAMPLE_TEMP";
            db.execSQL(MOVE_DATA_TO_TEMP_TABLE);

            HuaweiWorkoutDataSampleDao.createTable(db, false);

            String MIGRATE_DATA = "INSERT INTO HUAWEI_WORKOUT_DATA_SAMPLE SELECT WORKOUT_ID, TIMESTAMP, SPEED, CADENCE, 0, GROUND_CONTACT_TIME, IMPACT, SWING_ANGLE, FORE_FOOT_LANDING, MID_FOOT_LANDING, BACK_FOOT_LANDING, EVERSION_ANGLE, DATA_ERROR_HEX FROM HUAWEI_WORKOUT_DATA_SAMPLE_TEMP";
            db.execSQL(MIGRATE_DATA);

            String DROP_TEMP_TABLE = "DROP TABLE IF EXISTS HUAWEI_WORKOUT_DATA_SAMPLE_TEMP";
            db.execSQL(DROP_TEMP_TABLE);
        }
    }

    @Override
    public void downgradeSchema(SQLiteDatabase db) {
        // If necessary, let me know
        throw new NotImplementedException("");
    }
}
