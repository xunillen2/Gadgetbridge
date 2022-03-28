/*  Copyright (C) 2022 Gaignon Damien

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
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;

public class GetSupportedServicesRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSupportedServicesRequest.class);

    private byte MAX_SERVICES = 0x35;
    private byte[] allSupportedServices;
    protected byte[] activatedServices;

    public GetSupportedServicesRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.SupportedServices.id;
        this.allSupportedServices = createServices();
    }

    @Override
    protected byte[] createRequest() {
        return new DeviceConfig.SupportedServices.Request(support.secretsProvider, this.allSupportedServices).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Supported Services");

        if (!(receivedPacket instanceof DeviceConfig.SupportedServices.Response)) {
            // TODO: exception
            return;
        }

        byte[] supportedServices = ((DeviceConfig.SupportedServices.Response) receivedPacket).supportedServices;
        byte[] activatedServicesTmp = new byte[MAX_SERVICES];
        int j = 0;
        for (int i = 0; i < MAX_SERVICES; i++) {
            if (supportedServices[i] == 1) {
                activatedServicesTmp[j] = (byte)allSupportedServices[i];
                j++;
            }
        }
        activatedServices = new byte[j];
        System.arraycopy(activatedServicesTmp, 0, activatedServices, 0, j);
    }

    @Override
    public byte[] getValueReturned() {
        return activatedServices;
    }

    private byte[] createServices() {
        byte[] services = new byte[MAX_SERVICES];
        for (byte i = 0; i < MAX_SERVICES; i++) {
            services[i] = (byte)(i + 1);
        }
        return services;
    }
}
