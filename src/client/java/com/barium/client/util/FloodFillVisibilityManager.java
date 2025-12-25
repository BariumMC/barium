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

            int effective = com.barium.config.BariumConfig.C.EFFECTIVE_RENDER_DISTANCE > 0 ? com.barium.config.BariumConfig.C.EFFECTIVE_RENDER_DISTANCE : renderDistance;
            int sparse = Math.max(1, com.barium.config.BariumConfig.C.SPARSE_CHUNK_FACTOR);

            chunksToRender.add(startPos.toLong());
            queue.add(startPos);
            
            // Limite de iterações defensivo baseado na efetiva distância
            int iterations = 0;
            int maxIterations = (effective * effective) * 4;

            while(!queue.isEmpty() && iterations < maxIterations) {
                ChunkPos currentChunkPos = queue.poll();
                iterations++;
                
                for(Direction direction : Direction.values()){
                    // Apenas direções horizontais para culling 2D simplificado
                    if (direction.getAxis() == Direction.Axis.Y) continue;
                    
                    ChunkPos neighborChunkPos = new ChunkPos(currentChunkPos.x + direction.getOffsetX(), currentChunkPos.z + direction.getOffsetZ());

                    if (!chunksToRender.contains(neighborChunkPos.toLong())) {
                        long key = neighborChunkPos.toLong();

                        int dx = Math.abs(neighborChunkPos.x - startPos.x);
                        int dz = Math.abs(neighborChunkPos.z - startPos.z);

                        // Limita a propagação para não carregar o mundo inteiro usando a efetiva distância
                        if (dx <= effective && dz <= effective) {
                            // Se houver fator esparso, só propagamos em uma grade espaçada para reduzir carga
                            if (sparse <= 1 || (Math.floorMod(neighborChunkPos.x - startPos.x, sparse) == 0 && Math.floorMod(neighborChunkPos.z - startPos.z, sparse) == 0)) {
                                chunksToRender.add(key);
                                queue.add(neighborChunkPos);
                            }
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
        this.visibleChunkKeys.set(new LongOpenHashSet());
        this.lastUpdateTime = 0;
    }
}