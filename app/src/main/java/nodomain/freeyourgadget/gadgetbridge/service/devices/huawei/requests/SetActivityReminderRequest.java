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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiUtil;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;
import nodomain.freeyourgadget.gadgetbridge.util.AlarmUtils;

public class SetActivityReminderRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetActivityReminderRequest.class);

    public SetActivityReminderRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = FitnessData.id;
        this.commandId = FitnessData.ActivityReminder.id;
    }

    @Override
    protected byte[] createRequest() {
        SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(support.deviceMac);

        boolean longsitSwitch = sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_ENABLE, false);
        String longsitInterval = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_INACTIVITY_THRESHOLD, "60");
        String longsitStart = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_INACTIVITY_START, "06:00");
        String longsitEnd = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_INACTIVITY_END, "23:00");
        byte[] start = HuaweiUtil.timeToByte(longsitStart);
        byte[] end = HuaweiUtil.timeToByte(longsitEnd);
        int cycle = AlarmUtils.createRepetitionMask(
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_MO, false),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_TU, false),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_WE, false),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_TH, false),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_FR, false),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_SA, false),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_INACTIVITY_SU, false)
        );

        return new FitnessData.ActivityReminder.Request(
                support.secretsProvider,
                longsitSwitch,
                (byte) Integer.parseInt(longsitInterval),
                start,
                end,
                (byte) cycle
        ).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Activity Reminder");
    }
}