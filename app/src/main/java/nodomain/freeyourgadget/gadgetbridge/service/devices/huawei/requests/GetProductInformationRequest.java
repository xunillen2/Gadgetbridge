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
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig.ProductInfo;

public class GetProductInformationRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetProductInformationRequest.class);

    public GetProductInformationRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = ProductInfo.id;
    }

    @Override
    protected byte[] createRequest() {
        HuaweiTLV productInfoTLVs = new HuaweiTLV();
        for (int i = 0; i < 14; i++) {
            productInfoTLVs.put(i);
        }
        requestedPacket = new HuaweiPacket(
            serviceId,
            commandId,
            productInfoTLVs
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request Product Information: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Product Information");
        getDevice().setFirmwareVersion(receivedPacket.tlv.getString(ProductInfo.SoftwareVersion));
        getDevice().setFirmwareVersion2(receivedPacket.tlv.getString(ProductInfo.HardwareVersion));
        getDevice().setModel(receivedPacket.tlv.getString(ProductInfo.ProductModel));
    }
}