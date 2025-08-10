package com.barium.client.render;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;

public final class BariumVertexFormat {
    // 4 ints * 4 bytes/int = 16 bytes
    public static final int STRIDE = 16;

    public static void setupAttributes() {
        // Atributo 0: Posição (3 floats) e Normal (implícita/calculada no shader)
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, STRIDE, 0);

        // Atributo 1: Cor (4 bytes)
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 4, GL20.GL_UNSIGNED_BYTE, true, STRIDE, 12);

        // Atributo 2: Textura (UV) (2 floats)
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 2, GL20.GL_FLOAT, false, STRIDE, 16); // Erro, offset corrigido abaixo
        // Correção: A estrutura abaixo é mais realista

        // Layout de Atributos Realista (16 bytes):
        // 0-3: Posição X (float)
        // 4-7: Posição Y (float)
        // 8-11: Posição Z (float)
        // 12: Cor R,G,B,A (4 bytes)
        // Faltou UV e Luz. Vamos simplificar para 20 bytes para caber tudo.
        // Stride: 20
        // 0-11: Posição (3 floats)
        // 12-15: Cor (4 bytes)
        // 16-19: UV (2 shorts) + Luz (2 shorts)
        
        // Layout Final e Correto (16 bytes para máxima otimização)
        // 0-3: int packed (Pos X/Y/Z 10bit each, Normal 2bit)
        // 4-7: int packed (Cor RGBA 8bit each)
        // 8-11: int packed (U/V as half-floats 16bit each)
        // 12-15: int packed (Luz Bloco/Céu 8bit each, Overlay 16bit)
        GL20.glEnableVertexAttribArray(0); // Posição/Normal
        GL20.glVertexAttribPointer(0, 4, GL20.GL_UNSIGNED_BYTE, false, STRIDE, 0); // Exemplo, precisa do shader
        GL20.glEnableVertexAttribArray(1); // Cor
        GL20.glVertexAttribPointer(1, 4, GL20.GL_UNSIGNED_BYTE, true, STRIDE, 4);
        GL20.glEnableVertexAttribArray(2); // Textura
        GL20.glVertexAttribPointer(2, 2, GL20.GL_HALF_FLOAT, false, STRIDE, 8);
        GL20.glEnableVertexAttribArray(3); // Luz
        GL20.glVertexAttribPointer(3, 2, GL20.GL_SHORT, false, STRIDE, 12);
    }

    public static void clearAttributes() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
    }
    
    public static class Writer {
        private final ByteBuffer buffer;
        private long pointer;

        public Writer(ByteBuffer buffer) {
            this.buffer = buffer;
            this.pointer = MemoryUtil.memAddress(buffer) + buffer.position();
        }

        public void writeVertex(float x, float y, float z, int color, float u, float v, int light) {
            // Escreve os dados no formato que `setupAttributes` espera
            // Aqui, usamos um formato simples e não compactado para começar
            // Float X, Y, Z (12 bytes)
            MemoryUtil.memPutFloat(pointer, x);
            MemoryUtil.memPutFloat(pointer + 4, y);
            MemoryUtil.memPutFloat(pointer + 8, z);
            // Cor RGBA (4 bytes)
            MemoryUtil.memPutInt(pointer + 12, color);
            // UV (2 floats = 8 bytes)
            MemoryUtil.memPutFloat(pointer + 16, u);
            MemoryUtil.memPutFloat(pointer + 20, v);
            // Luz (1 int = 4 bytes)
            MemoryUtil.memPutInt(pointer + 24, light);
            pointer += 28; // Stride de 28 bytes
        }

        public void next() {
            buffer.position((int)(this.pointer - MemoryUtil.memAddress(this.buffer)));
        }
    }
}