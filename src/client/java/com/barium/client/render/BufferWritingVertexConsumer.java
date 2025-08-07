package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

/**
 * VERSÃO CORRIGIDA: Implementa a interface VertexConsumer da 1.21.8
 */
public class BufferWritingVertexConsumer implements VertexConsumer {

    private final BariumVertexFormat.Writer writer;
    private int normal;

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.writer = new BariumVertexFormat.Writer(buffer);
    }

    // CORREÇÃO: A interface agora usa floats para o método vertex.
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
        // Ignoramos por enquanto
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.writer.writeLight(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        // TODO: Compactar a normal
        return this;
    }

    // CORREÇÃO: O método next() agora é `endVertex()`
    @Override
    public void endVertex() {
        this.writer.next();
    }

    // Métodos que precisam ser implementados, mas que podemos deixar vazios
    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        // Não usado por modelos de bloco
    }

    @Override
    public void unfixColor() {
        // Não usado por modelos de bloco
    }
}