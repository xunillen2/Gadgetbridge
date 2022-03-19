package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Notifications;

public class SetWearMessagePushRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(SetWearMessagePushRequest.class);

    public SetWearMessagePushRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = Notifications.id;
        this.commandId = Notifications.SetWearMessagePushRequest.id;
    }

    @Override
    protected byte[] createRequest() {
        boolean activate = GBApplication
            .getDeviceSpecificSharedPrefs(support.getDevice().getAddress())
            .getBoolean(DeviceSettingsPreferenceConst.PREF_NOTIFICATION_ENABLE, false);
        return new Notifications.SetWearMessagePushRequest(support.secretsProvider, activate).serialize();
    }

    @Override
    protected void processResponse() throws GBException {
        LOG.debug("handle Set WearMessage Push ");
    }
}
