package edu.berkeley.cs.jqf.fuzz.ei.ir;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class TypedInputStream extends InputStream {
    private Random random;
    private ZestGuidance.LinearInput input;
    private int bytesRead = 0;
    private int positionInInput;

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
        if (positionInInput >= input.size()) {
            if (ZestGuidance.GENERATE_EOF_WHEN_OUT) {
                throw new IllegalStateException(new EOFException("Reached end of input stream"));
            }
        }
        if (bytesRead > ZestGuidance.MAX_INPUT_SIZE)
            throw new IllegalStateException(new EOFException("Input too large"));
    }

    @Override
    public int read() throws IOException {
        throw new IOException("TypedInputStream does not support read()");
    }

    public double readDouble() throws IOException {
        checkForEOF();
        if (positionInInput < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Double) {
                double ret = input.getValues().getDouble();
                bytesRead += 8;
                positionInInput++;
                return ret;
            } else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Double);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Double);
                    positionInInput = idx;
                    input.numAlignments++;
                    double ret = input.getValues().getDouble();
                    return ret;
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                double ret = random.nextDouble();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(positionInInput);
                }
                input.getValues().reset();
                input.addDouble(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            double ret = random.nextDouble();
            input.addDouble(ret);
            return ret;
        }
    }

    public long readLong() throws IOException {
        checkForEOF();
        if (positionInInput < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Long) {
                long ret = input.getValues().getLong();
                bytesRead += 8;
                positionInInput++;
                return ret;
            } else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Long);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Long);
                    positionInInput = idx;
                    input.numAlignments++;
                    long ret = input.getValues().getLong();
                    return ret;
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                long ret = random.nextLong();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(positionInInput);
                }
                input.getValues().reset();
                input.addLong(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            long ret = random.nextLong();
            input.addLong(ret);
            return ret;
        }
    }

    private int getIdxOfNextValWithType(TypedGeneratedValue.Type type) {
        int maxScanPosition = Math.min(input.size()/9, input.position() + SCAN_FORWARD_LIMIT);
        for (int i = positionInInput + 1; i < maxScanPosition; i++) {
            TypedGeneratedValue.Type next = TypedGeneratedValue.Type.values()[input.getValues().get(i * 9)];
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
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(typ);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == typ);
                    positionInInput = idx;
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
                    input.clearAfter(positionInInput);
                }
                input.reset();
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
            return ret;
        }
    }

    public int readInt() throws IOException {
        return _readInt(TypedGeneratedValue.Type.Integer);
    }

    public byte readByte() throws IOException {
        checkForEOF();
        if (positionInInput < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Byte) {
                bytesRead += 1;
                positionInInput++;
                return input.getByte();
            } else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Byte);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Byte);
                    positionInInput = idx;
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
                    input.clearAfter(positionInInput);
                }
                input.getValues().reset();
                input.addByte(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            byte ret = (byte) random.nextInt(256);
            input.addByte(ret);
            return ret;
        }
    }

    public boolean readBoolean() throws IOException {
        checkForEOF();
        if (positionInInput < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Boolean) {
                bytesRead += 1;
                positionInInput++;
                return input.getBoolean();
            } else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Boolean);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Boolean);
                    positionInInput = idx;
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
                    input.clearAfter(positionInInput);
                }
                input.getValues().reset();
                input.addBoolean(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            boolean ret = random.nextBoolean();
            input.addBoolean(ret);
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
        if (positionInInput < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Char) {
                bytesRead += 2;
                positionInInput++;
                return input.getChar();
            } else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Char);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Char);
                    positionInInput = idx;
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
                    input.clearAfter(positionInInput);
                }
                input.getValues().reset();
                input.addChar(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            char ret = (char) random.nextInt();
            input.addChar(ret);
            return ret;
        }
    }

    public float readFloat() throws IOException {
        checkForEOF();
        if (positionInInput < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Float) {
                positionInInput++;
                return input.getFloat();
            } else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Float);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Float);
                    positionInInput = idx;
                    input.numAlignments++;
                    return input.getShort();
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                float ret = (float) random.nextFloat();
                if (input.misAlignmentsThisRun > SCAN_DISCARD_LIMIT) {
                    input.misAlignmentsThisRun = 0;
                    // We are misaligned too much, discard the rest of the input
                    input.clearAfter(positionInInput);
                }
                input.getValues().reset();
                input.addFloat(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            float ret = (float) random.nextFloat();
            input.addFloat(ret);
            return ret;
        }
    }

    public short readShort() throws IOException {
        checkForEOF();
        if (positionInInput < input.size()) {
            input.getValues().mark();
            TypedGeneratedValue.Type atPosition = input.nextType();
            if (atPosition == TypedGeneratedValue.Type.Short) {
                bytesRead += 4;
                positionInInput++;
                return input.getShort();
            } else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                input.misAlignmentsThisRun++;
                int idx = getIdxOfNextValWithType(TypedGeneratedValue.Type.Short);
                if (idx != -1) {
                    input.skip(positionInInput * 9, idx);
                    assert (input.nextType() == TypedGeneratedValue.Type.Short);
                    positionInInput = idx;
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
                    input.clearAfter(positionInInput);
                }
                input.getValues().reset();
                input.addShort(ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            short ret = (short) random.nextInt();
            input.addShort(ret);
            return ret;
        }
    }

    private static final int SCAN_FORWARD_LIMIT = 100;
    private static final int SCAN_DISCARD_LIMIT = 5;

    private TypedGeneratedValue readValue(TypedGeneratedValue.Type type) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
