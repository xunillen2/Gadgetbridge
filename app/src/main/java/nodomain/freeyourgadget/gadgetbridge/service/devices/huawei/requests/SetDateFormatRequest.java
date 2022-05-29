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

import android.text.format.DateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;

public class SetDateFormatRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetDateFormatRequest.class);

    public SetDateFormatRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.SetDateFormatRequest.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        int time = DeviceConfig.Time.hours12;
        int date;
        String timeFormat = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getString(DeviceSettingsPreferenceConst.PREF_TIMEFORMAT, "auto");
        if (timeFormat.equals("auto")) {
            if (DateFormat.is24HourFormat(GBApplication.getContext()))
                time = DeviceConfig.Time.hours24;
        } else if (timeFormat.equals("24h")) {
            time = DeviceConfig.Time.hours24;
        }
        String dateFormat = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getString(DeviceSettingsPreferenceConst.PREF_DATEFORMAT, "MM/dd/yyyy");
        switch (dateFormat) {
            case "MM/dd/yyyy":
                date = DeviceConfig.Date.monthFirst;
                break;
            case "dd.MM.yyyy":
            case "dd/MM/yyyy":
                date = DeviceConfig.Date.dayFirst;
                break;
            default:
                date = DeviceConfig.Date.yearFirst;
        }
        try {
            return new DeviceConfig.SetDateFormatRequest(support.secretsProvider, (byte) date, (byte) time).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Date Format");
    }
}
