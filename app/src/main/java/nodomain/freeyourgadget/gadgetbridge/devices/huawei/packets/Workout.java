package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class Workout {
    public static final byte id = 0x17;

    public static class WorkoutCount {
        public static final byte id = 0x07;

        public static class Request extends HuaweiPacket {
            public Request(
                    SecretsProvider secretsProvider,
                    int start,
                    int end
            ) {
                super(secretsProvider);

                this.serviceId = Workout.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x81, new HuaweiTLV()
                                .put(0x03, start)
                                .put(0x04, end)
                        );

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public static class WorkoutNumbers {
                public byte[] rawData;

                public short workoutNumber;
                public short dataCount;
                public short paceCount;
            }

            public short count;
            public List<WorkoutNumbers> workoutNumbers;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (!this.tlv.contains(0x81))
                    throw new MissingTagException(0x81);

                HuaweiTLV container = this.tlv.getObject(0x81);

                if (!container.contains(0x02))
                    throw new MissingTagException(0x02);

                this.count = container.getShort(0x02);
                this.workoutNumbers = new ArrayList<>();

                if (this.count == 0)
                    return;

                if (!container.contains(0x85))
                    throw new MissingTagException(0x85);

                List<HuaweiTLV> subContainers = container.getObjects(0x85);
                for (HuaweiTLV subContainerTlv : subContainers) {
                    if (!subContainerTlv.contains(0x06))
                        throw new MissingTagException(0x06);
                    if (!subContainerTlv.contains(0x07))
                        throw new MissingTagException(0x07);
                    if (!subContainerTlv.contains(0x08))
                        throw new MissingTagException(0x08);

                    WorkoutNumbers workoutNumber = new WorkoutNumbers();
                    workoutNumber.rawData = subContainerTlv.serialize();
                    workoutNumber.workoutNumber = subContainerTlv.getShort(0x06);
                    workoutNumber.dataCount = subContainerTlv.getShort(0x07);
                    workoutNumber.paceCount = subContainerTlv.getShort(0x08);
                    this.workoutNumbers.add(workoutNumber);
                }
            }
        }
    }

    public static class WorkoutTotals {
        public static final byte id = 0x08;

        public static class Request extends HuaweiPacket {

            public Request(SecretsProvider secretsProvider, short number) {
                super(secretsProvider);

                this.serviceId = Workout.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV().put(0x81, new HuaweiTLV()
                        .put(0x02, number)
                );

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public byte[] rawData;

            public short number;
            public byte status; // TODO: enum?
            public int startTime;
            public int endTime;
            public int calories;
            public int distance;
            public int stepCount;
            public int totalTime;
            public int duration;
            public byte type; // TODO: enum?

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (!this.tlv.contains(0x81))
                    throw new MissingTagException(0x81);

                HuaweiTLV container = this.tlv.getObject(0x81);

                if (!container.contains(0x02))
                    throw new MissingTagException(0x02);
                if (!container.contains(0x04))
                    throw new MissingTagException(0x04);
                if (!container.contains(0x05))
                    throw new MissingTagException(0x05);

                this.rawData = container.serialize();
                this.number = container.getShort(0x02);
                if (container.contains(0x03))
                    this.status = container.getByte(0x03);
                else
                    this.status = -1;
                this.startTime = container.getInteger(0x04);
                this.endTime = container.getInteger(0x05);

                if (container.contains(0x06))
                    this.calories = container.getInteger(0x06);
                if (container.contains(0x07))
                    this.distance = container.getInteger(0x07);
                if (container.contains(0x08))
                    this.stepCount = container.getInteger(0x08);
                if (container.contains(0x09))
                    this.totalTime = container.getInteger(0x09);
                if (container.contains(0x12))
                    this.duration = container.getInteger(0x12);
                if (container.contains(0x14))
                    this.type = container.getByte(0x14);
            }
        }
    }

    public static class WorkoutData {
        public static final int id = 0x0a;

        public static class Request extends HuaweiPacket {

            public Request(
                    SecretsProvider secretsProvider,
                    short workoutNumber,
                    short dataNumber
            ) {
                super(secretsProvider);

                this.serviceId = Workout.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV().put(0x81, new HuaweiTLV()
                        .put(0x02, workoutNumber)
                        .put(0x03, dataNumber)
                );

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public static class Header {
                public short workoutNumber;
                public short dataNumber;
                public int timestamp;
                public byte interval;
                public short dataCount;
                public byte dataLength;
                public short bitmap; // TODO: can this be enum-like?

                @Override
                public String toString() {
                    return "Header{" +
                            "workoutNumber=" + workoutNumber +
                            ", dataNumber=" + dataNumber +
                            ", timestamp=" + timestamp +
                            ", interval=" + interval +
                            ", dataCount=" + dataCount +
                            ", dataLength=" + dataLength +
                            ", bitmap=" + bitmap +
                            '}';
                }
            }

            public static class Data {
                // If unknown data is encountered, the whole tlv will be in here so it can be parsed again later
                public byte[] unknownData = null;

                public short speed = -1;

                public short cadence = -1;
                public short stepLength = -1;
                public short groundContactTime = -1;
                public byte impact = -1;
                public short swingAngle = -1;
                public byte foreFootLanding = -1;
                public byte midFootLanding = -1;
                public byte backFootLanding = -1;
                public byte eversionAngle = -1;

                public int timestamp = -1; // Calculated timestamp for this data point

                @Override
                public String toString() {
                    return "Data{" +
                            "unknownData=" + unknownData +
                            ", speed=" + speed +
                            ", cadence=" + cadence +
                            ", stepLength=" + stepLength +
                            ", groundContactTime=" + groundContactTime +
                            ", impact=" + impact +
                            ", swingAngle=" + swingAngle +
                            ", foreFootLanding=" + foreFootLanding +
                            ", midFootLanding=" + midFootLanding +
                            ", backFootLanding=" + backFootLanding +
                            ", eversionAngle=" + eversionAngle +
                            ", timestamp=" + timestamp +
                            '}';
                }
            }

            private final byte[] bitmapLengths = {1, 2, 1, 2, 2, 4, -1, 2};
            private final byte[] innerBitmapLengths = {2, 2, 2, 1, 2, 1, 1, 1, 1, 2, 2};

            public short workoutNumber;
            public short dataNumber;
            public byte[] rawHeader;
            public byte[] rawData;
            public short innerBitmap;

            public Header header;
            public List<Data> dataList;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (!this.tlv.contains(0x81))
                    throw new MissingTagException(0x81);

                HuaweiTLV container = this.tlv.getObject(0x81);

                if (!container.contains(0x02))
                    throw new MissingTagException(0x02);
                if (!container.contains(0x03))
                    throw new MissingTagException(0x03);
                if (!container.contains(0x04))
                    throw new MissingTagException(0x04);
                if (!container.contains(0x05))
                    throw new MissingTagException(0x05); // TODO: not sure if 5 can also be omitted

                this.workoutNumber = container.getShort(0x02);
                this.dataNumber = container.getShort(0x03);
                this.rawHeader = container.getBytes(0x04);
                this.rawData = container.getBytes(0x05);

                if (container.contains(0x09))
                    innerBitmap = container.getShort(0x09);
                else
                    innerBitmap = 0x01FF; // This seems to be the default

                int innerDataLength = 0;
                for (byte i = 0; i < 16; i++) {
                    if ((innerBitmap & (1 << i)) != 0) {
                        innerDataLength += innerBitmapLengths[i];
                    }
                }

                if (this.rawHeader.length != 14)
                    throw new LengthMismatchException("Workout data header length mismatch.");

                this.header = new Header();
                ByteBuffer buf = ByteBuffer.wrap(this.rawHeader);
                header.workoutNumber = buf.getShort();
                header.dataNumber = buf.getShort();
                header.timestamp = buf.getInt();
                header.interval = buf.get();
                header.dataCount = buf.getShort();
                header.dataLength = buf.get();
                header.bitmap = buf.getShort();

                // Check data lengths from header
                if (this.header.dataCount * this.header.dataLength != this.rawData.length)
                    throw new LengthMismatchException("Workout data length mismatch with header.");

                // Check data lengths from bitmap
                int dataLength = 0;
                for (byte i = 0; i < 16; i++) {
                    if ((header.bitmap & (1 << i)) != 0) {
                        if (i == 6) {
                            dataLength += innerDataLength;
                        } else {
                            dataLength += bitmapLengths[i];
                        }
                    }
                }
                dataLength = dataLength * header.dataCount;
                if (dataLength != this.rawData.length)
                    throw new LengthMismatchException("Workout data length mismatch with bitmap.");

                this.dataList = new ArrayList<>();
                buf = ByteBuffer.wrap(this.rawData);
                for (short i = 0; i < header.dataCount; i++) {
                    Data data = new Data();
                    data.timestamp = header.timestamp + header.interval * i;
                    outerDataLoop:
                    for (byte j = 0; j < 16; j++) {
                        if ((header.bitmap & (1 << j)) != 0) {
                            switch (j) {
                                case 1:
                                    data.speed = buf.getShort();
                                    break;
                                case 6:
                                    // Inner data, parsing into data
                                    // TODO: function for readability?
                                    for (byte k = 0; k < 16; k++) {
                                        if ((innerBitmap & (1 << k)) != 0) {
                                            switch (k) {
                                                case 0:
                                                    data.cadence = buf.getShort();
                                                    break;
                                                case 1:
                                                    data.stepLength = buf.getShort();
                                                    break;
                                                case 2:
                                                    data.groundContactTime = buf.getShort();
                                                    break;
                                                case 3:
                                                    data.impact = buf.get();
                                                    break;
                                                case 4:
                                                    data.swingAngle = buf.getShort();
                                                    break;
                                                case 5:
                                                    data.foreFootLanding = buf.get();
                                                    break;
                                                case 6:
                                                    data.midFootLanding = buf.get();
                                                    break;
                                                case 7:
                                                    data.backFootLanding = buf.get();
                                                    break;
                                                case 8:
                                                    data.eversionAngle = buf.get();
                                                    break;
                                                default:
                                                    data.unknownData = this.tlv.serialize();
                                                    break outerDataLoop;
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    data.unknownData = this.tlv.serialize();
                                    break outerDataLoop;
                            }
                        }
                    }
                    this.dataList.add(data);
                }
            }
        }
    }

    public static class WorkoutPace {
        public static final int id = 0x0c;

        public static class Request extends HuaweiPacket {

            public Request(
                    SecretsProvider secretsProvider,
                    short workoutNumber,
                    short paceNumber
            ) {
                super(secretsProvider);

                this.serviceId = Workout.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV().put(0x81, new HuaweiTLV()
                        .put(0x02, workoutNumber)
                        .put(0x08, paceNumber)
                );

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public static class Block {
                public short distance = -1;
                public byte type = -1;
                public int pace = -1;
                public short correction = 0;

                @Override
                public String toString() {
                    return "Block{" +
                            "distance=" + distance +
                            ", type=" + type +
                            ", pace=" + pace +
                            ", correction=" + correction +
                            '}';
                }
            }

            public short workoutNumber;
            public short paceNumber;
            public List<Block> blocks;

            public Response(SecretsProvider secretsProvider) {
                super(secretsProvider);
            }

            @Override
            protected void parseTlv() throws ParseException {
                if (!this.tlv.contains(0x81))
                    throw new MissingTagException(0x81);

                HuaweiTLV container = this.tlv.getObject(0x81);

                if (!container.contains(0x02))
                    throw new MissingTagException(0x02);
                if (!container.contains(0x08))
                    throw new MissingTagException(0x08);
                // TODO: not sure what happens with an empty workout here...
                if (!container.contains(0x83))
                    throw new MissingTagException(0x83);

                this.workoutNumber = container.getShort(0x02);
                this.paceNumber = container.getShort(0x08);

                this.blocks = new ArrayList<>();
                for (HuaweiTLV blockTlv : container.getObjects(0x83)) {
                    if (!blockTlv.contains(0x04))
                        throw new MissingTagException(0x04);
                    if (!blockTlv.contains(0x05))
                        throw new MissingTagException(0x05);
                    if (!blockTlv.contains(0x06))
                        throw new MissingTagException(0x06);

                    Block block = new Block();
                    block.distance = blockTlv.getShort(0x04);
                    block.type = blockTlv.getByte(0x05);
                    block.pace = blockTlv.getInteger(0x06);
                    if (blockTlv.contains(0x09))
                        block.correction = blockTlv.getShort(0x09);
                    blocks.add(block);
                }
            }
        }
    }
}
