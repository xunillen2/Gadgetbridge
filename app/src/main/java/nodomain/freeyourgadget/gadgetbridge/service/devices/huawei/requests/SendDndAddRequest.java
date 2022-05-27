/*  Copyright (C) 2022 Gaignon Damien

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
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiUtil;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.util.AlarmUtils;

public class SendDndAddRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SendDndAddRequest.class);

    public SendDndAddRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.DndAddRequest.id;
    }

    @Override
    protected byte[] createRequest() {
        SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(support.deviceMac);

        int dndPriority = sharedPrefs.getInt(HuaweiConstants.PREF_HUAWEI_DND_PRIORITY, 0x00); //Device priority - accept activation
        boolean statusLiftWrist = sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_LIFTWRIST_NOSHED, false); //Activate on wrist lift
        boolean statusDndLiftWrist = sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_LIFT_WRIST, false); //Activate on wrist lift with DND
        String dndSwitch = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB, "off");
        boolean dndEnable = (dndSwitch.equals("off") ? false : true);
        String startStr = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_START, "00:00");
        if (dndSwitch.equals("automatic")) startStr = "00:00";
        byte[] start = HuaweiUtil.timeToByte(startStr);
        String endStr = sharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_END, "23:59");
        if (dndSwitch.equals("automatic")) endStr = "23:59";
        byte[] end = HuaweiUtil.timeToByte(endStr);
        int cycle = AlarmUtils.createRepetitionMask(
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_MO, true),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_TU, true),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_WE, true),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_TH, true),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_FR, true),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_SA, true),
                sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_SU, true)
        );

        return new DeviceConfig.DndAddRequest(
            support.secretsProvider,
            dndEnable,
            start,
            end,
            cycle,
            dndPriority,
            statusLiftWrist,
            statusDndLiftWrist
        ).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle DND Add");
    }
}