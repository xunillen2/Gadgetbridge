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

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig.LinkParams;

public class GetLinkParamsRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetLinkParamsRequest.class);

    protected final byte[] serverNonce;

    public GetLinkParamsRequest(HuaweiSupport support, TransactionBuilder builder) {
        super(support, builder);
        this.serviceId = DeviceConfig.id;
        this.commandId = LinkParams.id;
        this.serverNonce = new byte[18];
        isSelfQueue = false;
    }

    @Override
    protected byte[] createRequest(){
        requestedPacket = new HuaweiPacket(serviceId,
            commandId,
            new HuaweiTLV()
                .put(LinkParams.protocolVersion)
                .put(LinkParams.maxFrameSize)
                .put(LinkParams.maxLinkSize)
                .put(LinkParams.connectionInterval)
        );
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request LinkParams: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() {
        LOG.debug("handle LinkParams");
        System.arraycopy(receivedPacket.tlv.getBytes(LinkParams.serverNonce),
                            0, serverNonce,
                            0, 18
        );
    }

    @Override
    public byte[] getValueReturned() {
        return serverNonce;
    }
}