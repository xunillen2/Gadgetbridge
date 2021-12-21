/*  Copyright (C) 2021 Gaignon Damien

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alarms {
    public static final int id = 8;

    public static class EventAlarms {
        public static final int id = 1;
        public static final int index = 3; // byte
        public static final int setStatus = 4; // byte
        public static final int setStartTime = 5; //short
        public static final int repeat = 6; // byte
        public static final int alarmName = 7; // byte

        public static final int start = 129; // 0x81
        public static final int separator = 130; // 0x82
    }

    public static class SmartAlarms {
        public static final int id = 2;
        public static final int index = 3; // byte
        public static final int setStatus = 4; // byte
        public static final int setStartTime = 5; //short
        public static final int repeat = 6; // byte
        public static final int aheadTime = 7; // byte default value 5 min

        public static final int start = 129;
        public static final int separator = 130;
    }

    // getDeviceEventAlarm
    public static class EventAlarmsList {
        public static final int id = 3; // byte default value 0
        public static final int get = 1; // byte
    }

    // getDeviceSmartAlarm
    public static class SmartAlarmsList {
        public static final int id = 4; // byte default value 0
        public static final int get = 1; // byte
    }

    public Alarms() {
        super();
    }
}
