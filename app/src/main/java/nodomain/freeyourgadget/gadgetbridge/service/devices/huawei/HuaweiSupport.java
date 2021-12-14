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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice.State;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
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
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SendNotificationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.Request;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.Request.RequestCallback;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetAuthRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetBatteryLevelRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetBondParamsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetBondRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetLinkParamsRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.GetProductInformationRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetActivateOnRotateRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetDateFormatRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetLocaleRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetNavigateOnRotateRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetTimeRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetWearLocationRequest;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

// TO TEST
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetWorkModeRequest;

public class HuaweiSupport extends AbstractBTLEDeviceSupport{
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiSupport.class);

    protected int mtu = 65535;
    private boolean needsAuth = false;
    protected static String deviceMac; //get it from GB
    protected String macAddress;
    protected Request asynchronousRequest = null;
    protected boolean isAsynchronous = false;

    public static long encryptionCounter = 0;
    protected int msgId = 0;

    protected final List<Request> inProgressRequests = Collections.synchronizedList(new ArrayList<Request>());

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
            inProgressRequests.add(linkParamsReq);
            inProgressRequests.add(authReq);
            inProgressRequests.add(bondParamsReq);
            inProgressRequests.add(bondReq);
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
            SetDateFormatRequest setDateFormatReq = new SetDateFormatRequest(this);
            inProgressRequests.add(setDateFormatReq);
            setDateFormatReq.perform();
            onSetTime();
            GetProductInformationRequest productInformationReq = new GetProductInformationRequest(this);
            inProgressRequests.add(productInformationReq);
            productInformationReq.perform();
            GetBatteryLevelRequest batteryLevelReq = new GetBatteryLevelRequest(this);
            inProgressRequests.add(batteryLevelReq);
            batteryLevelReq.perform();
            builder.add(new SetDeviceStateAction(gbDevice, GBDevice.State.INITIALIZED, getContext()));
            performConnected(builder.getTransaction());
            // Workaround to enable PREF_HUAWEI_ROTATE_WRIST_TO_SWITCH_INFO preference
            SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(deviceMac);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(HuamiConst.PREF_ACTIVATE_DISPLAY_ON_LIFT, "p_on");
            editor.apply();
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
        SharedPreferences sharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());

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

    public HuaweiSupport enableNotifications(TransactionBuilder builder, boolean enable) {
        builder.notify(getCharacteristic(HuaweiConstants.UUID_CHARACTERISTIC_HUAWEI_READ), enable);
        return this;
    }

    public int getMtu() {
        return mtu;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        try {
            synchronized (inProgressRequests) {
                for (Request req : inProgressRequests) {
                    if (handleRequest(data, req)) {
                        return true;
                    }
                }
            }
            return handleAsynchronousResponse(data);
        } catch (GBException e) {
            LOG.error("Invalid response received: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean handleRequest(byte[] data, Request req) throws GBException {
        switch (req.checkReceivedPacket(data)) {
            case COMPLETE:
                req.handleResponse();
                if (req.cleanHasBeenHandled())
                    inProgressRequests.remove(req);
                if (isAsynchronous)
                    isAsynchronous = false;
                    asynchronousRequest = null;
            case INCOMPLETE:
                return true;
            case BAD:
            default:
                return false;
        }
    }

    private boolean handleAsynchronousResponse(byte[] data) {
        LOG.debug("handleAsynchronousResponse");
        isAsynchronous = true;
        try {
            if (asynchronousRequest == null)
                asynchronousRequest = new Request(this);
            return handleRequest(data, asynchronousRequest);
        } catch (GBException e) {
            LOG.debug(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void removeInProgressRequests(Request req) {
        inProgressRequests.remove(req);
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
            // SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());
            switch (config) {
                case DeviceSettingsPreferenceConst.PREF_DATEFORMAT:
                case DeviceSettingsPreferenceConst.PREF_TIMEFORMAT: {
                    SetDateFormatRequest setDateFormatReq = new SetDateFormatRequest(this);
                    inProgressRequests.add(setDateFormatReq);
                    setDateFormatReq.perform();
                    break;
                }
                case SettingsActivity.PREF_MEASUREMENT_SYSTEM:
                case DeviceSettingsPreferenceConst.PREF_LANGUAGE: {
                    SetLocaleRequest setLocaleReq = new SetLocaleRequest(this);
                    inProgressRequests.add(setLocaleReq);
                    setLocaleReq.perform();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_WEARLOCATION: {
                    SetWearLocationRequest setWearLocationReq = new SetWearLocationRequest(this);
                    inProgressRequests.add(setWearLocationReq);
                    setWearLocationReq.perform();
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_LIFTWRIST_NOSHED: {
                    SetActivateOnRotateRequest setActivateOnRotateReq = new SetActivateOnRotateRequest(this);
                    inProgressRequests.add(setActivateOnRotateReq);
                    setActivateOnRotateReq.perform();
                    break;
                }
                case MiBandConst.PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO: {
                    SetNavigateOnRotateRequest setNavigateOnRotateReq = new SetNavigateOnRotateRequest(this);
                    inProgressRequests.add(setNavigateOnRotateReq);
                    setNavigateOnRotateReq.perform();
                    break;
                }
                /*case DeviceSettingsPreferenceConst.PREF_ANTILOST_ENABLED: {
                    boolean enabled = prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_ANTILOST_ENABLED, true);
                    FeaturesCommand features = getCurrentEnabledFeatures();
                    features.setFeature(FeaturesCommand.FEATURE_ANTI_LOST, enabled);
                    sendEnabledFeaturesSetting(features);
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_LONGSIT_SWITCH: {
                    boolean enabled = prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_LONGSIT_SWITCH, false);
                    FeaturesCommand features = getCurrentEnabledFeatures();
                    features.setFeature(FeaturesCommand.FEATURE_SEDENTARY_REMINDER, enabled);
                    sendEnabledFeaturesSetting(features);
                    break;
                }
                case DeviceSettingsPreferenceConst.PREF_LONGSIT_PERIOD: {
                    String periodStr = prefs.getString(DeviceSettingsPreferenceConst.PREF_LONGSIT_PERIOD, "60");
                    try {
                        int period = Integer.parseInt(periodStr);
                        sendSedentaryReminderIntervalSetting(period);
                    } catch (NumberFormatException e) {
                        GB.toast(getContext(), "Invalid sedentary reminder interval value", Toast.LENGTH_SHORT,
                                GB.ERROR, e);
                    }
                    break;
                }*/
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

    }

    @Override
    public void onReset(int flags) {

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
            inProgressRequests.add(sendNotificationReq);
            sendNotificationReq.perform();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteNotification(int id) {

    }

    @Override
    public void onSetTime() {
        try {
            SetTimeRequest setTimeReq = new SetTimeRequest(this);
            inProgressRequests.add(setTimeReq);
            setTimeReq.perform();
        } catch (IOException e) {
            GB.toast(getContext(), "Faile to configure time", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }

    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {

    }

    @Override
    public void onSetCallState(CallSpec callSpec) {
        if (callSpec.command == CallSpec.CALL_INCOMING) {
            SendNotificationRequest sendNotificationReq = new SendNotificationRequest(this);
            try {
                sendNotificationReq.buildNotificationTLVFromCallSpec(callSpec);
                inProgressRequests.add(sendNotificationReq);
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

}
