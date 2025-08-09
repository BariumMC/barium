package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

/**
 * VERSÃO FINAL E MÍNIMA - Obedecendo ao compilador.
 * Contém apenas os métodos que a interface VertexConsumer exige,
 * sem nenhum extra que cause erros de @Override.
 */
public class BufferWritingVertexConsumer implements VertexConsumer {

    private final BariumVertexFormat.Writer writer;
    private int normal; // Placeholder

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.writer = new BariumVertexFormat.Writer(buffer);
    }

    // A assinatura com float, que o compilador exigiu.
    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.writer.writePosNormal(x, y, z, this.normal);
        return this;
    }

    // Os métodos a seguir são padrão e nunca deram erro.
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
        return this; // Ignorado
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.writer.writeLight(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this; // Ignorado
    }

    // O método 'next()' é o único método de finalização que resta.
    // O compilador rejeitou 'endVertex'.
    @Override
    public void next() {
        this.writer.next();
    }
}
