package edu.berkeley.cs.jqf.fuzz.ei.ir;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

public abstract class TypedGeneratedValue implements Serializable {
    private static final long serialVersionUID = 1L;
    public Type type;

    public static TypedGeneratedValue readOneValue(DataInputStream dis) throws IOException {
        Type type = Type.valueOf(dis.readUTF());
        switch (type) {
            case String:
                return new StringValue(dis.readInt());
            case Integer:
                return new IntegerValue(dis.readInt());
            case Float:
                return new FloatValue(dis.readFloat());
            case Boolean:
                return new BooleanValue(dis.readBoolean());
            case Byte:
                return new ByteValue(dis.readByte());
            case Long:
                return new LongValue(dis.readLong());
            case Double:
                return new DoubleValue(dis.readDouble());
            case Short:
                return new ShortValue(dis.readShort());
            case Char:
                return new CharValue(dis.readChar());
            default:
                throw new RuntimeException("Unknown type");
        }
    }

    public void writeTo(DataOutputStream out) {
        try {
            out.writeUTF(type.toString());
            switch (type) {
                case String:
                    out.writeInt(((StringValue) this).keyNotBoundedBySize);
                    break;
                case Integer:
                    out.writeInt(((IntegerValue) this).value);
                    break;
                case Float:
                    out.writeFloat(((FloatValue) this).value);
                    break;
                case Boolean:
                    out.writeBoolean(((BooleanValue) this).value);
                    break;
                case Byte:
                    out.writeByte(((ByteValue) this).value);
                    break;
                case Long:
                    out.writeLong(((LongValue) this).value);
                    break;
                case Double:
                    out.writeDouble(((DoubleValue) this).value);
                    break;
                case Short:
                    out.writeShort(((ShortValue) this).value);
                    break;
                case Char:
                    out.writeChar(((CharValue) this).value);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum Type {
        String, Integer, Float, Boolean, Byte, Long, Double, Short, Char;
    }
    private static final int MAX_STRING_LENGTH = 20;
//    public static TypedGeneratedValue generateString(Random random, List<String> dictionary){
////        boolean useDictionary = random.nextBoolean();
////        if(useDictionary){
//            return new StringValue(dictionary.get(random.nextInt(dictionary.size())), dictionary);
////        } else {
////            int length = random.nextInt(20);
////            StringBuilder sb = new StringBuilder();
////            for (int i = 0; i < length; i++) {
////                sb.append((char) random.nextInt(55295));
////            }
////            return new StringValue(sb.toString(), dictionary);
////        }
//    }
    public static TypedGeneratedValue generate(Type desiredType, Random random) {
        switch (desiredType) {
            case String:
                return new StringValue(Math.abs(random.nextInt()));
            case Integer:
                return new IntegerValue(random.nextInt());
            case Float:
                return new FloatValue(random.nextFloat());
            case Boolean:
                return new BooleanValue(random.nextBoolean());
            case Byte:
                return new ByteValue((byte) random.nextInt());
            case Long:
                return new LongValue(random.nextLong());
            case Double:
                return new DoubleValue(random.nextDouble());
            case Short:
                return new ShortValue((short) random.nextInt(Short.MAX_VALUE));
            default:
                throw new RuntimeException("Unknown type");
        }
    }

    private TypedGeneratedValue(Type type) {
        this.type = type;
    }

    public static class ShortValue extends TypedGeneratedValue {
        public final short value;

        public ShortValue(short value) {
            super(Type.Short);
            this.value = value;
        }

    }

    public static class CharValue extends TypedGeneratedValue {
        public final char value;

        public CharValue(char value) {
            super(Type.Char);
            this.value = value;
        }

    }

    public static class LongValue extends TypedGeneratedValue {
        public final long value;

        public LongValue(long value) {
            super(Type.Long);
            this.value = value;
        }

    }

    public static class DoubleValue extends TypedGeneratedValue {
        public final double value;

        public DoubleValue(double value) {
            super(Type.Double);
            this.value = value;
        }

        @Override
        public String toString() {
            return "DoubleValue{" +
                    "value=" + value +
                    '}';
        }

    }

    public static class StringValue extends TypedGeneratedValue {
        public final int keyNotBoundedBySize;

        public StringValue(int keyNotBoundedBySize) {
            super(Type.String);
            this.keyNotBoundedBySize = keyNotBoundedBySize;
        }
    }

    public static class IntegerValue extends TypedGeneratedValue {
        public final int value;

        public IntegerValue(int value) {
            super(Type.Integer);
            this.value = value;
        }
    }

    public static class FloatValue extends TypedGeneratedValue {
        public final float value;

        public FloatValue(float value) {
            super(Type.Float);
            this.value = value;
        }
    }

    public static class BooleanValue extends TypedGeneratedValue {
        public final boolean value;

        public BooleanValue(boolean value) {
            super(Type.Boolean);
            this.value = value;
        }
    }

    public static class ByteValue extends TypedGeneratedValue {
        public final byte value;

        public ByteValue(byte value) {
            super(Type.Byte);
            this.value = value;
        }

    }
}

