package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requestsBR.Request;

/**
 * Manages all response data.
 *
 * @param <T> HuaweiLESupport or HuaweiBTSupport
 *
 */
public class ResponseManagerBR {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseManagerBR.class);

    private final List<Request> handlers = Collections.synchronizedList(new ArrayList<Request>());
    private HuaweiPacket receivedPacket;
    private final AsynchronousResponseBR asynchronousResponse;
    private final HuaweiBRSupport support;

    public ResponseManagerBR(HuaweiBRSupport support) {
        this.asynchronousResponse = new AsynchronousResponseBR(support);
        this.support = support;
    }

    /**
     * Add a request to the response handler list
     * @param handler The request to handle responses
     */
    public void addHandler(Request handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
    }

    /**
     * Remove a request from the response handler list
     * @param handler The request to remove
     */
    public void removeHandler(Request handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    /**
     * Parses the data into a Huawei Packet.
     * If the packet is complete, it will be handled by the first request that accepts it,
     * or as an asynchronous request otherwise.
     *
     * @param data The received data
     */
    public void handleData(byte[] data) {
        try {
            if (receivedPacket == null)
                receivedPacket = new HuaweiPacket(support.secretsProvider).parse(data);
            else
                receivedPacket = receivedPacket.parse(data);
        } catch (HuaweiPacket.ParseException e) {
            LOG.error("Packet parse exception", e);

            // Clean up so the next message may be parsed correctly
            this.receivedPacket = null;
            return;
        }

        if (receivedPacket.complete) {
            Request handler = null;
            synchronized (handlers) {
                for (Request req : handlers) {
                    if (req.handleResponse(receivedPacket)) {
                        handler = req;
                        break;
                    }
                }
            }

            if (handler == null) {
                LOG.debug("Service: " + receivedPacket.serviceId + ", command: " + receivedPacket.commandId + ", asynchronous response.");

                // Asynchronous response
                asynchronousResponse.handleResponse(receivedPacket);
            } else {
                LOG.debug("Service: " + receivedPacket.serviceId + ", command: " + receivedPacket.commandId + ", handled by: " + handler.getClass());

                synchronized (handlers) {
                    handlers.remove(handler);
                }

                try {
                    handler.handleResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            receivedPacket = null;
        }
    }
}
