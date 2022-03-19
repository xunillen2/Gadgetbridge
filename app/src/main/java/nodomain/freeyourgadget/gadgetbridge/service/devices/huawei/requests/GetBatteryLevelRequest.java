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

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;

public class GetBatteryLevelRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetBatteryLevelRequest.class);

    public GetBatteryLevelRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.BatteryLevel.id;
    }

    @Override
    protected byte[] createRequest() {
        return new DeviceConfig.BatteryLevel.Request(support.secretsProvider).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Battery Level");

        if (!(receivedPacket instanceof DeviceConfig.BatteryLevel.Response)) {
            // TODO: exception
            return;
        }

        byte batteryLevel = ((DeviceConfig.BatteryLevel.Response) receivedPacket).level;
        getDevice().setBatteryLevel(batteryLevel);

        GBDeviceEventBatteryInfo batteryInfo = new GBDeviceEventBatteryInfo();
        batteryInfo.level = (int)batteryLevel & 0xff;
        getSupport().evaluateGBDeviceEvent(batteryInfo);
    }
}
