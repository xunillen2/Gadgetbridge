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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiLESupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;

public class SetTimeRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetTimeRequest.class);

    public SetTimeRequest(HuaweiLESupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.SetTimeRequest.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        // Thanks to https://stackoverflow.com/a/2453820
        Calendar now = Calendar.getInstance();
        int timestampSec = (int)(now.getTimeInMillis() / 1000);
        TimeZone timeZone = now.getTimeZone();
        int zoneRawOffset = timeZone.getRawOffset();
        if(timeZone.inDaylightTime(new Date())){
            zoneRawOffset = zoneRawOffset + timeZone.getDSTSavings();
        }
        int offsetHour = zoneRawOffset / 1000 / 60 / 60;
        if (offsetHour < 0) {offsetHour += 128;}
        int offsetMinutes = zoneRawOffset / 1000 / 60 % 60;

        short zoneOffset = ByteBuffer.allocate(2)
                .put((byte)offsetHour)
                .put((byte)offsetMinutes)
                .getShort(0);

        try {
            return new DeviceConfig.SetTimeRequest(support.secretsProvider, timestampSec, zoneOffset).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Time");
    }
}
