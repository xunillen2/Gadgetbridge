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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import android.content.SharedPreferences;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.devices.zetime.ZeTimeConstants;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.FitnessData.ActivityReminder;

public class SetActivityReminderRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetActivityReminderRequest.class);

    public SetActivityReminderRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = ActivityReminder.id;
    }

    @Override
    protected byte[] createRequest() {
        SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(this.support.getDevice().getAddress());

        boolean longsitSwitch = sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_LONGSIT_SWITCH, false);
        String longsitInterval = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_LONGSIT_PERIOD, "60");
        String longsitStart = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_LONGSIT_START, "08:00");
        String longsitEnd = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_LONGSIT_END, "23:00");
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("HH:mm");
        try {
            startCalendar.setTime(df.parse(longsitStart));
            endCalendar.setTime(df.parse(longsitEnd));
        } catch (ParseException e) {
            LOG.debug("settings error: " + e);
        }
        byte[] start = new byte[]{
            (byte)startCalendar.get(Calendar.HOUR_OF_DAY),
            (byte)startCalendar.get(Calendar.MINUTE)};
        byte[] end = new byte[]{
            (byte)endCalendar.get(Calendar.HOUR_OF_DAY),
            (byte)endCalendar.get(Calendar.MINUTE)};
        int cycle = (1 << 7); // set inactivity active: set bit 7
        cycle |= (sharedPrefs.getBoolean(ZeTimeConstants.PREF_INACTIVITY_MO, false) ? 1 : 0);
        cycle |= ((sharedPrefs.getBoolean(ZeTimeConstants.PREF_INACTIVITY_TU, false) ? 1 : 0) << 1);
        cycle |= ((sharedPrefs.getBoolean(ZeTimeConstants.PREF_INACTIVITY_WE, false) ? 1 : 0) << 2);
        cycle |= ((sharedPrefs.getBoolean(ZeTimeConstants.PREF_INACTIVITY_TH, false) ? 1 : 0) << 3);
        cycle |= ((sharedPrefs.getBoolean(ZeTimeConstants.PREF_INACTIVITY_FR, false) ? 1 : 0) << 4);
        cycle |= ((sharedPrefs.getBoolean(ZeTimeConstants.PREF_INACTIVITY_SA, false) ? 1 : 0) << 5);
        cycle |= ((sharedPrefs.getBoolean(ZeTimeConstants.PREF_INACTIVITY_SU, false) ? 1 : 0) << 6);

        HuaweiTLV container = new HuaweiTLV();
        container.put(ActivityReminder.longsitSwitch, longsitSwitch);
        container.put(ActivityReminder.longsitInterval, (byte)Integer.parseInt(longsitInterval));
        container.put(ActivityReminder.longsitStart, start);
        container.put(ActivityReminder.longsitEnd, end);
        container.put(ActivityReminder.longsitCycle, (byte)cycle);

        requestedPacket = new HuaweiPacket(
            serviceId,
            commandId,
            new HuaweiTLV()
                .put(ActivityReminder.container, container)
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request Set Activity Reminder: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Activity Reminder");
    }
}