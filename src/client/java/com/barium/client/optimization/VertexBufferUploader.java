package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.gl.VertexBuffer; // <-- CORREÇÃO: Import correto
import net.minecraft.client.render.BufferBuilder;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class VertexBufferUploader {

    /**
     * Tenta usar uma via de upload otimizada com buffers persistentes.
     * Se a otimização estiver desativada, ou se a GPU não suportar,
     * ele recorre ao método de upload padrão do Minecraft.
     */
    // CORREÇÃO: A assinatura do método usa o tipo de dados correto, `BufferBuilder.Buffer`.
    public static void upload(VertexBuffer buffer, BufferBuilder.Buffer data) {
        if (!BariumConfig.C.ENABLE_PERSISTENT_BUFFER_UPLOADING) {
            buffer.upload(data); // Usa o método vanilla se desativado.
            return;
        }

        // CORREÇÃO: Acessa o ByteBuffer através do método `byteBuffer()`.
        ByteBuffer chunkData = data.byteBuffer();
        if (chunkData == null || chunkData.remaining() == 0) {
            return; // Nada para enviar
        }

        buffer.bind(); // Garante que estamos operando no buffer correto.

        // "Mapeia" o buffer de forma persistente.
        ByteBuffer mappedBuffer = GL44.glMapBufferRange(GL30.GL_ARRAY_BUFFER, 0, chunkData.remaining(),
                GL44.GL_MAP_WRITE_BIT |
                GL44.GL_MAP_PERSISTENT_BIT |
                GL44.GL_MAP_FLUSH_EXPLICIT_BIT
        );

        if (mappedBuffer != null) {
            mappedBuffer.put(chunkData);
            mappedBuffer.flip();

            // Informa à GPU qual parte do buffer foi modificada.
            GL44.glFlushMappedBufferRange(GL30.GL_ARRAY_BUFFER, 0, chunkData.remaining());
            GL30.glUnmapBuffer(GL30.GL_ARRAY_BUFFER);
        } else {
            // Fallback: Se o mapeamento falhar, usa o método vanilla.
            buffer.upload(data);
        }

        VertexBuffer.unbind(); // Desvincula o buffer para evitar vazamentos de estado.
    }
}