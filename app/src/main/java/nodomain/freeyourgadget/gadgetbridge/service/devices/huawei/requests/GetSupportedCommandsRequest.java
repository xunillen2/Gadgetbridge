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

/* In order to be compatible with all devices, request send all possible commands
to all possible services. This implies long packet which is not handled on the device.
Thus, this request could be sliced in 3 packets. But this command does not support slicing.
Thus, one need to send multiple requests and concat the response.
Packets should be 240 bytes max */
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import android.util.Pair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV.TLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.util.DeviceHelper;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig.SupportedCommands;

public class GetSupportedCommandsRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetSupportedCommandsRequest.class);

    protected ArrayList<HuaweiTLV> commandsListArray;

    public GetSupportedCommandsRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = SupportedCommands.id;
        this.commandsListArray = new ArrayList<HuaweiTLV>();
    }

    public GetSupportedCommandsRequest(HuaweiSupport support, ArrayList<HuaweiTLV> commandsListArray) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = SupportedCommands.id;
        this.commandsListArray = commandsListArray;
    }

    @Override
    protected byte[] createRequest() {
        if (commandsListArray.isEmpty()) {
            byte[] activatedServices = pastRequest.getValueReturned();
            byte[] commandsOfService;
            HuaweiTLV commandsList = new HuaweiTLV();
            for (int i = 0; i < activatedServices.length; i++) {
                commandsOfService = createCommands(activatedServices[i]);
                if ((commandsList.length() + commandsOfService.length + 2)> 208) {
                    commandsListArray.add(commandsList);
                    commandsList = new HuaweiTLV();
                }
                commandsList.put(SupportedCommands.serviceId, (byte) activatedServices[i])
                            .put(SupportedCommands.commands, commandsOfService);
            }
            commandsListArray.add(commandsList);
        }
        requestedPacket = new HuaweiPacket(
            serviceId,
            commandId,
            new HuaweiTLV()
                .put(SupportedCommands.supportedCommands, commandsListArray.remove(0))
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request Supported Commands: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Supported Commands");
        HuaweiTLV supportedCommands = receivedPacket.tlv.getObject(SupportedCommands.supportedCommands);
        if (!commandsListArray.isEmpty()) {
            GetSupportedCommandsRequest nextRequest = new GetSupportedCommandsRequest(this.support, this.commandsListArray);
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
            if (commandsListArray.size() == 1) {
                nextRequest.setFinalizeReq(new RequestCallback() {
                    @Override
                    public void call() {
                        HuaweiCoordinator coordinator = (HuaweiCoordinator) DeviceHelper.getInstance().getCoordinator(support.getDevice());
                        coordinator.printCommandsPerService();
                    }
                });
            }
        }

        HuaweiCoordinator coordinator = (HuaweiCoordinator) DeviceHelper.getInstance().getCoordinator(this.support.getDevice());
        TreeMap<Integer, byte[]> commandsPerService = coordinator.getCommandsPerService();
        Integer service_id = null;
        for(TLV tlv : supportedCommands.get()) {
            if ((int)tlv.getTag() == SupportedCommands.serviceId) {
                service_id = (int)ByteBuffer.wrap(tlv.getValue()).get();
            } else if (service_id != null) {
                ByteBuffer buffer = ByteBuffer.allocate(tlv.getValue().length);
                for (int i = 0; i < tlv.getValue().length; i++) {
                    if ((int)tlv.getValue()[i] == 1) buffer.put((byte)(i + 1));
                    
                }
                byte[] commands = new byte[buffer.position()];
                buffer.rewind();
                buffer.get(commands);
                commandsPerService.put(service_id, commands);
                service_id = null;
            }
        }

    }

    private byte[] createCommands(int service) {
        // We have counted commands per service
        // If it is possible to use a MAX_COMMANDS per service (0x40) it could be usefull
        // for futur updates - freeze a little on connection due to lots of requests
        //                                    01  02  03  04  05  06  07  08  09  0A
        int[] commandsPerService = new int[] {56, 16, 4,  2,  1,  1,  42, 5,  15, 10,
        //                                    0B  0C  0D  0E  0F  10  11  12  13  14
                                              3,  5,  1,  1,  12, 1,  1,  1,  1,  1,
        //                                    15  16  17  18  19  1A  1B  1C  1D  1E
                                              1,  7,  23, 9,  4,  10, 26, 6,  10, 1,
        //                                    1F  20  21  22  23  24  25  26  27  28
                                              1,  10, 2,  2,  17, 1,  14, 3,  14, 2,
        //                                    29  2A  2B  2C  2D  2E  2F  30  31  32
                                              1,  7,  1,  1,  1,  3,  1,  1,  1,  5,
        //                                    33  34  35
                                              4,  1,  4};
        byte[] commands = new byte[commandsPerService[service - 1]];
        for (int i = 0; i <= (commands.length - 1); i++) {
            commands[i] = (byte)(i + 1);
        }
        return commands;
    }
}