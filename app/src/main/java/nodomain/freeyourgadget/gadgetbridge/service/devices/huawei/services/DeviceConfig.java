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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfig {
    public static final int id = 1;

    public static class LinkParams {
        public static final int id = 1;
        public static final int ProtocolVersion = 1;
        public static final int MaxFrameSize = 2;
        public static final int MaxLinkSize = 3;
        public static final int ConnectionInterval = 4;
        public static final int ServerNonce = 5;
        public static final int PathExtendNumber = 6;
    }

    public static class SetDateFormat {
        public static final int id = 4;
        public static final int DateFormat = 2;
        public static final int TimeFormat = 3;
        public static final int SetDateFormat = 129;
    }

    public static class SetTime {
        public static final int id = 5;
        public static final int Timestamp = 1;
        public static final int ZoneOffset = 2;
    }

    public static class ProductInfo {
        public static final int id = 7;
        public static final int BTVersion = 1;
        public static final int ProductType = 2;
        public static final int HardwareVersion = 3;
        public static final int PhoneNumber = 4;
        public static final int MacAddress = 5;
        public static final int IMEI = 6;
        public static final int SoftwareVersion = 7;
        public static final int OpenSourceVersion = 8;
        public static final int SerialNumber = 9;
        public static final int ProductModel = 10;
        public static final int eMMCId = 11;
        public static final int HealthAppSupport = 13;
    }

    public static class Bond {
        public static final int id = 14;
        public static final int BondRequest = 1;
        public static final int Status = 2;
        public static final int RequestCode = 3;
        public static final int ClientSerial = 5;
        public static final int BondingKey = 6;
        public static final int InitVector = 7;
    }

    public static class BondParams {
        public static final int id = 15;
        public static final int Status = 1;
        public static final int StatusInfo = 2;
        public static final int ClientSerial = 3;
        public static final int BTVersion = 4;
        public static final int MaxFrameSize = 5;
        public static final int ClientMacAddress = 7;
        public static final int EncryptionCounter = 9;
    }

    public static class Auth {
        public static final int id = 19;
        public static final int Challenge = 1;
        public static final int Nonce = 2;
    }

    public static class BatteryLevel {
        public static final int id = 8;
        public static final int GetStatus = 1;
    }

    public static class ActivateOnRotate {
        public static final int id = 9;
        public static final int SetStatus = 1;
    }

    public static class FactoryReset {
        public static final int id = 13;
        public static final int SetStatus = 1;
    }

    public static class NavigateOnRotate {
        public static final int id = 27;
        public static final int SetStatus = 1;
    }

    public static class LeftRightWrist {
        public static final int id = 26;
        public static final int SetStatus = 1;
    }

    public DeviceConfig() {
        super();
    }

    public static enum DateFormat {
        YEARFIRST(1),
        MONTHFIRST(2),
        DAYFIRST(3);
        public final int format;
        private DateFormat(int format) {this.format = format;}
    }

    public static enum TimeFormat {
        HOURS12(1),
        HOURS24(2);
        public final int format;
        private TimeFormat(int format) {this.format = format;}
    }
}
