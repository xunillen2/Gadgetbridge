package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class FitnessData {

    public static final byte id = 0x07;

    public static class MessageCount {
        public static final byte sleepId = 0x0C;
        public static final byte stepId = 0x0A;

        public static class Request extends HuaweiPacket {
            public Request(
                    SecretsProvider secretsProvider,
                    byte commandId,
                    int start,
                    int end
            ) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = commandId;

                this.tlv = new HuaweiTLV()
                        .put(0x81)
                        .put(0x03, start)
                        .put(0x04, end);
                this.tlv.encrypt(secretsProvider.getSecretKey(), secretsProvider.getIv());

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public short count;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
            }

            @Override
            protected void parseTlv() {
                this.tlv.decrypt(secretsProvider.getSecretKey());
                this.count = this.tlv.getObject(0x81).getShort(0x02);
                this.complete = true;
            }
        }
    }

    public static class MessageData {
        public static final byte sleepId = 0x0D;
        public static final byte stepId = 0x0B;

        public static class Request extends HuaweiPacket {
            public Request(SecretsProvider secretsProvider, byte commandId, short count) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = commandId;

                this.tlv = new HuaweiTLV()
                        .put(0x81, new HuaweiTLV()
                                .put(0x02, count)
                        );
                this.tlv.encrypt(secretsProvider.getSecretKey(), secretsProvider.getIv());

                this.complete = true;
            }
        }

        public static class SleepResponse extends HuaweiPacket {
            public static class SubContainer {
                public byte type;
                public byte[] timestamp;
            }

            public short number;
            public List<SubContainer> containers;

            public SleepResponse(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = sleepId;
            }

            @Override
            protected void parseTlv() {
                this.tlv.decrypt(secretsProvider.getSecretKey());

                HuaweiTLV container = this.tlv.getObject(0x81);
                List<HuaweiTLV> subContainers = container.getObjects(0x83);

                this.number = container.getShort(0x02);
                this.containers = new ArrayList<>();
                for (HuaweiTLV subContainerTlv : subContainers) {
                    SubContainer subContainer = new SubContainer();
                    subContainer.type = subContainerTlv.getByte(0x04);
                    subContainer.timestamp = subContainerTlv.getBytes(0x05);
                    this.containers.add(subContainer);
                }
            }
        }

        public static class StepResponse extends HuaweiPacket {
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

            private static final List<Byte> singleByteTagList = new ArrayList<>();
            static {
                singleByteTagList.add((byte) 0x20);
                singleByteTagList.add((byte) 0x40);
            }

            public StepResponse(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = stepId;
            }

            @Override
            protected void parseTlv() {
                this.tlv.decrypt(secretsProvider.getSecretKey());

                HuaweiTLV container = this.tlv.getObject(0x81);
                List<HuaweiTLV> subContainers = container.getObjects(0x84);

                if (!container.contains(0x02) || !container.contains(0x03)) {
                    // TODO: exception?
                    this.number = -1;
                    this.timestamp = -1;
                    return;
                }

                this.number = container.getShort(0x02);
                this.timestamp = container.getInteger(0x03);
                this.containers = new ArrayList<>();
                for (HuaweiTLV subContainerTlv : subContainers) {
                    SubContainer subContainer = new SubContainer();
                    subContainer.timestampOffset = subContainerTlv.getByte(0x05);
                    subContainer.timestamp = this.timestamp + 60 * subContainer.timestampOffset;
                    subContainer.data = subContainerTlv.getBytes(0x06);
                    parseData(subContainer, subContainer.data);
                    this.containers.add(subContainer);
                }
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
                        short value;

                        if (singleByteTagList.contains(bitToCheck)) {
                            if (data.length - 1 < i) {
                                returnValue.parsedData = null;
                                returnValue.parsedDataError = "Data is too short for selected features.";
                                return;
                            }

                            value = data[i++];
                        } else {
                            if (data.length - 2 < i) {
                                returnValue.parsedData = null;
                                returnValue.parsedDataError = "Data is too short for selected features.";
                                return;
                            }

                            value = (short) ((data[i++] & 0xFF) << 8 | (data[i++] & 0xFF));
                        }

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

    public static class FitnessTotals {
        public static final byte id = 0x03;

        public static class Request extends HuaweiPacket {
            public Request(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01);
                this.tlv.encrypt(secretsProvider.getSecretKey(), secretsProvider.getIv());

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {

            public int totalSteps = 0;
            public int totalCalories = 0;
            public int totalDistance = 0;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = id;
            }

            @Override
            protected void parseTlv() {
                this.tlv.decrypt(secretsProvider.getSecretKey());

                HuaweiTLV container = this.tlv.getObject(0x81);
                List<HuaweiTLV> containers = container.getObjects(0x83);

                for (HuaweiTLV tlv : containers) {
                    if (tlv.contains(0x05))
                        totalSteps += tlv.getInteger(0x05);
                    if (tlv.contains(0x06))
                        totalCalories += tlv.getShort(0x06);
                    if (tlv.contains(0x07))
                        totalDistance += tlv.getInteger(0x07);
                }

                this.complete = true;
            }
        }
    }

    public static class ActivityReminder {
        public static final byte id = 0x07;

        public static class Request extends HuaweiPacket {
            public Request(
                    SecretsProvider secretsProvider,
                    boolean longSitSwitch,
                    byte longSitInterval,
                    byte[] longSitStart,
                    byte[] longSitEnd,
                    byte cycle
            ) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x81, new HuaweiTLV()
                                .put(0x02, longSitSwitch)
                                .put(0x03, longSitInterval)
                                .put(0x04, longSitStart)
                                .put(0x05, longSitEnd)
                                .put(0x06, cycle)
                        );
                this.tlv.encrypt(secretsProvider.getSecretKey(), secretsProvider.getIv());

                this.complete = true;
            }
        }
    }

    public static class TruSleep {
        public static final byte id = 0x16;

        public static class Request extends HuaweiPacket {
            public Request(SecretsProvider secretsProvider, boolean truSleepSwitch) {
                super(secretsProvider);

                this.serviceId = FitnessData.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01, truSleepSwitch);
                this.tlv.encrypt(secretsProvider.getSecretKey(), secretsProvider.getIv());

                this.complete = true;
            }
        }
    }
}
