package com.barium.client.render;

import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;

public class StreamingBuffer {

    private final int vbo;
    private final long capacity;
    private long writeOffset = 0;
    
    // Usaremos mapeamento persistente para uploads de alta performance.
    private final ByteBuffer mappedBuffer;

    public StreamingBuffer(long capacity) {
        this.capacity = capacity;
        this.vbo = GL30C.glGenBuffers();
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, this.vbo);

        // Aloca o buffer na GPU com mapeamento persistente e coerente.
        // Isso permite que a CPU escreva diretamente na memória da GPU sem chamadas de upload explícitas.
        int flags = GL44C.GL_MAP_WRITE_BIT | GL44C.GL_MAP_PERSISTENT_BIT | GL44C.GL_MAP_COHERENT_BIT;
        GL44C.glBufferStorage(GL30C.GL_ARRAY_BUFFER, capacity, flags);

        this.mappedBuffer = GL44C.glMapBufferRange(GL30C.GL_ARRAY_BUFFER, 0, capacity, flags);
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, 0);

        if (this.mappedBuffer == null) {
            throw new RuntimeException("Falha ao mapear o StreamingBuffer. Verifique o suporte a OpenGL 4.4+.");
        }
    }
    
    public Region alloc(int size) {
        // Se não houver espaço, reinicia o buffer. (Isso é chamado de "orfaning")
        // Uma implementação real precisaria de sincronização (fences) para não sobrescrever dados em uso.
        if (this.writeOffset + size > this.capacity) {
            this.writeOffset = 0;
            // TODO: Inserir uma barreira de sincronização aqui.
        }

        Region region = new Region(this.writeOffset, size);
        this.writeOffset += size;
        return region;
    }
    
    public void upload(Region region, ByteBuffer data) {
        MemoryUtil.memCopy(MemoryUtil.memAddress(data), MemoryUtil.memAddress(this.mappedBuffer) + region.offset, data.remaining());
    }

    public void bind() {
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, this.vbo);
    }
    
    public void delete() {
        GL30C.glDeleteBuffers(this.vbo);
    }

    // Representa uma fatia do nosso buffer gigante.
    public record Region(long offset, int size) {
        public int getVertexCount() {
            return size / BariumVertexFormat.STRIDE;
        }
    }
}