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
package nodomain.freeyourgadget.gadgetbridge.devices.huawei.honorband4;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class HonorBand4Coordinator extends HuaweiCoordinator{
    private static final Logger LOG = LoggerFactory.getLogger(HonorBand4Coordinator.class);

    public HonorBand4Coordinator() {
        super();
    }

    @Override
    public String getManufacturer() {
        return "Honor";
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.HONORBAND4;
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        try {
            BluetoothDevice device = candidate.getDevice();
            String name = device.getName();
            if (name != null && name.toLowerCase().startsWith(HuaweiConstants.HO_BAND4_NAME)) {
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
                R.xml.devicesettings_dateformat,
                R.xml.devicesettings_wearlocation,
                // R.xml.devicesettings_huawei_band3e,
        };
        return concatSettings(mainCoordinatorSettings, coordinatorSettings);
    }

}
