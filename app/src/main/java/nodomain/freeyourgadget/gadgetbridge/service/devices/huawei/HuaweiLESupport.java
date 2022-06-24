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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Workout;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst;
import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummary;
import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummaryDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutDataSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutPaceSample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutPaceSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiWorkoutSummarySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.entities.Alarm;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.User;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.CalendarEventSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CannedMessagesSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.RecordedDataTypes;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceBusyAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.StopNotificationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetFitnessTotalsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetSleepDataCountRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetStepDataCountRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetWorkoutCountRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SendNotificationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetMusicRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.AlarmsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.Request;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.Request.RequestCallback;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetAuthRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetBatteryLevelRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetBondParamsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetBondRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetDndPriorityRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetLinkParamsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetProductInformationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetSupportedCommandsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.GetSupportedServicesRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SendDndAddRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SendFactoryResetRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SendDndDeleteRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetActivateOnRotateRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetActivityReminderRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetDateFormatRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetLocaleRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetNotificationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetNavigateOnRotateRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetTimeRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetTruSleepRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetWearLocationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetWearMessagePushRequest;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FitnessData;
// TODO: change
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE.SetWorkModeRequest;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.DeviceHelper;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class HuaweiLESupport extends AbstractBTLEDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiLESupport.class);

    protected int mtu = 65535;
    private boolean needsAuth = false;
    public static String deviceMac; //get it from GB
    protected String macAddress;

    public long encryptionCounter = 0;
    protected int msgId = 0;

    protected ResponseManagerLE responseManager = new ResponseManagerLE(this);

    private MusicStateSpec musicStateSpec = null;
    private MusicSpec musicSpec = null;

    public HuaweiPacket.SecretsProvider secretsProvider = new HuaweiPacket.SecretsProvider() {
        @Override
        public byte[] getSecretKey() {
            return HuaweiLESupport.this.getSecretKey();
        }

        @Override
        public byte[] getIv() {
            return HuaweiLESupport.this.getIV();
        }
    };

    public HuaweiLESupport() {
        super(LOG);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_HUMAN_INTERFACE_DEVICE);
        addSupportedService(HuaweiConstants.UUID_SERVICE_HUAWEI_SERVICE);

    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.setCallback(this);
        builder.notify(getCharacteristic(HuaweiConstants.UUID_CHARACTERISTIC_HUAWEI_READ), true);
        deviceMac = gbDevice.getAddress();
        createRandomMacAddress();
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.AUTHENTICATING, getContext()));
        try {
            GetLinkParamsRequest linkParamsReq = new GetLinkParamsRequest(this, builder);
            GetAuthRequest authReq = new GetAuthRequest(this);
            GetBondParamsRequest bondParamsReq = new GetBondParamsRequest(this);
            GetBondRequest bondReq = new GetBondRequest(this);
            linkParamsReq.nextRequest(authReq);
            authReq.pastRequest(linkParamsReq);
            authReq.nextRequest(bondParamsReq);
            bondParamsReq.nextRequest(bondReq);
            bondReq.pastRequest(linkParamsReq);
            responseManager.addHandler(linkParamsReq);
            responseManager.addHandler(authReq);
            responseManager.addHandler(bondParamsReq);
            responseManager.addHandler(bondReq);
            RequestCallback finalizeReq = new RequestCallback() {
                @Override
                public void call() {
                    initializeDeviceFinalize();
                }

                @Override
                public void handleException(HuaweiPacket.ParseException e) {
                    LOG.error("Bond params TLV exception", e);
                }
            };
            bondParamsReq.setFinalizeReq(finalizeReq);
            bondReq.setFinalizeReq(finalizeReq);
            linkParamsReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Authenticating Huawei device failed", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
        return builder;
    }

    @Override
    public boolean connectFirstTime() {
        needsAuth = true;
        return connect();
    }

    public void setMtu(short mtu) {
        this.mtu = mtu;
    }

    protected void initializeDeviceFinalize() {
        TransactionBuilder builder = createTransactionBuilder("Initializing");
        builder.setCallback(this);
        builder.notify(getCharacteristic(HuaweiConstants.UUID_CHARACTERISTIC_HUAWEI_READ), true);
        builder.add(new SetDeviceStateAction(gbDevice, GBDevice.State.INITIALIZING, getContext()));
        try {
            String name = gbDevice.getName();
            if (name != null && !name.toLowerCase().startsWith(HuaweiConstants.HU_BAND3E_NAME)) {
                setDateFormat();
            }
            GetProductInformationRequest productInformationReq = new GetProductInformationRequest(this);
            responseManager.addHandler(productInformationReq);
            productInformationReq.doPerform();
            if (needsAuth) {
                // Workaround to enable PREF_HUAWEI_ROTATE_WRIST_TO_SWITCH_INFO preference
                SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(deviceMac);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(DeviceSettingsPreferenceConst.PREF_ACTIVATE_DISPLAY_ON_LIFT, "p_on");
                editor.apply();
                initializeAlarms();
                // getAlarms();
                // getCommands();
                setWearLocation();
                setActivateOnRotate();
                setNavigateOnRotate();
                setActivityReminder();
                setTrusleep();
                setNotification();
                initDnd();
            }
            onSetTime();
            getBatteryLevel();
            builder.add(new SetDeviceStateAction(gbDevice, GBDevice.State.INITIALIZED, getContext()));
            performConnected(builder.getTransaction());
        } catch (IOException e) {
            GB.toast(getContext(), "Initializing Huawei device failed", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public byte[] getSecretKey() {

        SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(deviceMac);

        String authKey = sharedPrefs.getString("authkey", null);
        if (authKey == null || authKey.isEmpty()) {
            SharedPreferences.Editor editor = sharedPrefs.edit();

            authKey = StringUtils.bytesToHex(HuaweiCrypto.generateNonce());
            LOG.debug("Created authKey: " + authKey);
            editor.putString("authkey", authKey);
            editor.apply();
        }
        return GB.hexStringToByteArray(authKey);
    }

    public byte[] getIV() {
        ArrayList ivCounter = HuaweiCrypto.initializationVector(this.encryptionCounter);
        byte[] iv = (byte[])ivCounter.get(0);
        this.encryptionCounter = (long)ivCounter.get(1) & 0xFFFFFFFFL;
        return iv;
    }

    protected void createRandomMacAddress() {
        SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(deviceMac);

        macAddress =  sharedPrefs.getString(HuaweiConstants.PREF_HUAWEI_ADDRESS, null);
        if (macAddress == null || macAddress.isEmpty()) {
            String mac = "FF:FF:FF";
            Random r = new Random();
            for (int i = 0; i < 3; i++) {
                int n = r.nextInt(255);
                mac += String.format(":%02x", n);
            }
            macAddress = mac.toUpperCase();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(HuaweiConstants.PREF_HUAWEI_ADDRESS, macAddress);
            editor.apply();
        }
    }

    public byte[] getMacAddress() {
        return macAddress.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getSerial() {
        return macAddress.replace(":", "").substring(6, 12).getBytes(StandardCharsets.UTF_8);
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    // Do not work on some band, have to check
    /*public void getAlarms() throws IOException {
        AlarmsRequest alarmReq = new AlarmsRequest(this, false);
        alarmReq.listEventAlarm();
        inProgress.add(alarmReq);
        alarmReq.doPerform();
        alarmReq = new AlarmsRequest(this, true);
        alarmReq.listSmartAlarm();
        inProgres.requestsLE.add(alarmReq);
        alarmReq.doPerform();
    }*/

    private void initializeAlarms() {
        // Populate alarms in order to specify important data
        List<Alarm> alarms = DBHelper.getAlarms(gbDevice);
        DeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(gbDevice);
        int supportedNumAlarms = coordinator.getAlarmSlotCount();
        if (alarms.size() == 0) {
            try (DBHandler db = GBApplication.acquireDB()) {
                DaoSession daoSession = db.getDaoSession();
                Device device = DBHelper.getDevice(gbDevice, daoSession);
                User user = DBHelper.getUser(daoSession);
                for (int position = 0; position < supportedNumAlarms; position++) {
                    LOG.info("adding missing alarm at position " + position);
                    DBHelper.store(createDefaultAlarm(device, user, position));
                }
            } catch (Exception e) {
                LOG.error("Error accessing database", e);
            }
        }
    }

    private Alarm createDefaultAlarm(@NonNull Device device, @NonNull User user, int position) {
        boolean smartWakeup = false;
        String title = getContext().getString(R.string.menuitem_alarm);
        String description = getContext().getString(R.string.huawei_alarm_event_description);;
        if (position == 0) {
            smartWakeup = true;
            title = getContext().getString(R.string.alarm_smart_wakeup);
            description = getContext().getString(R.string.huawei_alarm_smart_description);
        }
        return new Alarm(device.getId(), user.getId(), position, false, smartWakeup, false, 0, 6, 30, false, title, description);
    }

    protected void getCommands() throws IOException {
        GetSupportedServicesRequest supportedServicesReq = new GetSupportedServicesRequest(this);
        GetSupportedCommandsRequest supportedCommandsReq = new GetSupportedCommandsRequest(this);
        supportedServicesReq.nextRequest(supportedCommandsReq);
        supportedCommandsReq.pastRequest(supportedServicesReq);
        responseManager.addHandler(supportedServicesReq);
        responseManager.addHandler(supportedCommandsReq);
        supportedServicesReq.doPerform();
    }

    public int getMtu() {
        return mtu;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        responseManager.handleData(data);
        return true;
    }

    public void removeInProgressRequests(Request req) {
        responseManager.removeHandler(req);
    }

    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {

    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {

    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {

    }

    @Override
    public void onSendConfiguration(String config) {
        try {
            switch (config) {
                case DeviceSettingsPreferenceConst.PREF_DATEFORMAT:
                case DeviceSettingsPreferenceConst.PREF_TIMEFORMAT: {
                    setDateFormat();
                    break;
                }
                case SettingsActivity.PREF_MEASUREMENT_SYSTEM:
                case DeviceSettingsPreferenceConst.PREF_LANGUAGE: {
                    SetLocaleRequest setLocaleReq = new SetLocaleRequest(this);
                    responseManager.addHandler(setLocaleReq);
                    setLocaleReq.doPerform();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_WEARLOCATION: {
                    setWearLocation();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_LIFTWRIST_NOSHED: {
                    setActivateOnRotate();
                    break;
                }
                case MiBandConst.PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO: {
                    setNavigateOnRotate();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_ENABLE:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_THRESHOLD:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_START:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_END:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_MO:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_TU:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_WE:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_TH:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_FR:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_SA:
                case DeviceSettingsPreferenceConst.PREF_INACTIVITY_SU: {
                    setActivityReminder();
                    break;
                }
                case HuaweiConstants.PREF_HUAWEI_TRUSLEEP: {
                    setTrusleep();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_NOTIFICATION_ENABLE: {
                    setNotification();
                    break;
                }
                case HuaweiConstants.PREF_HUAWEI_WORKMODE:
                    SetWorkModeRequest setWorkModeReq = new SetWorkModeRequest(this);
                    responseManager.addHandler(setWorkModeReq);
                    setWorkModeReq.doPerform();
                    break;
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_START:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_END:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_LIFT_WRIST:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_MO:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_TU:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_WE:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_TH:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_FR:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_SA:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_SU: {
                    setDnd();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_FIND_PHONE:
                case DeviceSettingsPreferenceConst.PREF_FIND_PHONE_DURATION:
                    // TODO: enable/disable the find phone applet on band
                    break;
            }
        } catch (IOException e) {
            GB.toast(getContext(), "Configuration of Huawei device failed", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }

    }

    @Override
    public void onReadConfiguration(String config) {

    }

    @Override
    public void onTestNewFunction() {

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {

    }

    @Override
    public void onScreenshotReq() {

    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
    
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {

    }

    @Override
    public void onInstallApp(Uri uri) {

    }

    @Override
    public void onAppInfoReq() {

    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {

    }

    @Override
    public void onAppDelete(UUID uuid) {

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {

    }

    @Override
    public void onAppReorder(UUID[] uuids) {

    }

    @Override
    public void onFetchRecordedData(int dataTypes) {
        if (getDevice().isBusy()) {
            LOG.warn("Device is already busy with " + getDevice().getBusyTask() + ", so won't fetch data now.");
            // TODO: better way of letting user know?
            // TODO: use string that can be translated
            GB.toast("Device is already busy with " + getDevice().getBusyTask() + ", so won't fetch data now.", Toast.LENGTH_LONG, 0);
            return;
        }

        if (dataTypes == RecordedDataTypes.TYPE_ACTIVITY) {
            fetchActivityData();
        } else if (dataTypes == RecordedDataTypes.TYPE_GPS_TRACKS) {
            fetchWorkoutData();
        } else {
            LOG.warn("Recorded data type {} not implemented yet.", dataTypes);
        }
    }

    private void fetchActivityData() {
        int sleepStart = 0;
        int stepStart = 0;
        int end = (int) (System.currentTimeMillis() / 1000);

        SharedPreferences sharedPreferences = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());
        long prefLastSyncTime = sharedPreferences.getLong("lastSyncTimeMillis", 0);
        if (prefLastSyncTime != 0) {
            sleepStart = (int) (prefLastSyncTime / 1000);
            stepStart = (int) (prefLastSyncTime / 1000);

            // Reset for next calls
            sharedPreferences.edit().putLong("lastSyncTimeMillis", 0).apply();
        } else {
            try (DBHandler db = GBApplication.acquireDB()) {
                HuaweiSampleProvider sampleProvider = new HuaweiSampleProvider(getDevice(), db.getDaoSession());
                sleepStart = sampleProvider.getLastSleepFetchTimestamp();
                stepStart = sampleProvider.getLastStepFetchTimestamp();
            } catch (Exception e) {
                LOG.warn("Exception for getting start times, using 01/01/2000 - 00:00:00.");
            }

            // Some bands don't work with zero timestamp, so starting later
            if (sleepStart == 0)
                sleepStart = 946684800;
            if (stepStart == 0)
                stepStart = 946684800;
        }

        TransactionBuilder transactionBuilder = createTransactionBuilder("FetchRecordedData");
        transactionBuilder.add(new SetDeviceBusyAction(getDevice(), getContext().getString(R.string.busy_task_fetch_activity_data), getContext()));

        final GetSleepDataCountRequest getSleepDataCountRequest = new GetSleepDataCountRequest(this, transactionBuilder, sleepStart, end);
        final GetStepDataCountRequest getStepDataCountRequest = new GetStepDataCountRequest(this, stepStart, end);
        final GetFitnessTotalsRequest getFitnessTotalsRequest = new GetFitnessTotalsRequest(this);

        getFitnessTotalsRequest.setFinalizeReq(new RequestCallback() {
            @Override
            public void call() {
                handleSyncFinished();
            }

            @Override
            public void handleException(HuaweiPacket.ParseException e) {
                LOG.error("Fitness totals exception", e);
                handleSyncFinished();
            }
        });

        getStepDataCountRequest.setFinalizeReq(new RequestCallback() {
            @Override
            public void call() {
                try {
                    responseManager.addHandler(getFitnessTotalsRequest);
                    getFitnessTotalsRequest.doPerform();
                } catch (IOException e) {
                    handleSyncFinished();
                    e.printStackTrace();
                }
            }

            @Override
            public void handleException(HuaweiPacket.ParseException e) {
                LOG.error("Step data count exception", e);
                handleSyncFinished();
            }
        });

        getSleepDataCountRequest.setFinalizeReq(new RequestCallback() {
            @Override
            public void call() {
                try {
                    responseManager.addHandler(getStepDataCountRequest);
                    getStepDataCountRequest.doPerform();
                } catch (IOException e) {
                    handleSyncFinished();
                    e.printStackTrace();
                }
            }

            @Override
            public void handleException(HuaweiPacket.ParseException e) {
                LOG.error("Sleep data count exception", e);
                handleSyncFinished();
            }
        });

        try {
            responseManager.addHandler(getSleepDataCountRequest);
            getSleepDataCountRequest.doPerform();
        } catch (IOException e) {
            handleSyncFinished();
            e.printStackTrace();
        }
    }

    private void fetchWorkoutData() {
        int start = 0;
        int end = (int) (System.currentTimeMillis() / 1000);

        SharedPreferences sharedPreferences = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());
        long prefLastSyncTime = sharedPreferences.getLong("lastSportsActivityTimeMillis", 0);
        if (prefLastSyncTime != 0) {
            start = (int) (prefLastSyncTime / 1000);

            // Reset for next calls
            sharedPreferences.edit().putLong("lastSportsActivityTimeMillis", 0).apply();
        } else {
            try (DBHandler db = GBApplication.acquireDB()) {
                Long userId = DBHelper.getUser(db.getDaoSession()).getId();
                Long deviceId = DBHelper.getDevice(getDevice(), db.getDaoSession()).getId();

                QueryBuilder qb1 = db.getDaoSession().getHuaweiWorkoutSummarySampleDao().queryBuilder().where(
                        HuaweiWorkoutSummarySampleDao.Properties.DeviceId.eq(deviceId),
                        HuaweiWorkoutSummarySampleDao.Properties.UserId.eq(userId)
                ).orderDesc(
                        HuaweiWorkoutSummarySampleDao.Properties.StartTimestamp
                ).limit(1);

                List<HuaweiWorkoutSummarySample> samples1 = qb1.list();
                if (!samples1.isEmpty())
                    start = samples1.get(0).getEndTimestamp();

                QueryBuilder qb2 = db.getDaoSession().getBaseActivitySummaryDao().queryBuilder().where(
                        BaseActivitySummaryDao.Properties.DeviceId.eq(deviceId),
                        BaseActivitySummaryDao.Properties.UserId.eq(userId)
                ).orderDesc(
                        BaseActivitySummaryDao.Properties.StartTime
                ).limit(1);

                List<BaseActivitySummary> samples2 = qb2.list();
                if (!samples2.isEmpty())
                    start = Math.min(start, (int) (samples2.get(0).getEndTime().getTime() / 1000L));

                start = start + 1;
            } catch (Exception e) {
                LOG.warn("Exception for getting start time, using 10/06/2022 - 00:00:00.");
            }

            if (start == 0 || start == 1)
                start = 1654819200;
        }

        TransactionBuilder transactionBuilder = createTransactionBuilder("FetchWorkoutData");
        // TODO: maybe use a different string from the other synchronization
        transactionBuilder.add(new SetDeviceBusyAction(getDevice(), getContext().getString(R.string.busy_task_fetch_activity_data), getContext()));

        final GetWorkoutCountRequest getWorkoutCountRequest = new GetWorkoutCountRequest(this, transactionBuilder, start, end);
        getWorkoutCountRequest.setFinalizeReq(new RequestCallback() {
            @Override
            public void call() {
                handleSyncFinished();
            }

            @Override
            public void handleException(HuaweiPacket.ParseException e) {
                LOG.error("Workout parsing exception", e);
                handleSyncFinished();
            }
        });

        try {
            responseManager.addHandler(getWorkoutCountRequest);
            getWorkoutCountRequest.doPerform();
        } catch (IOException e) {
            handleSyncFinished();
            e.printStackTrace();
        }
    }

    private void handleSyncFinished() {
        if (getDevice().isBusy()) {
            getDevice().unsetBusyTask();
            getDevice().sendDeviceUpdateIntent(getContext());
        }
        GB.signalActivityDataFinish();
    }

    @Override
    public void onReset(int flags) {
        try {
            if(flags== GBDeviceProtocol.RESET_FLAGS_FACTORY_RESET) {
                SendFactoryResetRequest sendFactoryResetReq = new SendFactoryResetRequest(this);
                responseManager.addHandler(sendFactoryResetReq);
                sendFactoryResetReq.doPerform();
            }
        } catch (IOException e) {
            GB.toast(getContext(), "Factory resetting Huawei device failed", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onHeartRateTest() {

    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {

    }

    @Override
    public void onFindDevice(boolean start) {

    }

    @Override
    public void onSetConstantVibration(int integer) {
    
    }

    public void setNotification() {
        try {
            SetNotificationRequest setNotificationReq = new SetNotificationRequest(this);
            responseManager.addHandler(setNotificationReq);
            setNotificationReq.doPerform();
            SetWearMessagePushRequest setWearMessagePushReq = new SetWearMessagePushRequest(this);
            responseManager.addHandler(setWearMessagePushReq);
            setWearMessagePushReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Setting notification failed", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public int getNotificationId() {
        if (msgId < 256) {
            msgId += 1;
        } else {
            msgId = 0;
        }
        return msgId;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        SendNotificationRequest sendNotificationReq = new SendNotificationRequest(this);
        try {
            sendNotificationReq.buildNotificationTLVFromNotificationSpec(notificationSpec);
            responseManager.addHandler(sendNotificationReq);
            sendNotificationReq.doPerform();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteNotification(int id) {

    }

    public void setDateFormat() {
        try {
            SetDateFormatRequest setDateFormatReq = new SetDateFormatRequest(this);
            responseManager.addHandler(setDateFormatReq);
            setDateFormatReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure date format", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onSetTime() {
        try {
            SetTimeRequest setTimeReq = new SetTimeRequest(this);
            responseManager.addHandler(setTimeReq);
            setTimeReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure time", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onSetAlarms(ArrayList<? extends nodomain.freeyourgadget.gadgetbridge.model.Alarm> alarms) {
        AlarmsRequest smartAlarmReq = new AlarmsRequest(this, true);
        responseManager.addHandler(smartAlarmReq);
        AlarmsRequest eventAlarmReq = new AlarmsRequest(this, false);
        responseManager.addHandler(eventAlarmReq);
        for (nodomain.freeyourgadget.gadgetbridge.model.Alarm alarm : alarms) {
            if (alarm.getPosition() == 0) {
                smartAlarmReq.buildSmartAlarm(alarm);
            } else {
                eventAlarmReq.addEventAlarm(alarm);
            }
        }
        try {
            smartAlarmReq.doPerform();
            eventAlarmReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure alarms", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onSetCallState(CallSpec callSpec) {
        if (callSpec.command == CallSpec.CALL_INCOMING) {
            SendNotificationRequest sendNotificationReq = new SendNotificationRequest(this);
            try {
                sendNotificationReq.buildNotificationTLVFromCallSpec(callSpec);
                responseManager.addHandler(sendNotificationReq);
                sendNotificationReq.doPerform();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (
                callSpec.command == CallSpec.CALL_ACCEPT ||
                callSpec.command == CallSpec.CALL_START ||
                callSpec.command == CallSpec.CALL_REJECT ||
                callSpec.command == CallSpec.CALL_END
        ) {
            StopNotificationRequest stopNotificationRequest = new StopNotificationRequest(this);
            try {
                stopNotificationRequest.doPerform();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {

    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {
        this.musicStateSpec = stateSpec;
        sendSetMusic();
    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {
        this.musicSpec = musicSpec;
        sendSetMusic();
    }

    public void sendSetMusic() {
        // This often gets called twice in a row because of onSetMusicState and onSetMusicInfo
        // Maybe we can consolidate that into just one request?
        SetMusicRequest setMusicRequest = new SetMusicRequest(this, this.musicStateSpec, this.musicSpec);
        try {
            responseManager.addHandler(setMusicRequest);
            setMusicRequest.doPerform();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transmitActivityStatus() {

    }

    public void addInProgressRequest(Request request) {
        responseManager.addHandler(request);
    }

    public void addSleepActivity(int timestamp, short duration, byte type) {
        try (DBHandler db = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(db.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(getDevice(), db.getDaoSession()).getId();
            HuaweiSampleProvider sampleProvider = new HuaweiSampleProvider(getDevice(), db.getDaoSession());

            HuaweiActivitySample activitySample = new HuaweiActivitySample(
                    timestamp,
                    deviceId,
                    userId,
                    timestamp + duration,
                    (byte) FitnessData.MessageData.sleepId,
                    type,
                    1,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED,
                    ActivitySample.NOT_MEASURED
            );
            activitySample.setProvider(sampleProvider);

            sampleProvider.addGBActivitySample(activitySample);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addStepData(int timestamp, short steps, short calories, short distance, byte spo) {
        try (DBHandler db = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(db.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(getDevice(), db.getDaoSession()).getId();
            HuaweiSampleProvider sampleProvider = new HuaweiSampleProvider(getDevice(), db.getDaoSession());

            HuaweiActivitySample activitySample = new HuaweiActivitySample(
                    timestamp,
                    deviceId,
                    userId,
                    timestamp + 60,
                    (byte) FitnessData.MessageData.stepId,
                    ActivitySample.NOT_MEASURED,
                    1,
                    steps,
                    calories,
                    distance,
                    spo,
                    ActivitySample.NOT_MEASURED
            );
            activitySample.setProvider(sampleProvider);

            sampleProvider.addGBActivitySample(activitySample);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTotalFitnessData(int steps, int calories, int distance) {
        LOG.debug("FITNESS total steps: " + steps);
        LOG.debug("FITNESS total calories: " + calories); // TODO: May actually be kilocalories
        LOG.debug("FITNESS total distance: " + distance + " m");

        // TODO: potentially do more with this, maybe through realtime data?
    }

    public Long addWorkoutTotalsData(Workout.WorkoutTotals.Response packet) {
        try (DBHandler db = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(db.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(getDevice(), db.getDaoSession()).getId();

            // Avoid duplicates
            QueryBuilder qb = db.getDaoSession().getHuaweiWorkoutSummarySampleDao().queryBuilder().where(
                    HuaweiWorkoutSummarySampleDao.Properties.UserId.eq(userId),
                    HuaweiWorkoutSummarySampleDao.Properties.DeviceId.eq(deviceId),
                    HuaweiWorkoutSummarySampleDao.Properties.WorkoutNumber.eq(packet.number),
                    HuaweiWorkoutSummarySampleDao.Properties.StartTimestamp.eq(packet.startTime),
                    HuaweiWorkoutSummarySampleDao.Properties.EndTimestamp.eq(packet.endTime)
            );
            List<HuaweiWorkoutSummarySample> results = qb.build().list();
            Long workoutId = null;
            if (!results.isEmpty())
                workoutId = results.get(0).getWorkoutId();

            byte[] raw;
            if (packet.rawData == null)
                raw = null;
            else
                raw = StringUtils.bytesToHex(packet.rawData).getBytes(StandardCharsets.UTF_8);

            HuaweiWorkoutSummarySample summarySample = new HuaweiWorkoutSummarySample(
                    workoutId,
                    deviceId,
                    userId,
                    packet.number,
                    packet.status,
                    packet.startTime,
                    packet.endTime,
                    packet.calories,
                    packet.distance,
                    packet.stepCount,
                    packet.totalTime,
                    packet.duration,
                    packet.type,
                    raw
            );
            db.getDaoSession().getHuaweiWorkoutSummarySampleDao().insertOrReplace(summarySample);

            return summarySample.getWorkoutId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addWorkoutSampleData(Long workoutId, List<Workout.WorkoutData.Response.Data> dataList) {
        if (workoutId == null)
            return;

        try (DBHandler db = GBApplication.acquireDB()) {
            HuaweiWorkoutDataSampleDao dao = db.getDaoSession().getHuaweiWorkoutDataSampleDao();

            for (Workout.WorkoutData.Response.Data data : dataList) {
                byte[] unknown;
                if (data.unknownData == null)
                    unknown = null;
                else
                    unknown = StringUtils.bytesToHex(data.unknownData).getBytes(StandardCharsets.UTF_8);

                HuaweiWorkoutDataSample dataSample = new HuaweiWorkoutDataSample(
                        workoutId,
                        data.timestamp,
                        data.heartRate,
                        data.speed,
                        data.cadence,
                        data.stepLength,
                        data.groundContactTime,
                        data.impact,
                        data.swingAngle,
                        data.foreFootLanding,
                        data.midFootLanding,
                        data.backFootLanding,
                        data.eversionAngle,
                        unknown
                );
                dao.insertOrReplace(dataSample);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addWorkoutPaceData(Long workoutId, List<Workout.WorkoutPace.Response.Block> paceList) {
        if (workoutId == null)
            return;

        try (DBHandler db = GBApplication.acquireDB()) {
            HuaweiWorkoutPaceSampleDao dao = db.getDaoSession().getHuaweiWorkoutPaceSampleDao();

            for (Workout.WorkoutPace.Response.Block block : paceList) {
                HuaweiWorkoutPaceSample paceSample = new HuaweiWorkoutPaceSample(
                        workoutId,
                        block.distance,
                        block.type,
                        block.pace,
                        block.correction
                );
                dao.insertOrReplace(paceSample);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setWearLocation() {
        try {
            SetWearLocationRequest setWearLocationReq = new SetWearLocationRequest(this);
            responseManager.addHandler(setWearLocationReq);
            setWearLocationReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Wear Location", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void getBatteryLevel() {
        try {
            GetBatteryLevelRequest batteryLevelReq = new GetBatteryLevelRequest(this);
            responseManager.addHandler(batteryLevelReq);
            batteryLevelReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to get Batterry Level", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void setActivateOnRotate() {
        try {
            SetActivateOnRotateRequest setActivateOnRotateReq = new SetActivateOnRotateRequest(this);
            responseManager.addHandler(setActivateOnRotateReq);
            setActivateOnRotateReq.doPerform();
            SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(deviceMac);
            boolean statusDndLiftWrist = sharedPrefs.getBoolean(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_LIFT_WRIST, false);
            if (statusDndLiftWrist) {
                setDnd();
            }
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Activate on Rotate", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void setNavigateOnRotate() {
        try {
            SetNavigateOnRotateRequest setNavigateOnRotateReq = new SetNavigateOnRotateRequest(this);
            responseManager.addHandler(setNavigateOnRotateReq);
            setNavigateOnRotateReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Navigate on Rotate", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void setActivityReminder() {
        try {
            SetActivityReminderRequest setActivityReminderReq = new SetActivityReminderRequest(this);
            responseManager.addHandler(setActivityReminderReq);
            setActivityReminderReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Activity reminder", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void setTrusleep() {
        try {
            SetTruSleepRequest setTruSleepReq = new SetTruSleepRequest(this);
            responseManager.addHandler(setTruSleepReq);
            setTruSleepReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure truSleep", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public HuaweiCoordinator getCoordinator() {
        return ((HuaweiCoordinator) DeviceHelper.getInstance().getCoordinator(this.getDevice()));
    }

    public void initDnd() {
        try {
            GetDndPriorityRequest GetDndPriorityReq = new GetDndPriorityRequest(this);
            SendDndDeleteRequest sendDndDeleteReq = new SendDndDeleteRequest(this);
            SendDndAddRequest sendDndAddReq = new SendDndAddRequest(this);
            GetDndPriorityReq.nextRequest(sendDndDeleteReq);
            sendDndDeleteReq.nextRequest(sendDndAddReq);
            responseManager.addHandler(GetDndPriorityReq);
            responseManager.addHandler(sendDndDeleteReq);
            responseManager.addHandler(sendDndAddReq);
            GetDndPriorityReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to init DND", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void setDnd() {
        try {
            SendDndDeleteRequest sendDndDeleteReq = new SendDndDeleteRequest(this);
            SendDndAddRequest sendDndAddReq = new SendDndAddRequest(this);
            sendDndDeleteReq.nextRequest(sendDndAddReq);
            responseManager.addHandler(sendDndDeleteReq);
            responseManager.addHandler(sendDndAddReq);
            sendDndDeleteReq.doPerform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to set DND", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }
}