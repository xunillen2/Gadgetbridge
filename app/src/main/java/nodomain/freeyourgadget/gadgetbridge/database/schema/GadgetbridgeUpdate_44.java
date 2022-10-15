/*  Copyright (C) 2022 José Rebelo

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.database.schema;

import android.database.sqlite.SQLiteDatabase;

import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.database.DBUpdateScript;
import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummaryDao;

import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySampleDao;

public class GadgetbridgeUpdate_44 implements DBUpdateScript {
    @Override
    public void upgradeSchema(final SQLiteDatabase db) {
        if (!DBHelper.existsColumn(BaseActivitySummaryDao.TABLENAME, BaseActivitySummaryDao.Properties.RawDetailsPath.columnName, db)) {
            final String statement = "ALTER TABLE " + BaseActivitySummaryDao.TABLENAME + " ADD COLUMN "
                    + BaseActivitySummaryDao.Properties.RawDetailsPath.columnName + " TEXT";
            db.execSQL(statement);
        }

    // TODO: This class is for the Huawei development version of Gadgetbridge,
    //       and should never be present in the main branch of Gadgetbridge

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
    public void downgradeSchema(final SQLiteDatabase db) {
    }
}
