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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig.SetDateFormat;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig.TimeFormat;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig.DateFormat;

public class GetSetDateFormatRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSetDateFormatRequest.class);

    public GetSetDateFormatRequest(HuaweiSupport support, TransactionBuilder builder) {
        super(support, builder);
        this.serviceId = DeviceConfig.id;
        this.commandId = SetDateFormat.id;
    }

    @Override
    protected byte[] createRequest() {
        int time;
        int date;
        GBPrefs gbPrefs = new GBPrefs(new Prefs(GBApplication.getDeviceSpecificSharedPrefs(support.getDevice().getAddress())));
        String timeFormat = gbPrefs.getTimeFormat();
        if (timeFormat.equals("24h")) {
        time = TimeFormat.HOURS24.format;
        } else {
                    time = TimeFormat.HOURS12.format;
            }
            String dateFormat = GBApplication.getDeviceSpecificSharedPrefs(support.getDevice().getAddress()).getString("dateFormat", "MM/dd/yyyy");
        switch (dateFormat) {
                case "MM/dd/yyyy":
                    date = DateFormat.MONTHFIRST.format;
            break;
                case "dd.MM.yyyy":
                case "dd/MM/yyyy":
                    date = DateFormat.DAYFIRST.format;
                    break;
            default:
                date = DateFormat.YEARFIRST.format;
        }
        HuaweiTLV dateFormatTLVs = new HuaweiTLV()
            .put(SetDateFormat.DateFormat, (byte)date)
            .put(SetDateFormat.TimeFormat, (byte)time);
        requestedPacket = new HuaweiPacket(
            serviceId,
            commandId,
            new HuaweiTLV()
                .put(SetDateFormat.SetDateFormat, dateFormatTLVs)
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request Set Date Format: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Date Format");
    }
}