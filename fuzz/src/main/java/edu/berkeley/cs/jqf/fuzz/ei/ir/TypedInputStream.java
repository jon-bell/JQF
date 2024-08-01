package edu.berkeley.cs.jqf.fuzz.ei.ir;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class TypedInputStream extends InputStream {
    private Random random;
    private ZestGuidance.LinearInput input;
    private int bytesRead = 0;

    public enum Type {
        INVALID(0),
        Integer(4),
        Double(8),
        Long(8),
        Byte(1),
        Boolean(1),
        Float(4),
        Short(2);

        public final int bytes;

        Type(int bytes) {
            this.bytes = bytes;
        }

    }
    public TypedInputStream(ZestGuidance.LinearInput input, Random random) {
        super();
        this.input = input;
        this.random = random;
    }

    @Override
    public int read(byte[] b) throws IOException {
        throw new IOException("TypedInputStream does not support read()");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        throw new IOException("TypedInputStream does not support read()");
    }

    @Override
    public int read() throws IOException {
        throw new IOException("TypedInputStream does not support read()");
    }

    public double readDouble() throws IOException {
        double ret = input.getOrGenerateFreshDouble(bytesRead++, random);
        if(ret < 0)
            throw new GuidanceException("Negative double value generated");
        return ret;
    }

    public long readLong() throws IOException {
        return input.getOrGenerateFreshLong(bytesRead++, random);
    }

    public int readInt() throws IOException {
        return input.getOrGenerateFreshInt(bytesRead++, random);
    }

    public byte readByte() throws IOException {
        return input.getOrGenerateFreshByte(bytesRead++, random);
    }

    public boolean readBoolean() throws IOException {
        return input.getOrGenerateFreshBoolean(bytesRead++, random);
    }

    public float readFloat() throws IOException {
        return input.getOrGenerateFreshFloat(bytesRead++, random);
    }

    public short readShort() throws IOException {
        return input.getOrGenerateFreshShort(bytesRead++, random);
    }
}