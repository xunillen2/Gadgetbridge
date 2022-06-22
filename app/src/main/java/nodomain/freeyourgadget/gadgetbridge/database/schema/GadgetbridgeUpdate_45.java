package nodomain.freeyourgadget.gadgetbridge.database.schema;

import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.database.DBUpdateScript;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySampleDao;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class GadgetbridgeUpdate_45 implements DBUpdateScript {
    private static final Logger LOG = LoggerFactory.getLogger(GadgetbridgeUpdate_45.class);

    // TODO: This class is for the Huawei development version of Gadgetbridge,
    //       and should never be present in the main branch of Gadgetbridge

    @Override
    public void upgradeSchema(SQLiteDatabase db) {
        if (!DBHelper.existsColumn(HuaweiActivitySampleDao.TABLENAME, "HEART_RATE", db)) {
            String MOVE_DATA_TO_TEMP_TABLE = "ALTER TABLE HUAWEI_ACTIVITY_SAMPLE RENAME TO HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(MOVE_DATA_TO_TEMP_TABLE);

            HuaweiActivitySampleDao.createTable(db, false);

            String MIGRATE_DATA = "INSERT INTO HUAWEI_ACTIVITY_SAMPLE SELECT *, -1 FROM HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(MIGRATE_DATA);

            String DROP_TEMP_TABLE = "DROP TABLE IF EXISTS HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(DROP_TEMP_TABLE);
        }

        if (!DBHelper.existsColumn(HuaweiWorkoutDataSampleDao.TABLENAME, "HEART_RATE", db)) {
            String MOVE_DATA_TO_TEMP_TABLE = "ALTER TABLE HUAWEI_WORKOUT_DATA_SAMPLE RENAME TO HUAWEI_WORKOUT_DATA_SAMPLE_TEMP";
            db.execSQL(MOVE_DATA_TO_TEMP_TABLE);

            HuaweiWorkoutDataSampleDao.createTable(db, false);

            String MIGRATE_DATA = "INSERT INTO HUAWEI_WORKOUT_DATA_SAMPLE SELECT WORKOUT_ID, TIMESTAMP, -1, SPEED, CADENCE, STEP_LENGTH, GROUND_CONTACT_TIME, IMPACT, SWING_ANGLE, FORE_FOOT_LANDING, MID_FOOT_LANDING, BACK_FOOT_LANDING, EVERSION_ANGLE, DATA_ERROR_HEX FROM HUAWEI_WORKOUT_DATA_SAMPLE_TEMP";
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
