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
            public short count;

            public static Response fromTlv(HuaweiTLV input) {
                Response returnValue = new Response();
                returnValue.count = input.getObject(0x81).getShort(0x02);
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
            public static class SubContainer {
                public byte type;
                public byte[] timestamp;
            }

            public short number;
            public List<SubContainer> containers;

            public static SleepResponse fromTlv(HuaweiTLV input) {
                HuaweiTLV container = input.getObject(0x81);
                List<HuaweiTLV> subContainers = container.getObjects(0x83);

                SleepResponse returnValue = new SleepResponse();
                returnValue.number = container.getShort(0x02);
                returnValue.containers = new ArrayList<>();
                for (HuaweiTLV subContainerTlv : subContainers) {
                    SubContainer subContainer = new SubContainer();
                    subContainer.type = subContainerTlv.getByte(0x04);
                    subContainer.timestamp = subContainerTlv.getBytes(0x05);
                    returnValue.containers.add(subContainer);
                }
                return returnValue;
            }
        }

        public static class StepResponse {
            public static class SubContainer {
                public static class TV {
                    public final byte tag;
                    public final short value;

                    public TV(byte tag, short value) {
                        this.tag = tag;
                        this.value = value;
                    }

                    @Override
                    public String toString() {
                        return "TV{" +
                                "tag=" + tag +
                                ", value=" + value +
                                '}';
                    }
                }

                /*
                 * Data directly from packet
                 */
                public byte timestampOffset;
                public byte[] data;

                /*
                 * Inferred data
                 */
                public int timestamp;

                public List<TV> parsedData = null;
                public String parsedDataError = "";

                public int steps = -1;
                public int calories = -1;
                public int distance = -1;

                public List<TV> unknownTVs = null;
            }

            public short number;
            public int timestamp;
            public List<SubContainer> containers;

            public static StepResponse fromTlv(HuaweiTLV input) {
                HuaweiTLV container = input.getObject(0x81);
                List<HuaweiTLV> subContainers = container.getObjects(0x84);

                StepResponse returnValue = new StepResponse();
                returnValue.number = container.getShort(0x02);
                returnValue.timestamp = container.getInteger(0x03);
                returnValue.containers = new ArrayList<>();
                for (HuaweiTLV subContainerTlv : subContainers) {
                    SubContainer subContainer = new SubContainer();
                    subContainer.timestampOffset = subContainerTlv.getByte(0x05);
                    subContainer.timestamp = returnValue.timestamp + 60 * subContainer.timestampOffset;
                    subContainer.data = subContainerTlv.getBytes(0x06);
                    parseData(subContainer, subContainer.data);
                    returnValue.containers.add(subContainer);
                }
                return returnValue;
            }

            private static void parseData(SubContainer returnValue, byte[] data) {
                int i = 0;

                if (data.length <= 0) {
                    returnValue.parsedData = null;
                    returnValue.parsedDataError = "Data is missing feature bitmap.";
                    return;
                }
                byte featureBitmap1 = data[i++];

                byte featureBitmap2 = 0;
                if ((featureBitmap1 & 128) != 0) {
                    if (data.length <= i) {
                        returnValue.parsedData = null;
                        returnValue.parsedDataError = "Data is missing second feature bitmap.";
                        return;
                    }
                    featureBitmap2 = data[i++];
                }

                returnValue.parsedData = new ArrayList<>();
                returnValue.unknownTVs = new ArrayList<>();

                // The greater than zero check is because Java is always signed, so we only check 7 bits
                for (byte bitToCheck = 1; bitToCheck > 0; bitToCheck <<= 1) {
                    if ((featureBitmap1 & bitToCheck) != 0) {
                        if (data.length - 2 < i) {
                            returnValue.parsedData = null;
                            returnValue.parsedDataError = "Data is too short for selected features.";
                            return;
                        }

                        short value = (short) ((data[i++] & 0xFF) << 8 | (data[i++] & 0xFF));

                        // The bitToCheck is used as tag, which may not be optimal, but works
                        SubContainer.TV tv = new SubContainer.TV(bitToCheck, value);
                        returnValue.parsedData.add(tv);

                        if (bitToCheck == 0x02)
                            returnValue.steps = value;
                        else if (bitToCheck == 0x04)
                            returnValue.calories = value;
                        else if (bitToCheck == 0x08)
                            returnValue.distance = value;
                        else
                            returnValue.unknownTVs.add(tv);
                    }
                }

                // TODO: second bitmap
            }
        }
    }

    public static class ActivityReminder {
        public static final int id = 0x07;

        public static class Request {
            public static HuaweiTLV toTlv(
                    boolean longSitSwitch,
                    byte longSitInterval,
                    byte[] longSitStart,
                    byte[] longSitEnd,
                    byte cycle
            ) {
                return new HuaweiTLV()
                        .put(0x81, new HuaweiTLV()
                                .put(0x02, longSitSwitch)
                                .put(0x03, longSitInterval)
                                .put(0x04, longSitStart)
                                .put(0x05, longSitEnd)
                                .put(0x06, cycle)
                        );
            }
        }
    }

    public static class TruSleep {
        public static final int id = 0x16;

        public static class Request {
            public static HuaweiTLV toTlv(boolean truSleepSwitch) {
                return new HuaweiTLV()
                        .put(0x01, truSleepSwitch);
            }
        }
    }
}
