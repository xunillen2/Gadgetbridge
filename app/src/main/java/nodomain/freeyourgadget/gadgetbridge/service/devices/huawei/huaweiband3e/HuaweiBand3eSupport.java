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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.huaweiband3e;

import android.widget.Toast;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.SetWorkModeRequest;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class HuaweiBand3eSupport extends HuaweiSupport{
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiBand3eSupport.class);

    public HuaweiBand3eSupport() {
        super();
        mtu = 20;
    }

    @Override
    public void onSendConfiguration(String config) {
        try {
            switch (config) {
                case HuaweiConstants.PREF_HUAWEI_WORKMODE:
                    LOG.debug("Workmode");
                    SetWorkModeRequest setWorkModeReq = new SetWorkModeRequest(this);
                    inProgressRequests.add(setWorkModeReq);
                    setWorkModeReq.perform();
                    break;
                default:
                    super.onSendConfiguration(config);
                    return;
            }
        } catch (IOException e) {
            GB.toast(getContext(), "Configuration of Huawei device failed", Toast.LENGTH_SHORT, GB.ERROR, e);
            e.printStackTrace();
        }
    }


}
