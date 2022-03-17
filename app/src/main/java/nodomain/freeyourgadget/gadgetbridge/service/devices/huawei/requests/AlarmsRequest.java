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
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.EventAlarms;
import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.SmartAlarms;
import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.EventAlarmsList;
import static nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Alarms.SmartAlarmsList;

public class AlarmsRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmsRequest.class);

    private HuaweiTLV alarmTLV;
    private HuaweiTLV eventAlarmTLV = null;
    private final int maxEventAlarm = 4;

    private EventAlarms.Request eventAlarmsRequest;

    public AlarmsRequest(HuaweiSupport support, boolean smart) {
        super(support);
        this.serviceId = Alarms.id;
        this.commandId = smart ? SmartAlarms.id : EventAlarms.id;
        if (smart) {
            this.alarmTLV = new HuaweiTLV();
            this.eventAlarmTLV = new HuaweiTLV();
        } else {
            eventAlarmsRequest = new EventAlarms.Request();
        }
    }

    /*public void listEventAlarm() {
        commandId = EventAlarmsList.id;
        alarmTLV.put(EventAlarmsList.get, (byte)0);
    }

    public void listSmartAlarm() {
        commandId = SmartAlarmsList.id;
        alarmTLV.put(SmartAlarmsList.get, (byte)0);
    }*/

    private byte[] getTime(Alarm alarm) {
        return new byte[]{(byte)alarm.getHour(), (byte)alarm.getMinute()};
    }

    public void addEventAlarm(Alarm alarm) {
        eventAlarmsRequest.addAlarm(
                (byte) alarm.getPosition(),
                (alarm.getEnabled() && !alarm.getUnused()),
                (short) (alarm.getHour() << 8 + (byte) alarm.getMinute()),
                (byte) alarm.getRepetition(),
                alarm.getTitle()
        );
        if (alarm.getPosition() == maxEventAlarm)
            alarmTLV = eventAlarmsRequest.toTlv();
    }

    public void buildSmartAlarm(Alarm alarm) {
        HuaweiTLV eventAlarmDataTLV = new HuaweiTLV()
            .put(SmartAlarms.index, (byte)1)
            .put(SmartAlarms.setStatus, (alarm.getEnabled() && !alarm.getUnused()))
            .put(SmartAlarms.setStartTime, getTime(alarm))
            .put(SmartAlarms.repeat, (byte)alarm.getRepetition())
            .put(SmartAlarms.aheadTime, (byte)5);
        eventAlarmTLV.put(SmartAlarms.separator, eventAlarmDataTLV);
        alarmTLV.put(SmartAlarms.start, eventAlarmTLV);
    }
    
    @Override
    protected byte[] createRequest() {
        requestedPacket = new HuaweiPacket(
            serviceId,
            commandId,
            alarmTLV
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Request Alarm: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Alarm");
    }
}