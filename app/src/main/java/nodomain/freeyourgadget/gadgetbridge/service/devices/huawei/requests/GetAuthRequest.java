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
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class GetAuthRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetAuthRequest.class);

    protected final byte[] clientNonce;
    protected byte[] serverNonce = new byte[16];
    protected byte[] authVersion = new byte[2];

    public GetAuthRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.Auth.id;
        this.clientNonce = HuaweiCrypto.generateNonce();
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        System.arraycopy(pastRequest.getValueReturned(), 2, serverNonce, 0, 16);
        System.arraycopy(pastRequest.getValueReturned(), 0, authVersion, 0, 2);
        huaweiCrypto = new HuaweiCrypto(authVersion);
        try {
            byte[] challenge = huaweiCrypto.digestChallenge(clientNonce, serverNonce);
            byte[] nonce = ByteBuffer.allocate(18)
                                            .put(authVersion)
                                            .put(clientNonce)
                                            .array();
            return new DeviceConfig.Auth.Request(support.paramsProvider, challenge, nonce).serialize();
        } catch (NoSuchAlgorithmException | InvalidKeyException | HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Auth");

        if (!(receivedPacket instanceof DeviceConfig.Auth.Response)) {
            // TODO: exception
            return;
        }

        try {
            byte[] expectedAnswer = huaweiCrypto.digestResponse(clientNonce, serverNonce);
            byte[] actualAnswer = ((DeviceConfig.Auth.Response) receivedPacket).challengeResponse;
            if (!Arrays.equals(expectedAnswer, actualAnswer)) {
                throw new GBException("Challenge answer mismatch : "
                        + StringUtils.bytesToHex(actualAnswer)
                        + " != "
                        + StringUtils.bytesToHex(expectedAnswer)
                );
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            throw new GBException("Challenge response digest exception");
        }
    }
}
