package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

import static nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.services.Notifications.SetNotification;

public class SetNotificationRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetNotificationRequest.class);

    public SetNotificationRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = SetNotification.id;
    }

    @Override
    protected byte[] createRequest() {
        boolean activate = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getBoolean(DeviceSettingsPreferenceConst.PREF_NOTIFICATION_ENABLE, false);
        requestedPacket = new HuaweiPacket(
                serviceId,
                commandId,
                new HuaweiTLV()
                    .put(SetNotification.container,
                        new HuaweiTLV()
                            .put(SetNotification.setStatus, activate)
                            .put(SetNotification.setStatus2, activate)
                    )
        ).encrypt(support.getSecretKey(), support.getIV());
        byte[] serializedPacket = requestedPacket.serialize();
        LOG.debug("Send Set Notification Request: " + StringUtils.bytesToHex(serializedPacket));
        return serializedPacket;
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Notification");
    }
}
