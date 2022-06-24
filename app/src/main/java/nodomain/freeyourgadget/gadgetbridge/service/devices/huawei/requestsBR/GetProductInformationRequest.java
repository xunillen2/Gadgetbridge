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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsBR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiBRSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;

public class GetProductInformationRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetProductInformationRequest.class);

    public GetProductInformationRequest(HuaweiBRSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.ProductInfo.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new DeviceConfig.ProductInfo.Request(support.secretsProvider).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Product Information");

        if (!(receivedPacket instanceof DeviceConfig.ProductInfo.Response)) {
            // TODO: exception
            return;
        }

        getDevice().setFirmwareVersion(((DeviceConfig.ProductInfo.Response) receivedPacket).softwareVersion);
        getDevice().setFirmwareVersion2(((DeviceConfig.ProductInfo.Response) receivedPacket).hardwareVersion);
        getDevice().setModel(((DeviceConfig.ProductInfo.Response) receivedPacket).productModel);
   }
}
