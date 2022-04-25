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
package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import java.util.UUID;

import static nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport.BASE_UUID;

public final class HuaweiConstants {

    public static final UUID UUID_SERVICE_HUAWEI_SERVICE = UUID.fromString(String.format(BASE_UUID, "FE86"));
    public static final UUID UUID_CHARACTERISTIC_HUAWEI_WRITE = UUID.fromString(String.format(BASE_UUID, "FE01"));
    public static final UUID UUID_CHARACTERISTIC_HUAWEI_READ = UUID.fromString(String.format(BASE_UUID, "FE02"));

    public static final byte HUAWEI_MAGIC = 0x5A;

    public static final byte PROTOCOL_VERSION = 0x02;

    public static final int TAG_RESULT = 127;
    public static final byte[] RESULT_SUCCESS = new byte[]{0x00, 0x01, (byte)0x86, (byte)0xA0};

    public static class CryptoTags {
        public static final int encryption = 124;
        public static final int initVector = 125;
        public static final int cipherText = 126;
    }

    public static final String HO_BAND3_NAME = "honor band 3-";
    public static final String HO_BAND4_NAME = "honor band 4-";
    public static final String HO_BAND5_NAME = "honor band 5-";
    public static final String HU_BAND3E_NAME = "huawei band 3e-";
    public static final String HU_BAND4E_NAME = "huawei band 4e-";
    public static final String HU_BAND6_NAME = "huawei band 6-";
    public static final String HU_WATCHGT2E_NAME = "huawei watch gt 2e-";
    public static final String HU_WATCHGT_NAME = "huawei watch gt-";

    public static final String PREF_HUAWEI_ADDRESS = "huawei_address";
    public static final String PREF_HUAWEI_WORKMODE = "workmode";
    public static final String PREF_HUAWEI_TRUSLEEP = "trusleep";

}
