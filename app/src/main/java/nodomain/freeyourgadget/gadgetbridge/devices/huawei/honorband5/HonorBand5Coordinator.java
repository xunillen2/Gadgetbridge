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
package nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband5;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;

public class HonorBand5Coordinator extends HuaweiCoordinator{
    private static final Logger LOG = LoggerFactory.getLogger(HonorBand5Coordinator.class);

    @Override
    public String getManufacturer() {
        return "Honor";
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.HONORBAND5;
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        try {
            BluetoothDevice device = candidate.getDevice();
            String name = device.getName();
            if (name != null && name.toLowerCase().startsWith(HuaweiConstants.HO_BAND5_NAME)) {
                return getDeviceType();
            }
        } catch (Exception ex) {
            LOG.error("unable to check device support", ex);
        }
        return DeviceType.UNKNOWN;

    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return true;
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_donotdisturb_allday_liftwirst,
                R.xml.devicesettings_trusleep,
                R.xml.devicesettings_notifications_enable,
                R.xml.devicesettings_vibrations_enable,
                R.xml.devicesettings_inactivity_sheduled,
                R.xml.devicesettings_liftwrist_display_noshed,
                R.xml.devicesettings_rotatewrist_cycleinfo,
                R.xml.devicesettings_wearlocation,
                R.xml.devicesettings_huawei,
                R.xml.devicesettings_dateformat,
                R.xml.devicesettings_timeformat,
                R.xml.devicesettings_allow_accept_reject_calls,
                R.xml.devicesettings_huawei_reparse_workout_data,
                R.xml.devicesettings_find_phone,
                R.xml.devicesettings_disable_find_phone_with_dnd,
        };
    }

}