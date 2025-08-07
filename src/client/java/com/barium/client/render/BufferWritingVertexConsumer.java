package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

/**
 * VERSÃO FINAL: Implementa a interface VertexConsumer exatamente como
 * ditado pelos erros de compilação.
 */
public class BufferWritingVertexConsumer implements VertexConsumer {

    private final BariumVertexFormat.Writer writer;
    private int normal; // Placeholder

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.writer = new BariumVertexFormat.Writer(buffer);
    }
    
    // Assinatura com floats, como exigido pelo compilador.
    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.writer.writePosNormal(x, y, z, this.normal);
        return this;
    }

    // Os métodos a seguir são padrão e corretos.
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
        // Ignorado por enquanto
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.writer.writeLight(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        // Ignorado por enquanto
        return this;
    }
    
    // O compilador nos disse que 'endVertex', 'fixedColor' e 'unfixColor' não existem.
    // Isso implica que o método para finalizar um vértice DEVE ser o 'next()'.
    @Override
    public void next() {
        this.writer.next();
    }
}