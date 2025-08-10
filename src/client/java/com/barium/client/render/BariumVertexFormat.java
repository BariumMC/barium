package com.barium.client.render;

import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;

public final class BariumVertexFormat {
    // 3 floats (pos)   = 12 bytes
    // 4 bytes (color)  = 4 bytes
    // 2 floats (uv)    = 8 bytes
    // 4 bytes (light/overlay) = 4 bytes
    // 4 bytes (normal) = 4 bytes
    // TOTAL = 32 bytes
    public static final int STRIDE = 32;

    public static class Writer {
        private final ByteBuffer buffer;
        private long pointer;
        private float x, y, z, u, v;
        private int r, g, b, a, light, overlay;
        private float nx, ny, nz;

        public Writer(ByteBuffer buffer) {
            this.buffer = buffer;
            this.pointer = MemoryUtil.memAddress(buffer) + buffer.position();
        }

        public void setPos(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
        public void setColor(int r, int g, int b, int a) { this.r = r; this.g = g; this.b = b; this.a = a; }
        public void setUV(float u, float v) { this.u = u; this.v = v; }
        public void setLight(int light) { this.light = light; }
        public void setOverlay(int overlay) { this.overlay = overlay; }
        public void setNormal(float nx, float ny, float nz) { this.nx = nx; this.ny = ny; this.nz = nz; }

        public void writeAndAdvance() {
            MemoryUtil.memPutFloat(pointer, x);
            MemoryUtil.memPutFloat(pointer + 4, y);
            MemoryUtil.memPutFloat(pointer + 8, z);
            MemoryUtil.memPutByte(pointer + 12, (byte) r);
            MemoryUtil.memPutByte(pointer + 13, (byte) g);
            MemoryUtil.memPutByte(pointer + 14, (byte) b);
            MemoryUtil.memPutByte(pointer + 15, (byte) a);
            MemoryUtil.memPutFloat(pointer + 16, u);
            MemoryUtil.memPutFloat(pointer + 20, v);
            MemoryUtil.memPutInt(pointer + 24, light);
            MemoryUtil.memPutInt(pointer + 28, overlay); // Simplificado
            pointer += STRIDE;
            buffer.position((int)(this.pointer - MemoryUtil.memAddress(this.buffer)));
        }
    }
}