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

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst;
import nodomain.freeyourgadget.gadgetbridge.devices.zetime.ZeTimeConstants;
import nodomain.freeyourgadget.gadgetbridge.entities.HuaweiActivitySample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice.State;
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
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetSleepDataCountRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SendNotificationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.AlarmsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.Request;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.Request.RequestCallback;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetAuthRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetBatteryLevelRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetBondParamsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetBondRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetLinkParamsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetProductInformationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetSupportedCommandsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetSupportedServicesRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SendFactoryResetRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetActivateOnRotateRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetActivityReminderRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetDateFormatRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetLocaleRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetNotificationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetNavigateOnRotateRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetTimeRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetTruSleepRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetWearLocationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetWearMessagePushRequest;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.DeviceHelper;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

// TO TEST
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetWorkModeRequest;

public class HuaweiSupport extends AbstractBTLEDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiSupport.class);

    protected int mtu = 65535;
    private boolean needsAuth = false;
    protected static String deviceMac; //get it from GB
    protected String macAddress;

    public static long encryptionCounter = 0;
    protected int msgId = 0;

    protected ResponseManager responseManager = new ResponseManager(this);

    // This is a bit of a hack, but it works
    private ArrayList<Integer> handledActivityTimestamps = null;

    public HuaweiSupport() {
        super(LOG);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_HUMAN_INTERFACE_DEVICE);
        addSupportedService(HuaweiConstants.UUID_SERVICE_HUAWEI_SERVICE);
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.setGattCallback(this);
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
            };
            bondParamsReq.setFinalizeReq(finalizeReq);
            bondReq.setFinalizeReq(finalizeReq);
            linkParamsReq.perform();
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

    protected void initializeDeviceFinalize() {
        TransactionBuilder builder = createTransactionBuilder("Initializing");
        builder.setGattCallback(this);
        builder.notify(getCharacteristic(HuaweiConstants.UUID_CHARACTERISTIC_HUAWEI_READ), true);
        builder.add(new SetDeviceStateAction(gbDevice, GBDevice.State.INITIALIZING, getContext()));
        try {
            String name = gbDevice.getName();
            if (name != null && !name.toLowerCase().startsWith(HuaweiConstants.HU_BAND3E_NAME)) {
                onSetDateFormat();
            }
            onSetTime();
            GetProductInformationRequest productInformationReq = new GetProductInformationRequest(this);
            responseManager.addHandler(productInformationReq);
            productInformationReq.perform();
            GetBatteryLevelRequest batteryLevelReq = new GetBatteryLevelRequest(this);
            responseManager.addHandler(batteryLevelReq);
            batteryLevelReq.perform();
            builder.add(new SetDeviceStateAction(gbDevice, GBDevice.State.INITIALIZED, getContext()));
            performConnected(builder.getTransaction());
            // Workaround to enable PREF_HUAWEI_ROTATE_WRIST_TO_SWITCH_INFO preference
            SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(deviceMac);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(HuamiConst.PREF_ACTIVATE_DISPLAY_ON_LIFT, "p_on");
            editor.apply();
            initializeAlarms();
            // getAlarms();
            // getCommands();
            onSetWearLocation();
            onSetActivateOnRotate();
            onSetNavigateOnRotate();
            onSetActivityReminder();
            onSetTrusleep();
            onSetNotification();
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
        inProgressRequests.add(alarmReq);
        alarmReq.perform();
        alarmReq = new AlarmsRequest(this, true);
        alarmReq.listSmartAlarm();
        inProgressRequests.add(alarmReq);
        alarmReq.perform();
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
        supportedServicesReq.perform();
    }

    public int getMtu() {
        return mtu;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();

        try {
            responseManager.handleData(data);
            return true;
        } catch (GBException e) {
            LOG.error("Invalid response received: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
            // SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(deviceMac);
            switch (config) {
                case DeviceSettingsPreferenceConst.PREF_DATEFORMAT:
                case DeviceSettingsPreferenceConst.PREF_TIMEFORMAT: {
                    onSetDateFormat();
                    break;
                }
                case SettingsActivity.PREF_MEASUREMENT_SYSTEM:
                case DeviceSettingsPreferenceConst.PREF_LANGUAGE: {
                    SetLocaleRequest setLocaleReq = new SetLocaleRequest(this);
                    responseManager.addHandler(setLocaleReq);
                    setLocaleReq.perform();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_WEARLOCATION: {
                    onSetWearLocation();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_LIFTWRIST_NOSHED: {
                    onSetActivateOnRotate();
                    break;
                }
                case MiBandConst.PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO: {
                    onSetNavigateOnRotate();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_LONGSIT_SWITCH:
                case DeviceSettingsPreferenceConst.PREF_LONGSIT_PERIOD:
                case DeviceSettingsPreferenceConst.PREF_LONGSIT_START:
                case DeviceSettingsPreferenceConst.PREF_LONGSIT_END:
                case ZeTimeConstants.PREF_INACTIVITY_MO:
                case ZeTimeConstants.PREF_INACTIVITY_TU:
                case ZeTimeConstants.PREF_INACTIVITY_WE:
                case ZeTimeConstants.PREF_INACTIVITY_TH:
                case ZeTimeConstants.PREF_INACTIVITY_FR:
                case ZeTimeConstants.PREF_INACTIVITY_SA:
                case ZeTimeConstants.PREF_INACTIVITY_SU: {
                    onSetActivityReminder();
                    break;
                }
                case HuaweiConstants.PREF_HUAWEI_TRUSLEEP: {
                    onSetTrusleep();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_NOTIFICATION_ENABLE: {
                    onSetNotification();
                    break;
                }
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
            return;
        }

        handledActivityTimestamps = new ArrayList<>();

        int start = 0;
        int end = (int) (System.currentTimeMillis() / 1000);

        try (DBHandler db = GBApplication.acquireDB()) {
            HuaweiSampleProvider sampleProvider = new HuaweiSampleProvider(getDevice(), db.getDaoSession());
            start = sampleProvider.getLastFetchTimestamp();
        } catch (Exception e) {
            LOG.warn("Exception for start time, using 01/01/2000 - 00:00:00.");
        }

        if (start == 0)
            start = 946684800; // Some bands don't work with zero timestamp, so starting later

        GetSleepDataCountRequest getSleepDataCountRequest = new GetSleepDataCountRequest(this, start, end);
        try {
            responseManager.addHandler(getSleepDataCountRequest);
            getSleepDataCountRequest.perform();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReset(int flags) {
        try {
            if(flags== GBDeviceProtocol.RESET_FLAGS_FACTORY_RESET) {
                SendFactoryResetRequest sendFactoryResetReq = new SendFactoryResetRequest(this);
                responseManager.addHandler(sendFactoryResetReq);
                sendFactoryResetReq.perform();
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

    public void onSetNotification() {
        try {
            SetNotificationRequest setNotificationReq = new SetNotificationRequest(this);
            responseManager.addHandler(setNotificationReq);
            setNotificationReq.perform();
            SetWearMessagePushRequest setWearMessagePushReq = new SetWearMessagePushRequest(this);
            responseManager.addHandler(setWearMessagePushReq);
            setWearMessagePushReq.perform();
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
            sendNotificationReq.perform();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteNotification(int id) {

    }

    public void onSetDateFormat() {
        try {
            SetDateFormatRequest setDateFormatReq = new SetDateFormatRequest(this);
            responseManager.addHandler(setDateFormatReq);
            setDateFormatReq.perform();
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
            setTimeReq.perform();
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
            smartAlarmReq.perform();
            eventAlarmReq.perform();
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
                sendNotificationReq.perform();
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

    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {

    }

    private void transmitActivityStatus() {

    }

    public void addInProgressRequest(Request request) {
        responseManager.addHandler(request);
    }

    public void addActivity(int timestamp, short duration, byte type) {
        try (DBHandler db = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(db.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(getDevice(), db.getDaoSession()).getId();

            // This sample is so that GB has a starting point of the activity
            HuaweiActivitySample activitySample_pre = new HuaweiActivitySample(
                    timestamp - 1,
                    deviceId,
                    userId,
                    ActivitySample.NOT_MEASURED,
                    0
            );
            // This sample is really for the data
            HuaweiActivitySample activitySample_start = new HuaweiActivitySample(
                    timestamp,
                    deviceId,
                    userId,
                    type,
                    1
            );
            // This sample is so the graphs are easier to read
            HuaweiActivitySample activitySample_end = new HuaweiActivitySample(
                    timestamp + duration - 1,
                    deviceId,
                    userId,
                    type,
                    1
            );
            // This sample is to make GB know there is an end to the activity here
            HuaweiActivitySample activitySample_post = new HuaweiActivitySample(
                    timestamp + duration,
                    deviceId,
                    userId,
                    ActivitySample.NOT_MEASURED,
                    0
            );

            HuaweiSampleProvider sampleProvider = new HuaweiSampleProvider(getDevice(), db.getDaoSession());

            if (!handledActivityTimestamps.contains(timestamp - 1))
                sampleProvider.addGBActivitySample(activitySample_pre);
            sampleProvider.addGBActivitySample(activitySample_start);
            sampleProvider.addGBActivitySample(activitySample_end);
            if (!handledActivityTimestamps.contains(timestamp + duration))
                sampleProvider.addGBActivitySample(activitySample_post);

            handledActivityTimestamps.add(timestamp);
            handledActivityTimestamps.add(timestamp + duration - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onSetWearLocation() {
        try {
            SetWearLocationRequest setWearLocationReq = new SetWearLocationRequest(this);
            responseManager.addHandler(setWearLocationReq);
            setWearLocationReq.perform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Wear Location", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void onSetActivateOnRotate() {
        try {
            SetActivateOnRotateRequest setActivateOnRotateReq = new SetActivateOnRotateRequest(this);
            responseManager.addHandler(setActivateOnRotateReq);
            setActivateOnRotateReq.perform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Activate on Rotate", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void onSetNavigateOnRotate() {
        try {
            SetNavigateOnRotateRequest setNavigateOnRotateReq = new SetNavigateOnRotateRequest(this);
            responseManager.addHandler(setNavigateOnRotateReq);
            setNavigateOnRotateReq.perform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Navigate on Rotate", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void onSetActivityReminder() {
        try {
            SetActivityReminderRequest setActivityReminderReq = new SetActivityReminderRequest(this);
            responseManager.addHandler(setActivityReminderReq);
            setActivityReminderReq.perform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure Activity reminder", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }

    public void onSetTrusleep() {
        try {
            SetTruSleepRequest setTruSleepReq = new SetTruSleepRequest(this);
            responseManager.addHandler(setTruSleepReq);
            setTruSleepReq.perform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure truSleep", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }
}
