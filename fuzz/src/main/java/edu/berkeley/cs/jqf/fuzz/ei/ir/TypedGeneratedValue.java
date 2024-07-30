package edu.berkeley.cs.jqf.fuzz.ei.ir;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.io.DataOutputStream;
import java.io.Serializable;
import java.util.Random;

public abstract class TypedGeneratedValue implements Serializable {
    private static final long serialVersionUID = 1L;
    public Type type;

    public void writeTo(DataOutputStream out) {
        try {
            out.writeUTF(type.name());
            switch (type) {
                case String:
                    out.writeUTF(((StringValue) this).value);
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
            out.writeChar('\n');
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum Type {
        String, Integer, Float, Boolean, Byte, Long, Double, Short, Char;
    }

    public static TypedGeneratedValue generate(Type desiredType, Random random) {
        switch (desiredType) {
            case String:
                int length = random.nextInt(10);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    sb.append((char) random.nextInt(55295));
                }
                return new StringValue(sb.toString());
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

    public abstract void mutate(Random random, boolean setToZero);

    public static class ShortValue extends TypedGeneratedValue {
        public short value;

        public ShortValue(short value) {
            super(Type.Short);
            this.value = value;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = 0;
                return;
            }
            value = (short) (random.nextInt(2 * Short.MAX_VALUE) - Short.MAX_VALUE);
        }
    }

    public static class CharValue extends TypedGeneratedValue {
        public char value;

        public CharValue(char value) {
            super(Type.Char);
            this.value = value;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = 0;
                return;
            }
            value = (char) (random.nextInt(2 * Character.MAX_VALUE) - Character.MAX_VALUE);
        }
    }

    public static class LongValue extends TypedGeneratedValue {
        public long value;

        public LongValue(long value) {
            super(Type.Long);
            this.value = value;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = 0;
                return;
            }
            value = random.nextLong();
        }
    }

    public static class DoubleValue extends TypedGeneratedValue {
        public double value;

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

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = 0;
                return;
            }
//            value = Double.longBitsToDouble(random.nextLong());
            value = random.nextDouble();
        }
    }

    public static class StringValue extends TypedGeneratedValue {
        public String value;

        public StringValue(String value) {
            super(Type.String);
            this.value = value;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = "";
                return;
            }
            int index = random.nextInt(value.length());
            byte[] chars = value.getBytes();
            chars[index] = (byte) random.nextInt(255);
            value = new String(chars);
        }
    }

    public static class IntegerValue extends TypedGeneratedValue {
        public int value;
        public int bound;

        public IntegerValue(int value) {
            super(Type.Integer);
            this.value = value;
        }

        public IntegerValue(int value, int bound) {
            super(Type.Integer);
            this.value = value;
            this.bound = bound;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = 0;
                return;
            }
            if (bound != 0) {
                value = random.nextInt(bound);
            } else {
                value = random.nextInt();
            }
        }
    }

    public static class FloatValue extends TypedGeneratedValue {
        public float value;

        public FloatValue(float value) {
            super(Type.Float);
            this.value = value;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = 0;
                return;
            }
            value = Float.intBitsToFloat(random.nextInt());
//            value = random.nextFloat();
        }
    }

    public static class BooleanValue extends TypedGeneratedValue {
        public boolean value;

        public BooleanValue(boolean value) {
            super(Type.Boolean);
            this.value = value;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = false;
                return;
            }
            value = random.nextBoolean();
        }
    }

    public static class ByteValue extends TypedGeneratedValue {
        public byte value;

        public ByteValue(byte value) {
            super(Type.Byte);
            this.value = value;
        }

        @Override
        public void mutate(Random random, boolean setToZero) {
            if (setToZero) {
                value = 0;
                return;
            }
            value = (byte) random.nextInt();
        }
    }
}

