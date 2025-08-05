package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexBuffer;
import org.lwjgl.opengl.GL44; // Precisamos de OpenGL 4.4 para persistent mapping
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class VertexBufferUploader {

    /**
     * Tenta usar uma via de upload otimizada com buffers persistentes.
     * Se a otimização estiver desativada, ou se a GPU não suportar,
     * ele recorre ao método de upload padrão do Minecraft.
     */
    public static void upload(VertexBuffer buffer, BufferBuilder.RenderBuffer data) {
        if (BariumConfig.C.ENABLE_PERSISTENT_BUFFER_UPLOADING) {
            // "Mapeia" o buffer de forma persistente. A CPU recebe um ponteiro direto
            // para a memória da GPU e pode escrever nele sem bloqueios.
            // GL_MAP_PERSISTENT_BIT: Garante que o ponteiro é válido mesmo enquanto a GPU o utiliza.
            // GL_MAP_WRITE_BIT: Indica que vamos escrever dados.
            // GL_MAP_FLUSH_EXPLICIT_BIT: Nos dá controle manual sobre quando "confirmar" os dados.
            ByteBuffer mappedBuffer = GL44.glMapBufferRange(GL30.GL_ARRAY_BUFFER, 0, data.getByteBuffer().remaining(),
                    GL44.GL_MAP_WRITE_BIT |
                    GL44.GL_MAP_PERSISTENT_BIT |
                    GL44.GL_MAP_FLUSH_EXPLICIT_BIT
            );

            if (mappedBuffer != null) {
                // Copia os dados do chunk diretamente para a memória da GPU.
                mappedBuffer.put(data.getByteBuffer());
                mappedBuffer.flip(); // Prepara o buffer para ser lido pela GPU.

                // Informa à GPU qual parte do buffer foi modificada e precisa ser "vista".
                GL44.glFlushMappedBufferRange(GL30.GL_ARRAY_BUFFER, 0, data.getByteBuffer().remaining());
                GL30.glUnmapBuffer(GL30.GL_ARRAY_BUFFER); // Desmapeia, mas o link persiste.
                return; // Sucesso!
            }
        }
        
        // Fallback: Se a otimização estiver desligada ou falhar, usa o método vanilla.
        buffer.upload(data);
    }
}