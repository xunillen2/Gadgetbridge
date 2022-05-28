package nodomain.freeyourgadget.gadgetbridge.database.schema;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.database.DBUpdateScript;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;

public class GadgetbridgeUpdate_40 implements DBUpdateScript {

    // TODO: This class is for the Huawei development version of Gadgetbridge,
    //       and should never be present in the main branch of Gadgetbridge

    @Override
    public void upgradeSchema(SQLiteDatabase db) {
        if (!DBHelper.existsColumn(HuaweiActivitySampleDao.TABLENAME, "SPO", db)) {
            String MOVE_DATA_TO_TEMP_TABLE = "ALTER TABLE HUAWEI_ACTIVITY_SAMPLE RENAME TO HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(MOVE_DATA_TO_TEMP_TABLE);

            HuaweiActivitySampleDao.createTable(db, false);

            String MIGRATE_DATA = "insert into HUAWEI_ACTIVITY_SAMPLE select *, -1 from HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(MIGRATE_DATA);

            String DROP_TEMP_TABLE = "drop table if exists HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(DROP_TEMP_TABLE);
        }
    }

    @Override
    public void downgradeSchema(SQLiteDatabase db) {
        if (DBHelper.existsColumn(HuaweiActivitySampleDao.TABLENAME, "SPO", db)) {
            String MOVE_DATA_TO_TEMP_TABLE = "ALTER TABLE HUAWEI_ACTIVITY_SAMPLE RENAME TO HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(MOVE_DATA_TO_TEMP_TABLE);

            db.execSQL("CREATE TABLE IF NOT EXISTS \"HUAWEI_ACTIVITY_SAMPLE\" (" + //
                    "\"TIMESTAMP\" INTEGER  NOT NULL ," + // 0: timestamp
                    "\"DEVICE_ID\" INTEGER  NOT NULL ," + // 1: deviceId
                    "\"USER_ID\" INTEGER NOT NULL ," + // 2: userId
                    "\"OTHER_TIMESTAMP\" INTEGER  NOT NULL ," + // 3: otherTimestamp
                    "\"SOURCE\" INTEGER  NOT NULL ," + // 4: source
                    "\"RAW_KIND\" INTEGER NOT NULL ," + // 5: rawKind
                    "\"RAW_INTENSITY\" INTEGER NOT NULL ," + // 6: rawIntensity
                    "\"STEPS\" INTEGER NOT NULL ," + // 7: steps
                    "\"CALORIES\" INTEGER NOT NULL ," + // 8: calories
                    "\"DISTANCE\" INTEGER NOT NULL ," + // 9: distance
                    "PRIMARY KEY (" +
                    "\"TIMESTAMP\" ," +
                    "\"DEVICE_ID\" ," +
                    "\"OTHER_TIMESTAMP\" ," +
                    "\"SOURCE\" ) ON CONFLICT REPLACE)" + ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? " WITHOUT ROWID;" : ";")
            );

            String MIGRATE_DATA = "insert into HUAWEI_ACTIVITY_SAMPLE select TIMESTAMP, DEVICE_ID, USER_ID, OTHER_TIMESTAMP, SOURCE, RAW_KIND, RAW_INTENSITY, STEPS, CALORIES, DISTANCE from HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(MIGRATE_DATA);

            String DROP_TEMP_TABLE = "DROP TABLE IF EXISTS HUAWEI_ACTIVITY_SAMPLE_TEMP";
            db.execSQL(DROP_TEMP_TABLE);
        }
    }
}
