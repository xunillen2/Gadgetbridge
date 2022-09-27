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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket.CryptoException;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class GetPincodeRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetPincodeRequest.class);

    protected byte[] authVersion = new byte[2];
    private byte[] pincode = null;

    public GetPincodeRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.PinCode.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            return new DeviceConfig.PinCode.Request(support.paramsProvider).serialize();
        } catch (CryptoException e) {
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Pincode");
        System.arraycopy(pastRequest.getValueReturned(), 0, authVersion, 0, 2);

        if (!(receivedPacket instanceof DeviceConfig.PinCode.Response)) {
            // TODO: exception
            return;
        }

        byte[] message = ((DeviceConfig.PinCode.Response) receivedPacket).message;
        byte[] iv = ((DeviceConfig.PinCode.Response) receivedPacket).iv;

        HuaweiCrypto huaweiCrypto = new HuaweiCrypto(authVersion);
        pincode = huaweiCrypto.decryptPinCode(message, iv);
    }

    @Override
    public byte[] getValueReturned() {
        return pincode;
    }
}
