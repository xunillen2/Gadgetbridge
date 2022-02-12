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
package nodomain.freeyourgadget.gadgetbridge.devices.huawei.huaweibandaw70;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;

public class HuaweiBandAw70Coordinator extends HuaweiCoordinator{
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiBandAw70Coordinator.class);

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.HUAWEIBANDAW70;
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        try {
            BluetoothDevice device = candidate.getDevice();
            String name = device.getName();
            if (name != null && (name.toLowerCase().startsWith(HuaweiConstants.HU_BAND3E_NAME) || name.toLowerCase().startsWith(HuaweiConstants.HU_BAND4E_NAME))) {
                return getDeviceType();
            }
        } catch (Exception ex) {
            LOG.error("unable to check device support", ex);
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        int[] mainCoordinatorSettings = super.getSupportedDeviceSpecificSettings(device);
        int[] coordinatorSettings = new int[]{
                R.xml.devicesettings_huawei_band_aw70,
        };
        return concatSettings(mainCoordinatorSettings, coordinatorSettings);
    }

}
