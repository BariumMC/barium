package com.barium.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;

public final class BariumVertexFormat {
    // 3 floats (pos)   = 12 bytes
    // 4 bytes (color)  = 4 bytes
    // 2 floats (uv)    = 8 bytes
    // 4 bytes (light/overlay packed) = 4 bytes
    // 4 bytes (normal packed) = 4 bytes
    // TOTAL = 32 bytes
    public static final int STRIDE = 32;

    public static void setupAttributes() {
        GL20.glEnableVertexAttribArray(0); // Posição
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, STRIDE, 0);
        GL20.glEnableVertexAttribArray(1); // Cor
        GL20.glVertexAttribPointer(1, 4, GL11.GL_UNSIGNED_BYTE, true, STRIDE, 12);
        GL20.glEnableVertexAttribArray(2); // Textura
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, STRIDE, 16);
        GL20.glEnableVertexAttribArray(3); // Luz/Overlay
        GL20.glVertexAttribPointer(3, 2, GL11.GL_SHORT, false, STRIDE, 24);
        GL20.glEnableVertexAttribArray(4); // Normal
        GL20.glVertexAttribPointer(4, 3, GL11.GL_BYTE, true, STRIDE, 28);
    }

    public static void clearAttributes() {
        for (int i = 0; i < 5; i++) GL20.glDisableVertexAttribArray(i);
    }
    
    public static class Writer {
        private final ByteBuffer buffer;
        private long pointer;
        private float x, y, z, u, v;
        private int r, g, b, a, lightU, lightV, overlayU, overlayV;
        private float nx, ny, nz;

        public Writer(ByteBuffer buffer) {
            this.buffer = buffer;
            this.pointer = MemoryUtil.memAddress(buffer) + buffer.position();
        }

        public void setPos(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
        public void setColor(int r, int g, int b, int a) { this.r = r; this.g = g; this.b = b; this.a = a; }
        public void setUV(float u, float v) { this.u = u; this.v = v; }
        public void setLight(int u, int v) { this.lightU = u; this.lightV = v; }
        public void setOverlay(int u, int v) { this.overlayU = u; this.overlayV = v; }
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
            MemoryUtil.memPutShort(pointer + 24, (short) lightU);
            MemoryUtil.memPutShort(pointer + 26, (short) overlayU); // Overlay U, V
            MemoryUtil.memPutByte(pointer + 28, (byte)(nx * 127));
            MemoryUtil.memPutByte(pointer + 29, (byte)(ny * 127));
            MemoryUtil.memPutByte(pointer + 30, (byte)(nz * 127));
            pointer += STRIDE;
            buffer.position((int)(this.pointer - MemoryUtil.memAddress(this.buffer)));
        }
    }
}