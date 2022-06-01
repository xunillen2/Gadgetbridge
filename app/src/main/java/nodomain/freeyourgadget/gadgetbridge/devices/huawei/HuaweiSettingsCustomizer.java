/*  Copyright (C) 2021 José Rebelo

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

import android.content.SharedPreferences;
import android.os.Parcel;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsCustomizer;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsHandler;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.XTimePreference;

import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_LIFTWRIST_NOSHED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_START;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_END;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_LIFT_WRIST;

public class HuaweiSettingsCustomizer implements DeviceSpecificSettingsCustomizer {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiSettingsCustomizer.class);

    final GBDevice device;

    public HuaweiSettingsCustomizer(final GBDevice device) {
        this.device = device;
    }

    @Override
    public void onPreferenceChange(final Preference preference, final DeviceSpecificSettingsHandler handler) {
        if (preference.getKey().equals(PREF_DO_NOT_DISTURB)) {
            final String dndState = ((ListPreference) preference).getValue();
            final XTimePreference dndStart = (XTimePreference) handler.findPreference(PREF_DO_NOT_DISTURB_START);
            final XTimePreference dndEnd = (XTimePreference) handler.findPreference(PREF_DO_NOT_DISTURB_END);
            final SwitchPreference dndLifWrist = (SwitchPreference) handler.findPreference(PREF_DO_NOT_DISTURB_LIFT_WRIST);
            SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(device.getAddress());
            boolean statusLiftWrist = sharedPrefs.getBoolean(PREF_LIFTWRIST_NOSHED, false);

            dndStart.setEnabled(false);
            dndEnd.setEnabled(false);
            dndLifWrist.setEnabled(true);
            if (dndState.equals("scheduled")) {
                dndStart.setEnabled(true);
                dndEnd.setEnabled(true);
            }
            if (!statusLiftWrist || dndState.equals("off")) {
                dndLifWrist.setChecked(false);
                dndLifWrist.setEnabled(false);
            }
        }
    }

    @Override
    public void customizeSettings(final DeviceSpecificSettingsHandler handleri, Prefs prefs) {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(device, 0);
    }

    @Override
    public Set<String> getPreferenceKeysWithSummary() {
        return null;
    }
}
