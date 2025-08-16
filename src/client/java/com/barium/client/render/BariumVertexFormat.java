package com.barium.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class BariumVertexFormat {
    public static final int STRIDE = 32; // 32 bytes por vértice

    public static void setupAttributes() {
        // Posição (3 floats = 12 bytes)
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, STRIDE, 0);
        
        // Cor (4 unsigned bytes = 4 bytes)
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 4, GL11.GL_UNSIGNED_BYTE, true, STRIDE, 12);
        
        // Coordenadas de Textura (2 floats = 8 bytes)
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, STRIDE, 16);
        
        // Luz/Overlay (2 shorts = 4 bytes)
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 2, GL11.GL_SHORT, false, STRIDE, 24);
        
        // Normal (3 bytes = 3 bytes, +1 de padding = 4 bytes)
        GL20.glEnableVertexAttribArray(4);
        GL20.glVertexAttribPointer(4, 3, GL11.GL_BYTE, true, STRIDE, 28);
    }

    public static void clearAttributes() {
        for (int i = 0; i < 5; i++) {
            GL20.glDisableVertexAttribArray(i);
        }
    }
}