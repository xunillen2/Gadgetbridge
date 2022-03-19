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

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;

public class SetTimeRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetTimeRequest.class);

    public SetTimeRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.SetTimeRequest.id;
    }

    @Override
    protected byte[] createRequest() {
        Calendar now = Calendar.getInstance();
        int timestampSec = (int)(now.getTimeInMillis() / 1000);
        float zoneOffsetMillis = now.get(Calendar.ZONE_OFFSET);
        float zoneOffsetHour = (zoneOffsetMillis / 1000 / 60 / 60);
        int offsetMinutes = (int)Math.abs(((zoneOffsetHour % 1) * 60.0));
        int offsetHour = zoneOffsetHour < 0 ? (int) Math.abs(zoneOffsetHour / 1.0) + 128 : (int) (zoneOffsetHour / 1.0);
        short zoneOffset = (short) (offsetHour << 8 + (byte) offsetMinutes);
        return new DeviceConfig.SetTimeRequest(support.secretsProvider, timestampSec, zoneOffset).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Time");
    }
}
