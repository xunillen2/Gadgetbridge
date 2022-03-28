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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;

public class SetWearLocationRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetWearLocationRequest.class);

    public SetWearLocationRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.WearLocationRequest.id;
    }

    @Override
    protected byte[] createRequest() {
        String locationString = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getString(DeviceSettingsPreferenceConst.PREF_WEARLOCATION, "left");
        byte location = (byte) (locationString.equals("left") ? 1 : 0);
        return new DeviceConfig.WearLocationRequest(support.secretsProvider, location).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Wear Location");
    }
}
