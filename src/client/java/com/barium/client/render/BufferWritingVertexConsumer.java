package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

/**
 * Um VertexConsumer que não desenha nada, mas em vez disso, escreve
 * os dados dos vértices diretamente em um ByteBuffer usando nosso formato customizado.
 */
public class BufferWritingVertexConsumer implements VertexConsumer {

    private final BariumVertexFormat.Writer writer;
    private int normal;

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.writer = new BariumVertexFormat.Writer(buffer);
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        // A lógica de `vertex` é chamada primeiro, então guardamos os dados.
        // O `writer` avançará o ponteiro quando todos os dados forem escritos.
        // Por simplicidade, assumimos que a posição é relativa à seção (0-16).
        this.writer.writePosNormal((float) x, (float) y, (float) z, this.normal);
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
        // TODO: Mapear o overlay para nosso formato
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.writer.writeLight(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        // TODO: Compactar a normal em 2-3 bits e guardá-la.
        // this.normal = ...
        return this;
    }

    @Override
    public void next() {
        // O método `next()` do VertexConsumer indica que um vértice está completo.
        // No nosso caso, o writer já avançou o ponteiro, então não precisamos fazer nada.
        this.writer.next();
    }
}