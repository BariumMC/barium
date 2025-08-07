package com.barium.client.render;

import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Representa um único e grande Vertex Buffer Object (VBO) na GPU que é gerenciado
 * como uma arena de alocação. Em vez de criar um VBO para cada chunk, nós
 * alocamos "regiões" dentro deste buffer gigante.
 *
 * Utiliza o mapeamento de buffer persistente (OpenGL 4.4+) para permitir
 * que a CPU escreva diretamente na memória da GPU com sobrecarga mínima,
 * eliminando a necessidade de chamadas de upload como glBufferSubData.
 */
public class StreamingBuffer {

    private final int vbo;
    /**
     * A capacidade total do buffer em bytes. É pública para que outros
     * managers possam consultá-la (ex: para logs ou decisões de alocação).
     */
    public final long capacity;
    
    // O offset atual para a próxima alocação. Move-se linearmente pelo buffer.
    private long writeOffset = 0;

    // Um ByteBuffer que aponta diretamente para a memória do VBO na GPU.
    // Qualquer escrita neste buffer é refletida imediatamente na GPU.
    private final ByteBuffer mappedBuffer;

    /**
     * Cria um novo buffer de streaming com uma capacidade especificada.
     * @param capacity A capacidade do buffer em bytes.
     */
    public StreamingBuffer(long capacity) {
        this.capacity = capacity;
        this.vbo = GL30C.glGenBuffers();
        
        // Faz o "bind" do buffer para que possamos configurá-lo.
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, this.vbo);

        // Aloca a memória na GPU. As 'flags' são a chave para a alta performance.
        int flags = GL44C.GL_MAP_WRITE_BIT          // Nós vamos escrever neste buffer.
                  | GL44C.GL_MAP_PERSISTENT_BIT   // O ponteiro do buffer será válido por toda a vida do buffer.
                  | GL44C.GL_MAP_COHERENT_BIT;    // As escritas da CPU são visíveis pela GPU sem barreiras explícitas.
        GL44C.glBufferStorage(GL30C.GL_ARRAY_BUFFER, capacity, flags);

        // Mapeia a memória do buffer da GPU para o nosso ByteBuffer na CPU.
        this.mappedBuffer = GL44C.glMapBufferRange(GL30C.GL_ARRAY_BUFFER, 0, capacity, flags);
        
        // Desfaz o bind para não afetar outras operações OpenGL.
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, 0);

        if (this.mappedBuffer == null) {
            throw new RuntimeException("Falha ao mapear o StreamingBuffer. " + 
                "Seu sistema pode não ter suporte a OpenGL 4.4+. " +
                "Verifique os drivers da placa de vídeo.");
        }
    }

    /**
     * Aloca uma região de um determinado tamanho dentro do buffer.
     * @param size O tamanho em bytes da região a ser alocada.
     * @return um objeto Region que representa a fatia alocada.
     */
    public Region alloc(int size) {
        // Se a alocação for exceder a capacidade, reiniciamos o offset para o início.
        // Este é um método simples chamado "orfaning". Uma implementação mais robusta
        // usaria vários buffers ou "fences" OpenGL para garantir que a GPU não esteja
        // lendo os dados que estamos prestes a sobrescrever.
        if (this.writeOffset + size > this.capacity) {
            // TODO: Inserir barreira de sincronização (glFenceSync) antes de reiniciar.
            this.writeOffset = 0;
        }

        Region region = new Region(this.writeOffset, size);
        this.writeOffset += size;
        return region;
    }

    /**
     * Escreve dados de um ByteBuffer da CPU para uma região específica do buffer da GPU.
     * @param region A região de destino no buffer da GPU.
     * @param data O ByteBuffer com os dados de vértice a serem escritos.
     */
    public void upload(Region region, ByteBuffer data) {
        MemoryUtil.memCopy(
                MemoryUtil.memAddress(data), // Endereço de origem (CPU)
                MemoryUtil.memAddress(this.mappedBuffer) + region.offset, // Endereço de destino (GPU)
                data.remaining() // Quantidade de bytes a copiar
        );
    }

    /**
     * Faz o bind deste buffer ao contexto OpenGL atual, preparando-o para ser usado
     * em uma chamada de desenho (como glDrawArrays).
     */
    public void bind() {
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, this.vbo);
    }

    /**
     * Libera os recursos do buffer (o VBO) quando não for mais necessário (ex: ao fechar o jogo).
     */
    public void delete() {
        GL30C.glDeleteBuffers(this.vbo);
    }

    /**
     * Um registro que representa uma fatia (região) alocada dentro do StreamingBuffer.
     * Contém o ponto inicial (offset) e o tamanho da fatia.
     */
    public record Region(long offset, int size) {
        /**
         * @return O número de vértices que podem ser armazenados nesta região,
         * com base no tamanho do nosso formato de vértice customizado.
         */
        public int getVertexCount() {
            // Garante que não haverá divisão por zero se o stride não for definido.
            return BariumVertexFormat.STRIDE > 0 ? size / BariumVertexFormat.STRIDE : 0;
        }
    }
}