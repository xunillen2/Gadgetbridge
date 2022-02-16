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
    public static final int id = 0x08;

    public static class EventAlarms {
        public static final int id = 0x01;
        public static final int index = 0x03; // byte
        public static final int setStatus = 0x04; // byte
        public static final int setStartTime = 0x05; //short
        public static final int repeat = 0x06; // byte
        public static final int alarmName = 0x07; // byte

        public static final int start = 0x81;
        public static final int separator = 0x82;
    }

    public static class SmartAlarms {
        public static final int id = 0x02;
        public static final int index = 0x03; // byte
        public static final int setStatus = 0x04; // byte
        public static final int setStartTime = 0x05; //short
        public static final int repeat = 0x06; // byte
        public static final int aheadTime = 0x07; // byte default value 5 min

        public static final int start = 0x81;
        public static final int separator = 0x82;
    }

    // getDeviceEventAlarm
    public static class EventAlarmsList {
        public static final int id = 0x03; // byte default value 0
        public static final int get = 0x01; // byte
    }

    // getDeviceSmartAlarm
    public static class SmartAlarmsList {
        public static final int id = 0x04; // byte default value 0
        public static final int get = 0x01; // byte
    }

    public Alarms() {
        super();
    }
}
