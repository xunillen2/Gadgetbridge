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

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.WorkMode.SwitchStatus;

public class SetWorkModeRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetWorkModeRequest.class);

    public SetWorkModeRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = WorkMode.id;
        this.commandId = SwitchStatus.id;
    }

    @Override
    protected byte[] createRequest() {
        String workModeString = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getString(HuaweiConstants.PREF_HUAWEI_WORKMODE, "auto");
        boolean workMode = workModeString.equals("auto") ? true : false;
        requestedPacket = new HuaweiPacket(
            serviceId,
            commandId,
            new HuaweiTLV()
                .put(SwitchStatus.setStatus, workMode)
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