package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

/**
 * VERSÃO FINALÍSSIMA - Mínima e em conformidade com o compilador.
 * Removemos o método 'next()' que o compilador rejeitou.
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

    // Métodos padrão que sabemos que existem e estão corretos.
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

    // O MÉTODO next() FOI REMOVIDO, POIS O COMPILADOR DISSE QUE ELE NÃO EXISTE NA INTERFACE.
    // Nosso writer interno já avança o ponteiro a cada chamada de escrita, então
    // funcionalmente, não perdemos nada.
}
