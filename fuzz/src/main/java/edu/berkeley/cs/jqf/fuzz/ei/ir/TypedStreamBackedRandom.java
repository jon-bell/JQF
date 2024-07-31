package edu.berkeley.cs.jqf.fuzz.ei.ir;

import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.StreamBackedRandom;

import java.io.IOException;

public class TypedStreamBackedRandom extends StreamBackedRandom {
    private TypedInputStream inputStream;

    public TypedStreamBackedRandom(TypedInputStream inputStream, int bytesToIgnore) {
        super(inputStream, bytesToIgnore);
        this.inputStream = inputStream;
    }

    @Override
    public double nextDouble() {
        try {
            return inputStream.readDouble();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }
    public byte nextByte() {
        try {
            return inputStream.readByte();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }
    public char nextChar() {
        try {
            return inputStream.readChar();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }
    public int nextInt() {
        try {
            return inputStream.readInt();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }

    @Override
    public int nextInt(int bound) {
        if(bound <= 0){
            throw new IllegalArgumentException("bound must be positive");
        }
        return Math.abs(this.nextInt() % bound);
    }

    public long nextLong() {
        try {
            return inputStream.readLong();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }
    public boolean nextBoolean() {
        try {
            return inputStream.readBoolean();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }
    public float nextFloat() {
        try{
            return (float) inputStream.readFloat();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }

    @Override
    public short nextShort() {
        try{
            return  inputStream.readShort();
        } catch(IOException e){
            throw new GuidanceException(e);
        }
    }
}
