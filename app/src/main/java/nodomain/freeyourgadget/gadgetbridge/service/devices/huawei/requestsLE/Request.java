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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsLE;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiLESupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.miband.operations.OperationStatus;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

// Ripped from nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.requests.Request

/**
 * Add capacity to :
 *     - chain requestsLE;
 *     - use data from a past request;
 *     - call a function after last request.
 */

public class Request {
    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    public static class RequestCreationException extends Exception { }

    protected OperationStatus operationStatus = OperationStatus.INITIAL;
    protected byte serviceId;
    protected byte commandId;
    protected HuaweiPacket receivedPacket = null;
    protected HuaweiLESupport support;
    protected TransactionBuilder builder = null;
    // Be able retrieve data from a previous request
    protected Request pastRequest = null;
    // Be able to autostart a request after this one
    protected Request nextRequest = null;
    protected boolean isSelfQueue = false;
     // Clean support.inProgressRequests after handleResponse()
    protected boolean cleanHandled = true;
    // Callback function to start after the request
    protected RequestCallback finalizeReq = null;
    // Stop chaining requests and clean support.inProgressRequests from these requests
    protected boolean stopChain = false;
    protected static HuaweiCrypto huaweiCrypto = null;

    public interface RequestCallback {
        public void call();
        public void handleException(HuaweiPacket.ParseException e);
    }

    public Request(HuaweiLESupport support, TransactionBuilder builder) {
        this.support = support;
        this.builder = builder;
    }

    public Request(HuaweiLESupport support) {
        this.support = support;
        this.builder = support.createTransactionBuilder(getName());
        this.builder.setCallback(support);
        this.isSelfQueue = true;
    }
    
    public void doPerform() throws IOException {
        BluetoothGattCharacteristic characteristic = support
                .getCharacteristic(HuaweiConstants.UUID_CHARACTERISTIC_HUAWEI_WRITE);
        try {
            byte[] request = createRequest();
            int mtu = support.getMtu();
            if (request.length >= mtu) {
                ByteBuffer buffer = ByteBuffer.wrap(request);
                byte[] data;
                while (buffer.hasRemaining()) {
                    int delta = Math.min(mtu, buffer.remaining());
                    data = new byte[delta];
                    buffer.get(data, 0, delta);
                    builder.write(characteristic, data);
                }
            } else {
                builder.write(characteristic, request);
            }
            builder.wait(100); // Need to wait a little to let some requests end correctly i.e. Battery Level on reconnection to not print correctly
            if (isSelfQueue) {
                support.performConnected(builder.getTransaction());
            }
        } catch (RequestCreationException e) {
            e.printStackTrace();
            // We cannot throw the RequestCreationException, so we throw an IOException
            throw new IOException("Request could not be created");
        }
    }

    protected byte[] createRequest() throws RequestCreationException {
        return null;
    }

    protected void processResponse() throws Exception {}

    public void handleResponse() throws Exception, GBException {
        try {
            this.receivedPacket.parseTlv();
        } catch (HuaweiPacket.ParseException e) {
            LOG.error("Parse TLV exception", e);
            if (finalizeReq != null)
                finalizeReq.handleException(e);
            return;
        }
        processResponse();
        if (nextRequest != null && !stopChain) {
            try {
                nextRequest.doPerform();
            } catch (IOException e) {
                GB.toast(support.getContext(), "nextRequest failed", Toast.LENGTH_SHORT, GB.ERROR, e);
                e.printStackTrace();
            }
        }
        if (nextRequest == null || stopChain) {
            operationStatus = OperationStatus.FINISHED;
            if (finalizeReq != null) {
                finalizeReq.call();
            }
        }
    }

    public void setSelfQueue() {
        isSelfQueue = true;
    }

    public Request pastRequest(Request req) {
        pastRequest = req;
        return this;
    }

    public Request nextRequest(Request req) {
        nextRequest = req;
        nextRequest.setSelfQueue();
        return this;
    }

    public byte[] getValueReturned() {
        return null;
    }

    public void stopChain(Request req) {
        req.stopChain();
        Request next = req.nextRequest;
        if (next != null) {
            next.stopChain(next);
            support.removeInProgressRequests(next);
        }
    }

    public void stopChain() {
        stopChain = true;
    }

    /**
     * Handler for responses from the device
     * @param response The response packet
     * @return True if this request handles this response, false otherwise
     */
    public boolean handleResponse(HuaweiPacket response) {
        if (response.serviceId == serviceId && response.commandId == commandId) {
            receivedPacket = response;
            return true;
        }
        return false;
    }

    protected Context getContext() {
        return support.getContext();
    }

    protected GBDevice getDevice() {
        return support.getDevice();
    }
    
    public HuaweiLESupport getSupport() {
        return support;
    }

    public String getName() {
        Class thisClass = getClass();
        while (thisClass.isAnonymousClass()) thisClass = thisClass.getSuperclass();
        return thisClass.getSimpleName();
    }

    public void setFinalizeReq(RequestCallback finalizeReq) {
        this.finalizeReq = finalizeReq;
    }
}
