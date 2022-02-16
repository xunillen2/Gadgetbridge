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

public class WorkMode {
    public static final int id = 0x26;

    public static class ModeStatus {
        public static final int id = 0x01;
        public static final int autoDetectMode  = 0x01;
        public static final int footWear  = 0x02;
    }

    public static class SwitchStatus {
        public static final int id = 0x02;
        public static final int setStatus  = 0x01;
    }

    /*public static class FootWear {
        public static final int id = 0x03;
        public static final int AutoDetectMode  = 0x01;
        public static final int FootWear  = 0x02;
    }*/

    public WorkMode() {
        super();
    }
}
