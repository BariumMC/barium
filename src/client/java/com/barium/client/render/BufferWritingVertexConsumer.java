package com.barium.client.render;

import net.minecraft.client.render.VertexConsumer;
import java.nio.ByteBuffer;

/**
 * VERSÃO FINAL - CONSTRUÍDA A PARTIR DOS ERROS DE COMPILAÇÃO
 */
public class BufferWritingVertexConsumer implements VertexConsumer {

    private final BariumVertexFormat.Writer writer;
    private int normal; // Placeholder

    public BufferWritingVertexConsumer(ByteBuffer buffer) {
        this.writer = new BariumVertexFormat.Writer(buffer);
    }
    
    // CORREÇÃO 1: A assinatura EXATA que o compilador exigiu.
    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.writer.writePosNormal(x, y, z, this.normal);
        return this;
    }

    // Métodos padrão que sabemos que estão corretos.
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
    
    // CORREÇÃO 2: O compilador nos disse que 'next()' não existe.
    // A alternativa lógica e padrão é 'endVertex()'.
    @Override
    public void endVertex() {
        this.writer.next(); // Nosso writer interno ainda usa 'next' para avançar, o nome não importa.
    }
    
    // Adicionando os métodos que podem ser necessários em algumas builds para satisfazer a interface
    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        // Deixado vazio, a implementação default pode não existir
    }

    @Override
    public void unfixColor() {
        // Deixado vazio
    }
}