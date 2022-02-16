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
        public static final int protocolVersion = 0x01;
        public static final int maxFrameSize = 0x02;
        public static final int maxLinkSize = 0x03;
        public static final int connectionInterval = 0x04;
        public static final int serverNonce = 0x05;
        public static final int pathExtendNumber = 0x06;
    }

    public static class SupportedServices {
        public static final int id = 0x02;
        public static final int services = 0x01;
        public static final int activeServices = 0x02;
    }

    public static class SupportedCommands {
        public static final int id = 0x03;
        public static final int serviceId = 0x02;
        public static final int commands = 0x03;
        public static final int activeCommands = 0x04;
        public static final int supportedCommands = 0x81;
    }

    public static class SetDateFormat {
        public static final int id = 0x04;
        public static final int dateFormat = 0x02;
        public static final int timeFormat = 0x03;
        public static final int setDateFormat = 0x81;
    }

    public static class SetTime {
        public static final int id = 0x05;
        public static final int timestamp = 0x01;
        public static final int zoneOffset = 0x02;
    }

    public static class ProductInfo {
        public static final int id = 0x07;
        public static final int BTVersion = 0x01;
        public static final int productType = 0x02;
        public static final int hardwareVersion = 0x03;
        public static final int phoneNumber = 0x04;
        public static final int macAddress = 0x05;
        public static final int IMEI = 0x06;
        public static final int softwareVersion = 0x07;
        public static final int openSourceVersion = 0x08;
        public static final int serialNumber = 0x09;
        public static final int productModel = 0x0A;
        public static final int eMMCId = 0x0B;
        public static final int healthAppSupport = 0x0D;
    }

    public static class Bond {
        public static final int id = 0x0E;
        public static final int bondRequest = 0x01;
        public static final int status = 0x02;
        public static final int requestCode = 0x03;
        public static final int clientSerial = 0x05;
        public static final int bondingKey = 0x06;
        public static final int initVector = 0x07;
    }

    public static class BondParams {
        public static final int id = 0x0F;
        public static final int status = 0x01;
        public static final int statusInfo = 0x02;
        public static final int clientSerial = 0x03;
        public static final int BTVersion = 0x04;
        public static final int maxFrameSize = 0x05;
        public static final int clientMacAddress = 0x07;
        public static final int encryptionCounter = 0x09;
    }

    public static class Auth {
        public static final int id = 0x13;
        public static final int challenge = 0x01;
        public static final int nonce = 0x02;
    }

    public static class BatteryLevel {
        public static final int id = 0x08;
        public static final int getStatus = 0x01;
    }

    public static class ActivateOnRotate {
        public static final int id = 0x09;
        public static final int setStatus = 0x01;
    }

    public static class FactoryReset {
        public static final int id = 0x0D;
        public static final int send = 0x01;
    }

    public static class NavigateOnRotate {
        public static final int id = 0x1B;
        public static final int setStatus = 0x01;
    }

    public static class WearLocation {
        public static final int id = 0x1A;
        public static final int setStatus = 0x01;
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
