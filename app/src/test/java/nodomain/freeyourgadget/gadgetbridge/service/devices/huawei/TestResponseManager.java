package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests.Request;

@RunWith(MockitoJUnitRunner.class)
public class TestResponseManager {

    HuaweiSupport support;
    Field handlersField;
    Field receivedPacketField;
    Field asynchronousResponseField;

    @Before
    public void beforeClass() throws NoSuchFieldException {
        support = new HuaweiSupport();

        handlersField = ResponseManager.class.getDeclaredField("handlers");
        handlersField.setAccessible(true);

        asynchronousResponseField = ResponseManager.class.getDeclaredField("asynchronousResponse");
        asynchronousResponseField.setAccessible(true);

        receivedPacketField = ResponseManager.class.getDeclaredField("receivedPacket");
        receivedPacketField.setAccessible(true);
    }

    @Test
    public void testAddHandler() throws IllegalAccessException {
        Request input = new Request(support);

        List<Request> expectedHandlers = Collections.synchronizedList(new ArrayList<Request>());
        expectedHandlers.add(input);

        ResponseManager responseManager = new ResponseManager(support);
        responseManager.addHandler(input);

        Assert.assertEquals(expectedHandlers, handlersField.get(responseManager));
    }

    @Test
    public void testRemoveHandler() throws IllegalAccessException {
        Request input = new Request(support);
        Request extra = new Request(support);

        List<Request> inputHandlers = Collections.synchronizedList(new ArrayList<Request>());
        inputHandlers.add(extra);
        inputHandlers.add(input);
        inputHandlers.add(extra);

        List<Request> expectedHandlers = Collections.synchronizedList(new ArrayList<Request>());
        expectedHandlers.add(extra);
        expectedHandlers.add(extra);

        ResponseManager responseManager = new ResponseManager(support);
        handlersField.set(responseManager, inputHandlers);

        responseManager.removeHandler(input);

        Assert.assertEquals(expectedHandlers, handlersField.get(responseManager));
    }

    @Test
    public void testHandleDataCompletePacketSynchronous() throws Exception {
        // Note that this is not a proper packet, but that doesn't matter as we're not testing
        // the packet parsing.
        byte[] input = {0x01, 0x02, 0x03, 0x04};

        AsynchronousResponse mockAsynchronousResponse = Mockito.mock(AsynchronousResponse.class);

        HuaweiPacket mockHuaweiPacket = Mockito.mock(HuaweiPacket.class);
        mockHuaweiPacket.complete = true;
        when(mockHuaweiPacket.parse((byte[]) any()))
                .thenReturn(mockHuaweiPacket);

        Request request1 = Mockito.mock(Request.class);
        when(request1.handleResponse((HuaweiPacket) any()))
                .thenReturn(true);
        Request request2 = Mockito.mock(Request.class);
        when(request2.handleResponse((HuaweiPacket) any()))
                .thenReturn(false);

        List<Request> inputHandlers = Collections.synchronizedList(new ArrayList<Request>());
        inputHandlers.add(request1);
        inputHandlers.add(request2);

        List<Request> expectedHandlers = Collections.synchronizedList(new ArrayList<Request>());
        expectedHandlers.add(request2);

        ResponseManager responseManager = new ResponseManager(support);
        handlersField.set(responseManager, inputHandlers);
        receivedPacketField.set(responseManager, mockHuaweiPacket);
        asynchronousResponseField.set(responseManager, mockAsynchronousResponse);

        responseManager.handleData(input);

        Assert.assertEquals(expectedHandlers, handlersField.get(responseManager));
        Assert.assertNull(receivedPacketField.get(responseManager));

        verify(mockHuaweiPacket, times(1)).parse(input);
        verify(mockAsynchronousResponse, times(0)).handleResponse((HuaweiPacket) any());
        verify(request1, times(1)).handleResponse(mockHuaweiPacket);
        verify(request1, times(1)).handleResponse();
        verify(request2, times(0)).handleResponse((HuaweiPacket) any());
        verify(request2, times(0)).handleResponse();
    }

    @Test
    public void testHandleDataCompletePacketAsynchronous() throws Exception {
        // Note that this is not a proper packet, but that doesn't matter as we're not testing
        // the packet parsing.
        byte[] input = {0x01, 0x02, 0x03, 0x04};

        AsynchronousResponse mockAsynchronousResponse = Mockito.mock(AsynchronousResponse.class);

        HuaweiPacket mockHuaweiPacket = Mockito.mock(HuaweiPacket.class);
        mockHuaweiPacket.complete = true;
        when(mockHuaweiPacket.parse((byte[]) any()))
                .thenReturn(mockHuaweiPacket);

        Request request1 = Mockito.mock(Request.class);
        when(request1.handleResponse((HuaweiPacket) any()))
                .thenReturn(false);
        Request request2 = Mockito.mock(Request.class);
        when(request2.handleResponse((HuaweiPacket) any()))
                .thenReturn(false);

        List<Request> inputHandlers = Collections.synchronizedList(new ArrayList<Request>());
        inputHandlers.add(request1);
        inputHandlers.add(request2);

        List<Request> expectedHandlers = Collections.synchronizedList(new ArrayList<Request>());
        expectedHandlers.add(request1);
        expectedHandlers.add(request2);

        ResponseManager responseManager = new ResponseManager(support);
        handlersField.set(responseManager, inputHandlers);
        receivedPacketField.set(responseManager, mockHuaweiPacket);
        asynchronousResponseField.set(responseManager, mockAsynchronousResponse);

        responseManager.handleData(input);

        Assert.assertEquals(expectedHandlers, handlersField.get(responseManager));
        Assert.assertNull(receivedPacketField.get(responseManager));

        verify(mockHuaweiPacket, times(1)).parse(input);
        verify(mockAsynchronousResponse, times(1)).handleResponse(mockHuaweiPacket);
        verify(request1, times(1)).handleResponse(mockHuaweiPacket);
        verify(request1, times(0)).handleResponse();
        verify(request2, times(1)).handleResponse(mockHuaweiPacket);
        verify(request2, times(0)).handleResponse();
    }

    @Test
    public void testHandleDataTwoPartialPacketsSynchronous() throws Exception {
        // Note that this is not a proper packet, but that doesn't matter as we're not testing
        // the packet parsing.
        byte[] input1 = {0x01, 0x02, 0x03, 0x04};
        byte[] input2 = {0x05, 0x06, 0x07, 0x08};

        AsynchronousResponse mockAsynchronousResponse = Mockito.mock(AsynchronousResponse.class);

        HuaweiPacket mockHuaweiPacket = Mockito.mock(HuaweiPacket.class);
        mockHuaweiPacket.complete = false;
        when(mockHuaweiPacket.parse((byte[]) any()))
                .thenReturn(mockHuaweiPacket);

        Request request1 = Mockito.mock(Request.class);
        when(request1.handleResponse((HuaweiPacket) any()))
                .thenReturn(true);
        Request request2 = Mockito.mock(Request.class);
        when(request2.handleResponse((HuaweiPacket) any()))
                .thenReturn(false);

        List<Request> inputHandlers = Collections.synchronizedList(new ArrayList<Request>());
        inputHandlers.add(request1);
        inputHandlers.add(request2);

        List<Request> expectedHandlers1 = Collections.synchronizedList(new ArrayList<Request>());
        expectedHandlers1.add(request1);
        expectedHandlers1.add(request2);

        List<Request> expectedHandlers2 = Collections.synchronizedList(new ArrayList<Request>());
        expectedHandlers2.add(request2);

        ResponseManager responseManager = new ResponseManager(support);
        handlersField.set(responseManager, inputHandlers);
        receivedPacketField.set(responseManager, mockHuaweiPacket);
        asynchronousResponseField.set(responseManager, mockAsynchronousResponse);

        responseManager.handleData(input1);

        Assert.assertEquals(expectedHandlers1, handlersField.get(responseManager));
        Assert.assertEquals(mockHuaweiPacket, receivedPacketField.get(responseManager));

        verify(mockHuaweiPacket, times(1)).parse(input1);
        verify(mockAsynchronousResponse, times(0)).handleResponse((HuaweiPacket) any());
        verify(request1, times(0)).handleResponse(mockHuaweiPacket);
        verify(request1, times(0)).handleResponse();
        verify(request2, times(0)).handleResponse((HuaweiPacket) any());
        verify(request2, times(0)).handleResponse();

        mockHuaweiPacket.complete = true;
        responseManager.handleData(input2);

        Assert.assertEquals(expectedHandlers2, handlersField.get(responseManager));
        Assert.assertNull(receivedPacketField.get(responseManager));

        verify(mockHuaweiPacket, times(1)).parse(input2);
        verify(mockAsynchronousResponse, times(0)).handleResponse((HuaweiPacket) any());
        verify(request1, times(1)).handleResponse(mockHuaweiPacket);
        verify(request1, times(1)).handleResponse();
        verify(request2, times(0)).handleResponse((HuaweiPacket) any());
        verify(request2, times(0)).handleResponse();
    }

    @Test
    public void testHandleDataTwoPartialPacketsAsynchronous() throws Exception {
        // Note that this is not a proper packet, but that doesn't matter as we're not testing
        // the packet parsing.
        byte[] input1 = {0x01, 0x02, 0x03, 0x04};
        byte[] input2 = {0x05, 0x06, 0x07, 0x08};

        AsynchronousResponse mockAsynchronousResponse = Mockito.mock(AsynchronousResponse.class);

        HuaweiPacket mockHuaweiPacket = Mockito.mock(HuaweiPacket.class);
        mockHuaweiPacket.complete = false;
        when(mockHuaweiPacket.parse((byte[]) any()))
                .thenReturn(mockHuaweiPacket);

        Request request1 = Mockito.mock(Request.class);
        when(request1.handleResponse((HuaweiPacket) any()))
                .thenReturn(false);
        Request request2 = Mockito.mock(Request.class);
        when(request2.handleResponse((HuaweiPacket) any()))
                .thenReturn(false);

        List<Request> inputHandlers = Collections.synchronizedList(new ArrayList<Request>());
        inputHandlers.add(request1);
        inputHandlers.add(request2);

        List<Request> expectedHandlers = Collections.synchronizedList(new ArrayList<Request>());
        expectedHandlers.add(request1);
        expectedHandlers.add(request2);

        ResponseManager responseManager = new ResponseManager(support);
        handlersField.set(responseManager, inputHandlers);
        receivedPacketField.set(responseManager, mockHuaweiPacket);
        asynchronousResponseField.set(responseManager, mockAsynchronousResponse);

        responseManager.handleData(input1);

        Assert.assertEquals(expectedHandlers, handlersField.get(responseManager));
        Assert.assertEquals(mockHuaweiPacket, receivedPacketField.get(responseManager));

        verify(mockHuaweiPacket, times(1)).parse(input1);
        verify(mockAsynchronousResponse, times(0)).handleResponse((HuaweiPacket) any());
        verify(request1, times(0)).handleResponse(mockHuaweiPacket);
        verify(request1, times(0)).handleResponse();
        verify(request2, times(0)).handleResponse((HuaweiPacket) any());
        verify(request2, times(0)).handleResponse();

        mockHuaweiPacket.complete = true;
        responseManager.handleData(input2);

        Assert.assertEquals(expectedHandlers, handlersField.get(responseManager));
        Assert.assertNull(receivedPacketField.get(responseManager));

        verify(mockHuaweiPacket, times(1)).parse(input2);
        verify(mockAsynchronousResponse, times(1)).handleResponse((HuaweiPacket) any());
        verify(request1, times(1)).handleResponse(mockHuaweiPacket);
        verify(request1, times(0)).handleResponse();
        verify(request2, times(1)).handleResponse((HuaweiPacket) any());
        verify(request2, times(0)).handleResponse();
    }
}
