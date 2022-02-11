package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.Request;

/**
 * Manages all response data.
 */
public class ResponseManager {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiSupport.class);

    private final List<Request> handlers = Collections.synchronizedList(new ArrayList<Request>());
    private HuaweiPacket receivedPacket;
    private final AsynchronousResponse asynchronousResponse;

    public ResponseManager(HuaweiSupport support) {
        this.asynchronousResponse = new AsynchronousResponse(support);
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
     * @throws GBException Thrown if the data cannot be parsed
     */
    public void handleData(byte[] data) throws GBException {
        if (receivedPacket == null)
            receivedPacket = new HuaweiPacket().parse(data);
        else
            receivedPacket.parse(data);

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

                handler.handleResponse();
            }
            receivedPacket = null;
        }
    }
}
