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

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public abstract class HuaweiCoordinator extends AbstractDeviceCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiCoordinator.class);

    public static TreeMap<Integer, byte[]> commandsPerService;

    public HuaweiCoordinator() {
        this.commandsPerService = new TreeMap<Integer, byte[]>();
    }

    @NonNull
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        ParcelUuid huaweiService = new ParcelUuid(HuaweiConstants.UUID_SERVICE_HUAWEI_SERVICE);
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(huaweiService).build();
        return Collections.singletonList(filter);
    }

    @Nullable
    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }
    
    @Override
    public int getBondingStyle(){
        return BONDING_STYLE_NONE;
    }

    @Override
    protected void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException {
        long deviceId = device.getId();
        QueryBuilder<?> qb = session.getHuaweiActivitySampleDao().queryBuilder();
        qb.where(HuaweiActivitySampleDao.Properties.DeviceId.eq(deviceId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    @Override
    public String getManufacturer() {
        return "Huawei";
    }

    @Override
    public boolean supportsScreenshots() {
        return false;
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return true;
    }

    @Override
    public boolean supportsFindDevice() {
        return false;
    }

    @Override
    public boolean supportsAlarmSnoozing() {
        return false;
    }

    public boolean supportsAlarmDescription(GBDevice device) {
        return true;
    }

    @Override
    public boolean supportsWeather() {
        return false;
    }

    @Override
    public boolean supportsRealtimeData() {
        return false;
    }

    @Override
    public boolean supportsCalendarEvents() {
        return false;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return null;
    }

    @Override
    public boolean supportsAppsManagement() {
        return false;
    }

    @Override
    public int getAlarmSlotCount() {
        return 5;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Override
    public boolean supportsActivityTracking() {
        return true;
    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return false;
    }


    @Override
    public InstallHandler findInstallHandler(Uri uri, Context context) {
        return null;
    }

    @Override
    public SampleProvider<? extends AbstractActivitySample> getSampleProvider(GBDevice device, DaoSession session) {
        return new HuaweiSampleProvider(device, session);
    }

    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_timeformat,
                R.xml.devicesettings_liftwrist_display_noshed,
                R.xml.devicesettings_rotatewrist_cycleinfo,
                R.xml.devicesettings_vibrations_enable,
        };
    }

    public int[] concatSettings(int[] main, int[] specific) {
        int[] settings = Arrays.copyOf(main, main.length + specific.length);
        System.arraycopy(specific, 0, settings, main.length, specific.length);
        return settings;
    }

    public TreeMap<Integer, byte[]> getCommandsPerService() {
        return commandsPerService;
    }

    // Print all Services ID and Commands ID
    public void printCommandsPerService() {
        String msg;
        for(Map.Entry<Integer, byte[]> entry : this.commandsPerService.entrySet()) {
            msg = "ServiceID: " + entry.getKey() + " => Commands: ";
            for (byte b: entry.getValue()) {
                msg += Integer.toHexString(b) + " ";
            }
            LOG.debug(msg);
        }
    }
}
