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
            if(ZestGuidance.GENERATE_EOF_WHEN_OUT){
                throw new IllegalStateException(new EOFException("Reached end of input stream"));
            }
        }
        if(bytesRead > ZestGuidance.MAX_INPUT_SIZE)
            throw new IllegalStateException(new EOFException("Input too large"));
    }

    @Override
    public int read() throws IOException {
        throw new IOException("TypedInputStream does not support read()");
    }

    public double readDouble() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Double);
        bytesRead += 8;
        positionInInput++;
        return ((TypedGeneratedValue.DoubleValue) ret).value;
    }

    public long readLong() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Long);
        bytesRead += 8;
        positionInInput++;
        return ((TypedGeneratedValue.LongValue) ret).value;
    }

    public int readInt() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Integer);
        bytesRead += 4;
        positionInInput++;
        return ((TypedGeneratedValue.IntegerValue) ret).value;
    }

    public byte readByte() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Byte);
        bytesRead++;
        positionInInput++;
        return ((TypedGeneratedValue.ByteValue) ret).value;
    }

    public boolean readBoolean() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Boolean);
        bytesRead++;
        positionInInput++;
        return ((TypedGeneratedValue.BooleanValue) ret).value;
    }

    public String readString(List<String> dictionary) throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.String);
        bytesRead+=4; //Historically JQF has counted strings as 4 bytes (an int into a dictionary)
        positionInInput++;
        return dictionary.get(Math.abs(((TypedGeneratedValue.StringValue) ret).keyNotBoundedBySize % dictionary.size()));
    }

    public char readChar() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Char);
        bytesRead += 2;
        positionInInput++;
        return ((TypedGeneratedValue.CharValue) ret).value;
    }

    public float readFloat() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Float);
        bytesRead += 4;
        positionInInput++;
        return ((TypedGeneratedValue.FloatValue) ret).value;
    }

    public short readShort() throws IOException {
        checkForEOF();
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Short);
        bytesRead += 2;
        positionInInput++;
        return ((TypedGeneratedValue.ShortValue) ret).value;
    }

    private static final int SCAN_FORWARD_LIMIT = 20;
    private TypedGeneratedValue readValue(TypedGeneratedValue.Type type){
        if(positionInInput < input.size()){
            TypedGeneratedValue ret = input.get(positionInInput);
            if(ret.type == type){
                return ret;
            }
            else {
                // Scan forward for the next value of the correct type
                input.misAlignments++;
                for(int i = 1; i < SCAN_FORWARD_LIMIT; i++){
                    if(positionInInput + i < input.size()){
                        TypedGeneratedValue next = input.get(positionInInput + i);
                        if(next.type == type){
                            input.skip(positionInInput, positionInInput + i);
                            positionInInput += i;
                            input.numAlignments++;
                            return next;
                        }
                    }
                }
                // If we reach here, we didn't find a value of the correct type
                // Generate a new value and insert it here in the input
                // TODO consider instead replacing the next value? experiment?
                ret = TypedGeneratedValue.generate(type, random);
                input.insert(positionInInput, ret);
                return ret;
            }
        } else {
            // We are at the end, generate a new value
            TypedGeneratedValue ret = TypedGeneratedValue.generate(type, random);
            input.add(ret);
            return ret;
        }

    }

}
