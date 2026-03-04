package com.barium.client.util;

import com.barium.config.BariumConfig;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;

public class ChunkRenderManager {
    private static final ChunkRenderManager INSTANCE = new ChunkRenderManager();
    public static ChunkRenderManager getInstance() { return INSTANCE; }

    private Frustum frustum;
    private long frameId = 0L;

    private final Long2BooleanOpenHashMap chunkVisibilityCache = new Long2BooleanOpenHashMap();
    private final Long2BooleanOpenHashMap sectionVisibilityCache = new Long2BooleanOpenHashMap();

    /**
    Atualiza o frustum atual. Chamado a cada frame pelo WorldRendererMixin.
    */
    public void setFrustum(Frustum frustum) {
        this.frustum = frustum;
    }

    public void beginFrame() {
        frameId++;
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE) {
            chunkVisibilityCache.clear();
            sectionVisibilityCache.clear();
        }
    }

    /**
    Verifica se um chunk está dentro do Frustum (campo de visão) da câmera.
    Esta implementação usa verificação direta de AABB (Box), que é extremamente rápida
    e não depende de grades pré-calculadas que podem bugar com a render distance.
    */
    public boolean isChunkInFrustum(int chunkX, int chunkZ) {
        long cacheKey = packChunkKey(chunkX, chunkZ);
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE && chunkVisibilityCache.containsKey(cacheKey)) {
            return chunkVisibilityCache.get(cacheKey);
        }

        boolean visible = computeChunkInFrustum(chunkX, chunkZ);
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE) {
            chunkVisibilityCache.put(cacheKey, visible);
        }

        return visible;
    }

    public boolean isSectionInFrustum(int sectionX, int sectionY, int sectionZ) {
        long cacheKey = packSectionKey(sectionX, sectionY, sectionZ);
        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE && sectionVisibilityCache.containsKey(cacheKey)) {
            return sectionVisibilityCache.get(cacheKey);
        }

        boolean visible;
        if (this.frustum == null) {
            visible = true;
        } else {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) {
                visible = true;
            } else {
                double minX = sectionX * 16.0;
                double minY = sectionY * 16.0;
                double minZ = sectionZ * 16.0;
                double maxX = minX + 16.0;
                double maxY = minY + 16.0;
                double maxZ = minZ + 16.0;
                visible = frustum.isVisible(new Box(minX, minY, minZ, maxX, maxY, maxZ));
            }
        }

        if (BariumConfig.C.ENABLE_FRAME_VISIBILITY_CACHE) {
            sectionVisibilityCache.put(cacheKey, visible);
        }

        return visible;
    }

    private boolean computeChunkInFrustum(int chunkX, int chunkZ) {
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
        int effective = BariumConfig.C.EFFECTIVE_RENDER_DISTANCE > 0 ? BariumConfig.C.EFFECTIVE_RENDER_DISTANCE : renderDistance;
        int playerChunkX = client.player.getChunkPos().x;
        int playerChunkZ = client.player.getChunkPos().z;
        int dx = Math.abs(chunkX - playerChunkX);
        int dz = Math.abs(chunkZ - playerChunkZ);

        if (dx <= effective && dz <= effective) {
            // Sempre mantém próximos ao jogador visíveis (raio 2)
            if (dx <= 2 && dz <= 2) return true;

            int sparse = Math.max(1, BariumConfig.C.SPARSE_CHUNK_FACTOR);
            if (sparse <= 1) return true;

            // Renderiza apenas chunks alinhados à grade do fator esparso
            return Math.floorMod(chunkX - playerChunkX, sparse) == 0 && Math.floorMod(chunkZ - playerChunkZ, sparse) == 0;
        }

        return false;
    }

    private long packChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL) ^ frameId;
    }

    private long packSectionKey(int sectionX, int sectionY, int sectionZ) {
        long value = ((sectionX & 0x3FFFFFL) << 42)
                | ((sectionY & 0xFFFFFL) << 22)
                | (sectionZ & 0x3FFFFFL);
        return value ^ frameId;
    }

    // Método mantido para compatibilidade, mas agora apenas reseta o frustum.
    public void calculateChunksToRender(MinecraftClient client, Frustum frustum) {
        this.setFrustum(frustum);
    }

    public void clear() {
        this.frustum = null;
        this.chunkVisibilityCache.clear();
        this.sectionVisibilityCache.clear();
        this.frameId = 0;
    }
}
