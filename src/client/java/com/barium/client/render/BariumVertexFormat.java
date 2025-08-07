package com.barium.client.render;

import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;

public final class BariumVertexFormat {

    // Nosso formato de vértice terá 16 bytes. Muito mais compacto que o do vanilla (~28-32 bytes).
    // - 4 bytes: Posição (X, Y, Z) e Normal (compactados)
    // - 4 bytes: Cor (R, G, B, A)
    // - 4 bytes: Textura (U, V)
    // - 4 bytes: Luz do Mapa (Block, Sky) e Overlay
    public static final int STRIDE = 16;

    // Classe auxiliar para escrever um vértice em um ByteBuffer.
    public static class Writer {
        private final ByteBuffer buffer;
        private long pointer;

        public Writer(ByteBuffer buffer) {
            this.buffer = buffer;
            this.pointer = MemoryUtil.memAddress(buffer);
        }

        // Posição: 10 bits para X, 10 para Y, 10 para Z. Normal: 2 bits. (total 32)
        public void writePosNormal(float x, float y, float z, int normal) {
            int ix = ((int)(x * 511.0f) & 0x3FF);
            int iy = ((int)(y * 511.0f) & 0x3FF);
            int iz = ((int)(z * 511.0f) & 0x3FF);
            int packed = (ix) | (iy << 10) | (iz << 20) | (normal << 30);
            MemoryUtil.memPutInt(pointer, packed);
            pointer += 4;
        }

        // Cor: 8 bits para cada canal (R, G, B, A)
        public void writeColor(float r, float g, float b, float a) {
            int ir = (int)(r * 255.0f) & 0xFF;
            int ig = (int)(g * 255.0f) & 0xFF;
            int ib = (int)(b * 255.0f) & 0xFF;
            int ia = (int)(a * 255.0f) & 0xFF;
            MemoryUtil.memPutInt(pointer, (ir) | (ig << 8) | (ib << 16) | (ia << 24));
            pointer += 4;
        }
        
        // Textura: 16 bits para U, 16 para V (como half-float)
        public void writeTexture(float u, float v) {
            // TODO: Implementar conversão para half-float para precisão.
            // Por simplicidade, vamos usar short por enquanto.
            short iu = (short)(u * 32767.0f);
            short iv = (short)(v * 32767.0f);
            MemoryUtil.memPutShort(pointer, iu);
            MemoryUtil.memPutShort(pointer + 2, iv);
            pointer += 4;
        }
        
        // Luz: 16 bits para luz do mapa, 16 para overlay
        public void writeLight(int light, int overlay) {
            MemoryUtil.memPutInt(pointer, (light) | (overlay << 16));
            pointer += 4;
        }

        public void next() {
            // O ponteiro já foi avançado, mas esta função pode ser usada para alinhar, se necessário.
        }
    }
}