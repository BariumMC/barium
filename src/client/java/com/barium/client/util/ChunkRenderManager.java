package com.barium.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkRenderManager {
    private static final ChunkRenderManager INSTANCE = new ChunkRenderManager();
    public static ChunkRenderManager getInstance() { return INSTANCE; }

    private Frustum frustum;

    /**
    Atualiza o frustum atual. Chamado a cada frame pelo WorldRendererMixin.
    */
    public void setFrustum(Frustum frustum) {
        this.frustum = frustum;
    }

    /**
    Verifica se um chunk está dentro do Frustum (campo de visão) da câmera.
    Esta implementação usa verificação direta de AABB (Box), que é extremamente rápida
    e não depende de grades pré-calculadas que podem bugar com a render distance.
    */
    public boolean isChunkInFrustum(int chunkX, int chunkZ) {
        // Se o frustum ainda não foi definido (ex: login), renderiza tudo por segurança.
        if (this.frustum == null) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return true;

        // Calcula as coordenadas do mundo real para o chunk
        double minX = chunkX * 16.0;
        double minZ = chunkZ * 16.0;
        double maxX = minX + 16.0;
        double maxZ = minZ + 16.0;

        // Pequena margem para evitar falsos positivos por precisão numérica na borda do frustum
        final double MARGIN = 1.0;
        minX -= MARGIN; minZ -= MARGIN;
        maxX += MARGIN; maxZ += MARGIN;

        // Obtém a altura do mundo para criar a caixa de colisão correta.
        // Usar a altura total evita que chunks sumam ao olhar muito para cima ou para baixo.
        double minY = client.world.getBottomY();
        double maxY = client.world.getHeight();

        // Verifica se a caixa (Box) do chunk intercepta o Frustum da câmera.
        boolean visible = frustum.isVisible(new Box(minX, minY, minZ, maxX, maxY, maxZ));
        if (visible) return true;

        // Fallback: assegura que chunks dentro da (possivelmente) efetiva render distance não sejam descartados
        int renderDistance = client.options.getViewDistance().getValue();
        int effective = com.barium.config.BariumConfig.C.EFFECTIVE_RENDER_DISTANCE > 0 ? com.barium.config.BariumConfig.C.EFFECTIVE_RENDER_DISTANCE : renderDistance;
        int playerChunkX = client.player.getChunkPos().x;
        int playerChunkZ = client.player.getChunkPos().z;
        int dx = Math.abs(chunkX - playerChunkX);
        int dz = Math.abs(chunkZ - playerChunkZ);

        if (dx <= effective && dz <= effective) {
            // Sempre mantém próximos ao jogador visíveis (raio 2)
            if (dx <= 2 && dz <= 2) return true;

            int sparse = Math.max(1, com.barium.config.BariumConfig.C.SPARSE_CHUNK_FACTOR);
            if (sparse <= 1) return true;

            // Renderiza apenas chunks alinhados à grade do fator esparso
            if (Math.floorMod(chunkX - playerChunkX, sparse) == 0 && Math.floorMod(chunkZ - playerChunkZ, sparse) == 0) {
                return true;
            }
        }

        return false; 
    }
    // Método mantido para compatibilidade, mas agora apenas reseta o frustum.
    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        this.setFrustum(frustum);
    }
    public void clear() {
        this.frustum = null;
    }
} 