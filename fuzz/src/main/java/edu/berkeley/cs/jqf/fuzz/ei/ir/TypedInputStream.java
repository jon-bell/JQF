package edu.berkeley.cs.jqf.fuzz.ei.ir;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class TypedInputStream extends InputStream {
    private Random random;
    private ZestGuidance.LinearInput input;
    private int bytesRead = 0;

    public TypedInputStream(ZestGuidance.LinearInput input, Random random) {
        super();
        this.input = input;
        this.random = random;
    }

    @Override
    public int read() throws IOException {
        throw new IOException("TypedInputStream does not support read()");
    }

    public TypedGeneratedValue readValue(TypedGeneratedValue.Type desiredType) throws IOException {
        TypedGeneratedValue ret = input.getOrGenerateFresh(bytesRead++, desiredType, random);
        return ret;
    }

    public double readDouble() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Double);
//        if(ret == null)
//            return -1;
        return ((TypedGeneratedValue.DoubleValue) ret).value;
    }

    public long readLong() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Long);
        if (ret == null)
            return -1;
        return ((TypedGeneratedValue.LongValue) ret).value;
    }

    public int readInt() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Integer);
//        if(ret == null)
//            return -1;
        return ((TypedGeneratedValue.IntegerValue) ret).value;
    }

    public byte readByte() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Byte);
//        if(ret == null)
//            return -1;
        return ((TypedGeneratedValue.ByteValue) ret).value;
    }

    public boolean readBoolean() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Boolean);

        return ((TypedGeneratedValue.BooleanValue) ret).value;
    }

    public String readString() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.String);
        return ((TypedGeneratedValue.StringValue) ret).value;
    }

    public char readChar() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Char);
//        if(ret == null)
//            return (char) -1;
        return ((TypedGeneratedValue.CharValue) ret).value;
    }

    public float readFloat() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Float);
//        if(ret == null)
//            return -1;
        return ((TypedGeneratedValue.FloatValue) ret).value;
    }

    public short readShort() throws IOException {
        TypedGeneratedValue ret = readValue(TypedGeneratedValue.Type.Short);
//        if(ret == null)
//            return -1;
        return ((TypedGeneratedValue.ShortValue) ret).value;
    }
}
