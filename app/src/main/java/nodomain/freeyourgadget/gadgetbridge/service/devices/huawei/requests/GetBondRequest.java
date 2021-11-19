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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig.Bond;

public class GetBondRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetBondRequest.class);

    protected String macAddress;

    public GetBondRequest(HuaweiSupport support, TransactionBuilder builder) {
        super(support, builder);
        this.serviceId = DeviceConfig.id;
        this.commandId = Bond.id;
    }

    @Override
    protected byte[] createRequest() {
        byte[] iv = support.getIV();
        byte[] authVersion = new byte[2];
        System.arraycopy(pastRequest.getValueReturned(), 0, authVersion, 0, 2);
        requestedPacket = new HuaweiPacket(serviceId,
            commandId,
            new HuaweiTLV()
                .put(Bond.BondRequest)
                .put(Bond.RequestCode, (byte)0x00)
                .put(Bond.ClientSerial, support.getSerial())
                .put(Bond.BondingKey, huaweiCrypto.createBondingKey(support.getDeviceMac(), support.getSecretKey(), iv))
                .put(Bond.InitVector, iv)
        );
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request Bond: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() {
        LOG.debug("handle Bond");
    }
}