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
package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuaweiUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiUtil.class);

    public static byte[] timeToByte(String time) {
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("HH:mm");
        try {
            calendar.setTime(df.parse(time));
        } catch (ParseException e) {
            LOG.debug("settings error: " + e);
        }
        return new byte[]{
            (byte)calendar.get(Calendar.HOUR_OF_DAY),
            (byte)calendar.get(Calendar.MINUTE)};
        }
}
