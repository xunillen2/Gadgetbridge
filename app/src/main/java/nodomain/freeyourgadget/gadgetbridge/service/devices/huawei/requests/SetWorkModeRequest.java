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
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.WorkMode;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.WorkMode.AutoDetectAndWorkMode;

public class SetWorkModeRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetWorkModeRequest.class);

    public SetWorkModeRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = WorkMode.id;
        this.commandId = AutoDetectAndWorkMode.id;
    }

    @Override
    protected byte[] createRequest() {
        String workModeString = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getString(HuaweiConstants.PREF_HUAWEI_WORKMODE, "auto");
        /*boolean workMode = workModeString.equals("auto") ? true : false;
        LOG.debug("workModeString: " + workModeString);
        LOG.debug("workMode: " + (workMode ? "true" : "false"));*/
        int auto = 0;
        int foot = 0;
        switch (workModeString) {
            case "0":
                auto = 0;
                foot = 0;
                break;
            case "1":
                auto = 0;
                foot = 1;
                break;
            case "2":
                auto = 0;
                foot = 2;
                break;
            case "3":
                auto = 0;
                foot = 3;
                break;
            case "4":
                auto = 1;
                foot = 0;
                break;
            case "5":
                auto = 1;
                foot = 1;
                break;
            case "6":
                auto = 1;
                foot = 2;
                break;
            case "7":
                auto = 1;
                foot = 3;
                break;
            case "8":
                auto = 2;
                foot = 0;
                break;
            case "9":
                auto = 2;
                foot = 1;
                break;
            case "10":
                auto = 2;
                foot = 2;
                break;
            case "11":
                auto = 2;
                foot = 3;
                break;
            case "12":
                auto = 3;
                foot = 0;
                break;
            case "13":
                auto = 3;
                foot = 1;
                break;
            case "14":
                auto = 3;
                foot = 2;
                break;
            case "15":
                auto = 3;
                foot = 3;
                break;
        }
        LOG.debug("workModeString: " + workModeString);
        requestedPacket = new HuaweiPacket(
            serviceId,
            commandId,
            new HuaweiTLV()
                // .put(AutoDetectAndWorkMode.AutoDetectMode, workMode)
                // .put(AutoDetectAndWorkMode.FootWear, workMode)
                .put(1, (byte)auto)
                .put(2, (byte)foot)
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request Set WorkMode: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set WorkMode");
    }
}