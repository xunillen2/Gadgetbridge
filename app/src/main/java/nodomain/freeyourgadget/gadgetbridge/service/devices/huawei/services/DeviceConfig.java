/*  Copyright (C) 2021-2022 Gaignon Damien

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfig {
    public static final int id = 0x01;

    public static class LinkParams {
        public static final int id = 0x01;
        public static final int ProtocolVersion = 0x01;
        public static final int MaxFrameSize = 0x02;
        public static final int MaxLinkSize = 0x03;
        public static final int ConnectionInterval = 0x04;
        public static final int ServerNonce = 0x05;
        public static final int PathExtendNumber = 0x06;
    }

    public static class SupportedServices {
        public static final int id = 0x02;
        public static final int Services = 0x01;
        public static final int ActiveServices = 0x02;
    }

    public static class SupportedCommands {
        public static final int id = 0x03;
        public static final int ServiceId = 0x02;
        public static final int Commands = 0x03;
        public static final int ActiveCommands = 0x04;
        public static final int SupportedCommands = 0x81;
    }

    public static class SetDateFormat {
        public static final int id = 0x04;
        public static final int DateFormat = 0x02;
        public static final int TimeFormat = 0x03;
        public static final int SetDateFormat = 0x81;
    }

    public static class SetTime {
        public static final int id = 0x05;
        public static final int Timestamp = 0x01;
        public static final int ZoneOffset = 0x02;
    }

    public static class ProductInfo {
        public static final int id = 0x07;
        public static final int BTVersion = 0x01;
        public static final int ProductType = 0x02;
        public static final int HardwareVersion = 0x03;
        public static final int PhoneNumber = 0x04;
        public static final int MacAddress = 0x05;
        public static final int IMEI = 0x06;
        public static final int SoftwareVersion = 0x07;
        public static final int OpenSourceVersion = 0x08;
        public static final int SerialNumber = 0x09;
        public static final int ProductModel = 0x0A;
        public static final int eMMCId = 0x0B;
        public static final int HealthAppSupport = 0x0D;
    }

    public static class Bond {
        public static final int id = 0x0E;
        public static final int BondRequest = 0x01;
        public static final int Status = 0x02;
        public static final int RequestCode = 0x03;
        public static final int ClientSerial = 0x05;
        public static final int BondingKey = 0x06;
        public static final int InitVector = 0x07;
    }

    public static class BondParams {
        public static final int id = 0x0F;
        public static final int Status = 0x01;
        public static final int StatusInfo = 0x02;
        public static final int ClientSerial = 0x03;
        public static final int BTVersion = 0x04;
        public static final int MaxFrameSize = 0x05;
        public static final int ClientMacAddress = 0x07;
        public static final int EncryptionCounter = 0x09;
    }

    public static class Auth {
        public static final int id = 0x13;
        public static final int Challenge = 0x01;
        public static final int Nonce = 0x02;
    }

    public static class BatteryLevel {
        public static final int id = 0x08;
        public static final int GetStatus = 0x01;
    }

    public static class ActivateOnRotate {
        public static final int id = 0x09;
        public static final int SetStatus = 0x01;
    }

    public static class FactoryReset {
        public static final int id = 0x0D;
        public static final int Send = 0x01;
    }

    public static class NavigateOnRotate {
        public static final int id = 0x1B;
        public static final int SetStatus = 0x01;
    }

    public static class WearLocation {
        public static final int id = 0x1A;
        public static final int SetStatus = 0x01;
    }

    public DeviceConfig() {
        super();
    }

    public static class Date {
        public static final int yearFirst = 0x01;
        public static final int monthFirst = 0x02;
        public static final int dayFirst = 0x03;
    }

    public static class Time {
        public static final int hours12 = 0x01;
        public static final int hours24 = 0x02;
    }
}
