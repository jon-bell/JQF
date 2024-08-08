package edu.berkeley.cs.jqf.fuzz.ei.ir;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class TypedInputStream extends InputStream {
    private Random random;
    private ZestGuidance.LinearInput input;
    private int bytesRead = 0;

    /*
    Plan:
    1. Migrate reading into this class from ZestGuidance.LinearInput
    2. Track "position" separately from bytesRead, allows for skipping
    3. Add skip-ahead functionality
    4. Add string nodes
     */
    public TypedInputStream(ZestGuidance.LinearInput input, Random random) {
        super();
        this.input = input;
        this.random = random;
    }

    private void checkForEOF() throws IOException {
        if (input.position() >= input.size()) {
            if (ZestGuidance.GENERATE_EOF_WHEN_OUT) {
                throw new IllegalStateException(new EOFException("Reached end of input stream"));
            }
        }
        if (input.position() + 9 > ZestGuidance.MAX_INPUT_SIZE)
            throw new IllegalStateException(new EOFException("Input too large"));
    }

    @Override
    public int read() throws IOException {
        throw new IOException("TypedInputStream does not support read()");
    }

    public double readDouble() throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Double) {
                return input.getDouble();
            } else {
                input.reset();
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Double);
                if (idx != -1) {
                    input.skipTo(idx);
                    if (input.nextType() != TypedGeneratedValue.Type.Double) {
                        throw new GuidanceException("Expected double, got " + input.nextType());
                    }
                    input.numAlignments++;
                    return input.getDouble();
                }
                double ret = random.nextDouble();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                input.addDouble(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            double ret = random.nextDouble();
            input.addDouble(ret);
            input.advance();
            return ret;
        }
    }

    public long readLong() throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Long) {
                long ret = input.getValues().getLong();
                return ret;
            } else {
                // Scan forward for the next value of the correct type
                input.reset();
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Long);
                if (idx != -1) {
                    input.skipTo(idx);
                    if (input.nextType() != TypedGeneratedValue.Type.Long) {
                        throw new GuidanceException("Expected long, got " + input.nextType());
                    }
                    input.numAlignments++;
                    return input.getValues().getLong();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                long ret = random.nextLong();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                input.addLong(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            long ret = random.nextLong();
            input.addLong(ret);
            input.advance();
            return ret;
        }
    }

    private int getIdxOfNextValWithType(TypedGeneratedValue.Type type) {
        int maxScanPosition = Math.min(input.size(),  input.position() + SCAN_FORWARD_LIMIT*9);//position in bytes!
        for (int i = input.position() + 9; i < maxScanPosition; i+=9) {
            TypedGeneratedValue.Type next = TypedGeneratedValue.Type.values()[input.getValues().get(i)];
            if(next == TypedGeneratedValue.Type.INVALID){
                throw new GuidanceException("Invalid type found in input stream");
            }
            if (next == type) {
                return i;
            }
        }
        return -1;
    }

    private int _readInt(TypedGeneratedValue.Type typ) throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == typ) {
                return input.getInt();
            } else {
                input.reset();
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(typ);
                if (idx != -1) {
                    input.skipTo(idx);
                    if (input.nextType() != typ) {
                        throw new GuidanceException("Expected " + typ + ", got " + input.nextType());
                    }
                    input.numAlignments++;
                    return input.getInt();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                int ret = random.nextInt();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                if (typ == TypedGeneratedValue.Type.Integer) {
                    input.addInt(ret);
                } else if (typ == TypedGeneratedValue.Type.String) {
                    input.addString(ret);
                } else {
                    throw new RuntimeException("Unsupported type");
                }
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            int ret = random.nextInt();
            if (typ == TypedGeneratedValue.Type.Integer) {
                input.addInt(ret);
            } else if (typ == TypedGeneratedValue.Type.String) {
                input.addString(ret);
            } else {
                throw new RuntimeException("Unsupported type");
            }
            input.advance();
            return ret;
        }
    }

    public int readInt() throws IOException {
        return _readInt(TypedGeneratedValue.Type.Integer);
    }

    public byte readByte() throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Byte) {
                return input.getByte();
            } else {
                input.reset();
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Byte);
                if (idx != -1) {
                    input.skipTo(idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Byte);
                    input.numAlignments++;
                    return input.getByte();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                byte ret = (byte) random.nextInt(256);
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                input.addByte(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            byte ret = (byte) random.nextInt(256);
            input.addByte(ret);
            input.advance();

            return ret;
        }
    }

    public boolean readBoolean() throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Boolean) {
                bytesRead += 1;
                return input.getBoolean();
            } else {
                input.reset();
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Boolean);
                if (idx != -1) {
                    input.skipTo(idx);
                    if(input.nextType() != TypedGeneratedValue.Type.Boolean){
                        throw new GuidanceException("Expected boolean, got " + input.nextType());
                    }
                    input.numAlignments++;
                    return input.getBoolean();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                boolean ret = random.nextBoolean();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                input.addBoolean(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            boolean ret = random.nextBoolean();
            input.addBoolean(ret);
            input.advance();
            return ret;
        }
    }

    public String readString(List<String> dictionary) throws IOException {
        checkForEOF();
        int ret = _readInt(TypedGeneratedValue.Type.String);
        return dictionary.get(Math.abs(ret % dictionary.size()));
    }

    public char readChar() throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Char) {
                bytesRead += 2;
                return input.getChar();
            } else {
                input.reset();
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Char);
                if (idx != -1) {
                    input.skipTo(idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Char);
                    input.numAlignments++;
                    return input.getChar();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                char ret = (char) random.nextInt();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                input.getValues().reset();
                input.addChar(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            char ret = (char) random.nextInt();
            input.addChar(ret);
            input.advance();

            return ret;
        }
    }

    public float readFloat() throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Float) {
                return input.getFloat();
            } else {
                input.reset();
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Float);
                if (idx != -1) {
                    input.skipTo(idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Float);
                    input.numAlignments++;
                    return input.getShort();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                float ret = random.nextFloat();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                input.addFloat(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            float ret = random.nextFloat();
            input.addFloat(ret);
            input.advance();

            return ret;
        }
    }

    public short readShort() throws IOException {
        checkForEOF();
        if (input.position() < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Short) {
                return input.getShort();
            } else {
                input.reset();
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Short);
                if (idx != -1) {
                    input.skipTo(idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Short);
                    input.numAlignments++;
                    return input.getShort();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                short ret = (short) random.nextInt();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(input.position());
                }
                input.addShort(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            short ret = (short) random.nextInt();
            input.addShort(ret);
            input.advance();

            return ret;
        }
    }

    private static final int SCAN_FORWARD_LIMIT = 100;
    private static final int SCAN_DISCARD_LIMIT = 5;

    private TypedGeneratedValue readValue(TypedGeneratedValue.Type type) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
