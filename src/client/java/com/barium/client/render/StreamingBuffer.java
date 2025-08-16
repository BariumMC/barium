package com.barium.client.render;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;

public class StreamingBuffer {
    private final int vbo;
    public final long capacity;
    private long writeOffset = 0;
    private final ByteBuffer mappedBuffer;

    public StreamingBuffer(long capacity) {
        this.capacity = capacity;
        this.vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        
        int flags = GL44.GL_MAP_WRITE_BIT | GL44.GL_MAP_PERSISTENT_BIT;
        GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, capacity, flags);
        
        this.mappedBuffer = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0, capacity, flags | GL44.GL_MAP_FLUSH_EXPLICIT_BIT);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        if (this.mappedBuffer == null) {
            throw new RuntimeException("Falha ao mapear o StreamingBuffer.");
        }
    }
    
    public Region alloc(int size) {
        if (size > this.capacity) return null; // Não pode alocar mais que a capacidade total
        if (this.writeOffset + size > this.capacity) {
            this.writeOffset = 0; // Volta para o início (efeito de buffer circular)
        }
        Region region = new Region(this.writeOffset, size);
        this.writeOffset += size;
        return region;
    }
    
    public void upload(Region region, ByteBuffer data) {
        if (this.mappedBuffer == null) return;
        this.mappedBuffer.position((int) region.offset);
        this.mappedBuffer.put(data);
        GL30.glFlushMappedBufferRange(GL15.GL_ARRAY_BUFFER, region.offset, region.size);
    }

    public void bind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
    }

    public void unbind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    
    public void delete() {
        GL15.glDeleteBuffers(this.vbo);
    }

    public record Region(long offset, int size) {
        public int getVertexCount() {
            return BariumVertexFormat.STRIDE > 0 ? size / BariumVertexFormat.STRIDE : 0;
        }
    }
}