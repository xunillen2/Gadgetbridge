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
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiBRSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms;

import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.EventAlarmsRequest;
import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.SmartAlarmRequest;
import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.EventAlarmsList;
import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.SmartAlarmsList;

public class AlarmsRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmsRequest.class);

    private EventAlarmsRequest eventAlarmsRequest = null;
    private SmartAlarmRequest smartAlarmRequest = null;

    public AlarmsRequest(HuaweiBRSupport support, boolean smart) {
        super(support);
        this.serviceId = Alarms.id;
        this.commandId = smart ? SmartAlarmRequest.id : EventAlarmsRequest.id;
        if (!smart)
            eventAlarmsRequest = new EventAlarmsRequest(support.secretsProvider);
    }

    /*public void listEventAlarm() {
        commandId = EventAlarmsList.id;
        alarmTLV.put(EventAlarmsList.get, (byte)0);
    }

    public void listSmartAlarm() {
        commandId = SmartAlarmsList.id;
        alarmTLV.put(SmartAlarmsList.get, (byte)0);
    }*/

    public void addEventAlarm(Alarm alarm) {
        eventAlarmsRequest.addAlarm(
                (byte) alarm.getPosition(),
                (alarm.getEnabled() && !alarm.getUnused()),
                (short) (((alarm.getHour() << 8) + (alarm.getMinute() & 0xFF)) & 0xFFFF),
                (byte) alarm.getRepetition(),
                alarm.getTitle()
        );
    }

    public void buildSmartAlarm(Alarm alarm) {
        this.smartAlarmRequest = new SmartAlarmRequest(
                support.secretsProvider,
                (alarm.getEnabled() && !alarm.getUnused()),
                (short) (alarm.getHour() << 8 + (byte) alarm.getMinute()),
                (byte) alarm.getRepetition(),
                (byte) 5 // TODO: setting for ahead time
        );
    }
    
    @Override
    protected byte[] createRequest() throws RequestCreationException {
        try {
            if (eventAlarmsRequest != null) {
                return eventAlarmsRequest.serialize();
            } else if (smartAlarmRequest != null) {
                return smartAlarmRequest.serialize();
            } else {
                throw new RequestCreationException();
            }
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Alarm");
    }
}
