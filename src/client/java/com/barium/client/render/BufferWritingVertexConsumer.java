package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

public class BufferWritingVertexConsumer implements VertexConsumer {

    private final BariumVertexFormat.Writer writer;

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.writer = new BariumVertexFormat.Writer(buffer);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        writer.setPos(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        writer.setColor(red, green, blue, alpha);
        // Após a cor, todos os dados do vértice anterior foram definidos, então escrevemos.
        writer.writeAndAdvance();
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        writer.setUV(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        writer.setOverlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        writer.setLight(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        writer.setNormal(x, y, z);
        return this;
    }
    
    // CORREÇÃO: O método next() foi removido, pois o compilador provou que ele não existe na interface.
    // A lógica de escrita foi movida para o final da cadeia de chamadas (no método `color`).
}