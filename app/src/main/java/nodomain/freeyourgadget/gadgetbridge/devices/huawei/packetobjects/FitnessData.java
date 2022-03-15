package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packetobjects;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class FitnessData {
    public static final int id = 0x07;

    public static class MessageCount {
        public static final int sleepId = 0x0C;
        public static final int stepId = 0x0A;

        public static class Request {
            public static HuaweiTLV toTlv(int start, int end) {
                return new HuaweiTLV()
                        .put(0x81)
                        .put(0x03, start)
                        .put(0x04, end);
            }
        }

        public static class Response {
            public static class Container {
                public short count;
            }

            public Container container;

            public static Response fromTlv(HuaweiTLV input) {
                Response returnValue = new Response();
                returnValue.container = new Container();
                returnValue.container.count = input.getObject(0x81).getShort(0x02);
                return returnValue;
            }
        }
    }

    public static class MessageData {
        public static final int sleepId = 0x0D;
        public static final int stepId = 0x0B;

        public static class Request {
            public static HuaweiTLV toTlv(short count) {
                return new HuaweiTLV()
                        .put(
                                0x81,
                                new HuaweiTLV()
                                        .put(0x02, count)
                        );
            }
        }

        public static class SleepResponse {
            public static class Container {
                public static class SubContainer {
                    public byte type;
                    public byte[] timestamp;
                }

                public short number;
                public List<SubContainer> containers;
            }

            public Container container;

            public static SleepResponse fromTlv(HuaweiTLV input) {
                HuaweiTLV container = input.getObject(0x81);
                List<HuaweiTLV> subContainers = container.getObjects(0x83);

                SleepResponse returnValue = new SleepResponse();
                returnValue.container = new Container();
                returnValue.container.number = container.getShort(0x02);
                returnValue.container.containers = new ArrayList<>();
                for (HuaweiTLV subContainerTlv : subContainers) {
                    Container.SubContainer subContainer = new Container.SubContainer();
                    subContainer.type = subContainerTlv.getByte(0x04);
                    subContainer.timestamp = subContainerTlv.getBytes(0x05);
                    returnValue.container.containers.add(subContainer);
                }
                return returnValue;
            }
        }

        public static class StepResponse {
            public static class Container {
                public static class SubContainer {
                    public byte timestampOffset;
                    public byte[] data;
                }

                public short number;
                public int timestamp;
                public List<SubContainer> containers;
            }

            public Container container;

            public static StepResponse fromTlv(HuaweiTLV input) {
                HuaweiTLV container = input.getObject(0x81);
                List<HuaweiTLV> subContainers = container.getObjects(0x84);

                StepResponse returnValue = new StepResponse();
                returnValue.container = new Container();
                returnValue.container.number = container.getShort(0x02);
                returnValue.container.timestamp = container.getInteger(0x03);
                returnValue.container.containers = new ArrayList<>();
                for (HuaweiTLV subContainerTlv : subContainers) {
                    Container.SubContainer subContainer = new Container.SubContainer();
                    subContainer.timestampOffset = subContainerTlv.getByte(0x05);
                    subContainer.data = subContainerTlv.getBytes(0x06);
                    returnValue.container.containers.add(subContainer);
                }
                return returnValue;
            }
        }
    }
}
