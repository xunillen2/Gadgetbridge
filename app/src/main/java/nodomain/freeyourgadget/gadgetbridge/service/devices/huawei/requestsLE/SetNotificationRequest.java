package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiLESupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Notifications;

public class SetNotificationRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetNotificationRequest.class);

    public SetNotificationRequest(HuaweiLESupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = Notifications.SetNotificationRequest.id;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        boolean activate = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getBoolean(DeviceSettingsPreferenceConst.PREF_NOTIFICATION_ENABLE, false);
        try {
            return new Notifications.SetNotificationRequest(support.secretsProvider, activate).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set Notification");
    }
}
