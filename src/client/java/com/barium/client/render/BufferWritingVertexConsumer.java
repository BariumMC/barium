package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

public class BufferWritingVertexConsumer implements VertexConsumer {

    private final BariumVertexFormat.Writer writer;
    private int normal;

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.writer = new BariumVertexFormat.Writer(buffer);
    }
    
    // CORREÇÃO: Usando a assinatura com float que o compilador exige.
    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.writer.writePosNormal(x, y, z, this.normal);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.writer.writeColor(red / 255.0f, green / 255.0f, blue / 255.0f, alpha / 255.0f);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.writer.writeTexture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this; // Ignored
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.writer.writeLight(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this; // Ignored
    }

    // CORREÇÃO: O nome do método é endVertex(), não next().
    @Override
    public void endVertex() {
        this.writer.next();
    }
    
    // Métodos adicionais exigidos pela interface em algumas versões
    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {}

    @Override
    public void unfixColor() {}
}