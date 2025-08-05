package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexBuffer; // CORREÇÃO: Voltando ao import que o compilador conhece.
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class VertexBufferUploader {

    /**
     * Lógica de upload otimizada.
     * Esta versão é chamada pelo nosso novo mixin no ChunkBuilder.
     */
    public static void upload(VertexBuffer buffer, BufferBuilder.DrawParameters drawParameters) {
        if (!BariumConfig.C.ENABLE_PERSISTENT_BUFFER_UPLOADING) {
            buffer.upload(drawParameters); // Fallback para o método vanilla
            return;
        }

        ByteBuffer data = drawParameters.buffer(); // CORREÇÃO: Acessando o ByteBuffer diretamente.
        if (data == null || data.remaining() == 0) {
            return;
        }

        buffer.bind();

        ByteBuffer mappedBuffer = GL44.glMapBufferRange(GL30.GL_ARRAY_BUFFER, 0, data.remaining(),
                GL44.GL_MAP_WRITE_BIT |
                GL44.GL_MAP_PERSISTENT_BIT |
                GL44.GL_MAP_FLUSH_EXPLICIT_BIT
        );

        if (mappedBuffer != null) {
            mappedBuffer.put(data);
            mappedBuffer.flip();
            GL44.glFlushMappedBufferRange(GL30.GL_ARRAY_BUFFER, 0, data.remaining());
            GL30.glUnmapBuffer(GL30.GL_ARRAY_BUFFER);
        } else {
            // Fallback se o mapeamento persistente não for suportado.
            buffer.upload(drawParameters);
        }

        VertexBuffer.unbind();
    }
}