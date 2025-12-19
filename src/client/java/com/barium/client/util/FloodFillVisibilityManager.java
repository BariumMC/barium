package com.barium.client.util;

import com.barium.client.BariumClient;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class FloodFillVisibilityManager {
    private static final FloodFillVisibilityManager INSTANCE = new FloodFillVisibilityManager();
    public static FloodFillVisibilityManager getInstance() { return INSTANCE; }

    private final AtomicReference<LongSet> visibleChunkKeys = new AtomicReference<>(new LongOpenHashSet());
    private Future<?> visibilityTask = null;
    private long lastUpdateTime = 0;
    // Intervalo reduzido para resposta mais rápida ao mover a câmera
    private static final long UPDATE_INTERVAL_MS = 50; // Reduzido para mais responsividade

    public void update(MinecraftClient client) {
        if (client.world == null || client.getCameraEntity() == null) return;
        
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdateTime) < UPDATE_INTERVAL_MS) return;

        if (visibilityTask != null && !visibilityTask.isDone()) return;
        
        lastUpdateTime = currentTime;
        // Captura a posição e o mundo na thread principal para segurança
        ChunkPos cameraChunkPos = client.getCameraEntity().getChunkPos();
        World world = client.world;
        int renderDistance = client.options.getViewDistance().getValue();

        visibilityTask = BariumClient.RENDER_THREAD_POOL.submit(() -> runFloodFill(world, cameraChunkPos, renderDistance));
    }
    
    private void runFloodFill(World world, ChunkPos startPos, int renderDistance) {
        try {
            LongSet chunksToRender = new LongOpenHashSet();
            Queue<ChunkPos> queue = new ArrayDeque<>();

            chunksToRender.add(startPos.toLong());
            queue.add(startPos);
            
            // Limite de iterações aumentado para cobrir mais chunks
            int iterations = 0;
            int maxIterations = renderDistance * renderDistance * 4; // Aumentado para melhor cobertura

            while(!queue.isEmpty() && iterations < maxIterations) {
                ChunkPos currentChunkPos = queue.poll();
                iterations++;
                
                for(Direction direction : Direction.values()){
                    // Apenas direções horizontais para culling 2D simplificado
                    if (direction.getAxis() == Direction.Axis.Y) continue;
                    
                    ChunkPos neighborChunkPos = new ChunkPos(currentChunkPos.x + direction.getOffsetX(), currentChunkPos.z + direction.getOffsetZ());

                    if (!chunksToRender.contains(neighborChunkPos.toLong())) {
                        // Verifica se podemos ver através da face (aproximação: sempre verdadeiro para direções horizontais, ou verificar opacidade)
                        // Para otimização, sempre propagamos dentro da distância, assumindo que frustum cuida do resto
                        long key = neighborChunkPos.toLong();
                        chunksToRender.add(key);
                        
                        // Limita a propagação para não carregar o mundo inteiro
                        if (Math.abs(neighborChunkPos.x - startPos.x) <= renderDistance &&
                            Math.abs(neighborChunkPos.z - startPos.z) <= renderDistance) {
                            queue.add(neighborChunkPos);
                        }
                    }
                }
            }
            visibleChunkKeys.set(chunksToRender);
        } catch (Exception e) {
            // Em caso de erro, não crasha
        }
    }
    
    public boolean isChunkVisible(int chunkX, int chunkZ) {
        LongSet visible = visibleChunkKeys.get();
        if (visible == null || visible.isEmpty()) return true;
        return visible.contains(ChunkPos.toLong(chunkX, chunkZ));
    }

    public void clear() {
        this.visibleSectionKeys.set(new LongOpenHashSet());
        this.lastUpdateTime = 0;
    }
}