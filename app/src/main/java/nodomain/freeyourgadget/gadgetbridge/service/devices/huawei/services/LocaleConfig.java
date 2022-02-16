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

public class LocaleConfig {
    public static final int id = 0x0C;

    public static class SetLocale {
        public static final int id = 0x01;
        public static final int LanguageTag  = 0x01;
        public static final int MeasurementSystem  = 0x02;
    }

    public LocaleConfig() {
        super();
    }

    public static class MeasurementSystem {
        public static final int metric = 0x00;
        public static final int imperial = 0x01;
    }
}
