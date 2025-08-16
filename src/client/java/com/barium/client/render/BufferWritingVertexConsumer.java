package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

public class BufferWritingVertexConsumer implements VertexConsumer {
    private final ByteBuffer buffer;
    private float x, y, z;
    private float u, v;
    private int light, overlay;
    private float nx, ny, nz;
    private int bytesWritten = 0;

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        buffer.putFloat(bytesWritten, x);
        buffer.putFloat(bytesWritten + 4, y);
        buffer.putFloat(bytesWritten + 8, z);
        buffer.put(bytesWritten + 12, (byte) red);
        buffer.put(bytesWritten + 13, (byte) green);
        buffer.put(bytesWritten + 14, (byte) blue);
        buffer.put(bytesWritten + 15, (byte) alpha);
        buffer.putFloat(bytesWritten + 16, u);
        buffer.putFloat(bytesWritten + 20, v);
        buffer.putInt(bytesWritten + 24, light);
        buffer.put(bytesWritten + 28, (byte)(nx * 127));
        buffer.put(bytesWritten + 29, (byte)(ny * 127));
        buffer.put(bytesWritten + 30, (byte)(nz * 127));
        
        bytesWritten += BariumVertexFormat.STRIDE;
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.u = u; this.v = v;
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        this.overlay = (v << 16) | (u & 0xFFFF);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.light = (v << 16) | (u & 0xFFFF);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.nx = x; this.ny = y; this.nz = z;
        return this;
    }
    
    public int getBytesWritten() {
        return this.bytesWritten;
    }
}