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

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.LocaleConfig;

public class SetLocaleRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetLocaleRequest.class);

    public SetLocaleRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = LocaleConfig.id;
        this.commandId = LocaleConfig.SetLocaleRequest.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        String localeString = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getString(DeviceSettingsPreferenceConst.PREF_LANGUAGE, "auto");
        if (localeString == null || localeString.equals("auto")) {
            String language = Locale.getDefault().getLanguage();
            String country = Locale.getDefault().getCountry();
            if (country.equals("")) {
                country = language;
            }
            localeString = language + "-" + country.toUpperCase();
        } else {
            localeString = localeString.replace("_", "-");
        }
        LOG.debug("localeString: " + localeString);
        String measurementString = GBApplication
            .getPrefs()
            .getString(SettingsActivity.PREF_MEASUREMENT_SYSTEM, getContext().getString(R.string.p_unit_metric));
        LOG.debug("measurementString: " + measurementString);
        byte measurement = measurementString.equals("metric") ? LocaleConfig.MeasurementSystem.metric : LocaleConfig.MeasurementSystem.imperial;
        try {
            return new LocaleConfig.SetLocaleRequest(support.secretsProvider, localeString.getBytes(StandardCharsets.UTF_8), measurement).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Locale");
    }
}
