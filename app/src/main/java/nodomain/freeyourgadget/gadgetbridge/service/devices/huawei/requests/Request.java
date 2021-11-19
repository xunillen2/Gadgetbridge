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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import android.bluetooth.BluetoothGattCharacteristic;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiCrypto;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEOperation;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.miband.operations.OperationStatus;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

// Ripped from nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.requests.Request

/*
Add capacity to :
    - chain requests;
    - use data from a past request;
    - call a function after last request.
*/

public class Request extends AbstractBTLEOperation<HuaweiSupport> {
    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    protected int serviceId;
    protected int commandId;
    protected HuaweiPacket requestedPacket = null;
    protected HuaweiPacket receivedPacket = null;
    protected HuaweiSupport support;
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
    }

    public enum RequestState {
        COMPLETE, INCOMPLETE, BAD
    }

    public Request(HuaweiSupport support, TransactionBuilder builder) {
        super(support);
        this.support = support;
        this.builder = builder;
    }

    public Request(HuaweiSupport support) {
        super(support);
        this.support = support;
    }

    @Override
    protected void doPerform() throws IOException {
        BluetoothGattCharacteristic characteristic = support
                .getCharacteristic(HuaweiConstants.UUID_CHARACTERISTIC_HUAWEI_WRITE);
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
    }

    protected byte[] createRequest() {
        return null;
    }

    protected void processResponse() throws GBException {}

    public void handleResponse() throws GBException {
        processResponse();
        if (nextRequest != null && !stopChain) {
            try {
                nextRequest.perform();
            } catch (IOException e) {
                GB.toast(getContext(), "nextRequest failed", Toast.LENGTH_SHORT, GB.ERROR, e);
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

    public boolean expectsResponse() {
        return true;
    }

    public void hasBeenHandled(){
        cleanHandled = true;
    }

    public boolean cleanHasBeenHandled() {
        return cleanHandled;
    }

    public Request pastRequest(Request req) {
        pastRequest = req;
        return this;
    }

    public Request nextRequest(Request req) {
        nextRequest = req;
        nextRequest.setBuilder(createTransactionBuilder(nextRequest.getName()));
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

    public RequestState checkReceivedPacket(byte[] data) throws GBException {
        if (receivedPacket != null) {
            if ( receivedPacket.serviceId == requestedPacket.serviceId && receivedPacket.commandId == requestedPacket.commandId) {
                receivedPacket.parse(data);
            }
        } else {
            receivedPacket = new  HuaweiPacket().parse(data);
        }
        if (requestedPacket != null) { // Request as been defined
            if ( receivedPacket.serviceId == requestedPacket.serviceId && receivedPacket.commandId == requestedPacket.commandId) {
                if ( receivedPacket.complete) {
                    if (receivedPacket.tlv.contains(HuaweiConstants.TAG_RESULT)) {
                        byte[] result = receivedPacket.tlv.getBytes(HuaweiConstants.TAG_RESULT);
                        if (!Arrays.equals(result, HuaweiConstants.RESULT_SUCCESS)) {
                            throw new GBException("Packet returned ErrorCode: " + StringUtils.bytesToHex(result));
                        }
                    } else if (receivedPacket.tlv.contains(HuaweiConstants.CryptoTags.encryption)) {
                            receivedPacket.decrypt(support.getSecretKey());
                    }
                    return RequestState.COMPLETE;
                }
                return RequestState.INCOMPLETE;
            }
            return RequestState.BAD;
        } else { // Request is not defined - AsynchronousRequest
            if ( receivedPacket.complete) {
                LOG.debug("AsynchronousResponse complete");
                return  RequestState.COMPLETE;
            } else {
                LOG.debug("AsynchronousResponse incomplete");
                return RequestState.INCOMPLETE;
            }
        }
    }

    public boolean isPacketComplet() {
        return receivedPacket.complete;
    }

    public String getName() {
        Class thisClass = getClass();
        while (thisClass.isAnonymousClass()) thisClass = thisClass.getSuperclass();
        return thisClass.getSimpleName();
    }

    public void setBuilder(TransactionBuilder builder) {
        this.builder = builder;
        builder.setGattCallback(support);
    }

    public void setFinalizeReq(RequestCallback finalizeReq) {
        this.finalizeReq = finalizeReq;
    }

}