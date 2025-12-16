package com.barium.client.util;

import com.barium.client.BariumClient;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class FloodFillVisibilityManager {
    private static final FloodFillVisibilityManager INSTANCE = new FloodFillVisibilityManager();
    public static FloodFillVisibilityManager getInstance() { return INSTANCE; }

    private final AtomicReference<LongSet> visibleSectionKeys = new AtomicReference<>(new LongOpenHashSet());
    private Future<?> visibilityTask = null;
    private long lastUpdateTime = 0;
    // Intervalo reduzido para resposta mais rápida ao mover a câmera
    private static final long UPDATE_INTERVAL_MS = 100; 

    public void update(MinecraftClient client) {
        if (client.world == null || client.getCameraEntity() == null) return;
        
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdateTime) < UPDATE_INTERVAL_MS) return;

        if (visibilityTask != null && !visibilityTask.isDone()) return;
        
        lastUpdateTime = currentTime;
        // Captura a posição e o mundo na thread principal para segurança
        BlockPos cameraBlockPos = client.getCameraEntity().getBlockPos();
        World world = client.world;
        int renderDistance = client.options.getViewDistance().getValue();

        visibilityTask = BariumClient.RENDER_THREAD_POOL.submit(() -> runFloodFill(world, cameraBlockPos, renderDistance));
    }
    
    private void runFloodFill(World world, BlockPos startPos, int renderDistance) {
        try {
            LongSet sectionsToRender = new LongOpenHashSet();
            Queue<BlockPos> queue = new ArrayDeque<>();

            // Começa da seção onde o jogador está
            BlockPos startSectionPos = new BlockPos(
                startPos.getX() >> 4,
                startPos.getY() >> 4,
                startPos.getZ() >> 4
            );

            sectionsToRender.add(startSectionPos.asLong());
            queue.add(startSectionPos);
            
            // Limite de iterações para evitar travamento da thread em mundos muito abertos
            int iterations = 0;
            int maxIterations = Math.min(5000, renderDistance * renderDistance * 2); 

            while(!queue.isEmpty() && iterations < maxIterations) {
                BlockPos currentSectionPos = queue.poll();
                iterations++;
                
                for(Direction direction : Direction.values()){
                    BlockPos neighborSectionPos = currentSectionPos.add(direction.getVector());

                    if (!sectionsToRender.contains(neighborSectionPos.asLong())) {
                        // Transforma posição de seção em posição de bloco real (origem da seção)
                        BlockPos ourSectionOrigin = new BlockPos(currentSectionPos.getX() * 16, currentSectionPos.getY() * 16, currentSectionPos.getZ() * 16);
                        
                        // Se a face que leva ao vizinho NÃO é opaca, podemos ver através dela
                        if (!ChunkCullingUtils.isNeighboringFaceOpaque(world, ourSectionOrigin, direction)) {
                            long key = neighborSectionPos.asLong();
                            sectionsToRender.add(key);
                            
                            // Limita a propagação para não carregar o mundo inteiro (distância de render ~renderDistance chunks)
                            if (Math.abs(neighborSectionPos.getX() - startSectionPos.getX()) <= renderDistance &&
                                Math.abs(neighborSectionPos.getZ() - startSectionPos.getZ()) <= renderDistance) {
                                queue.add(neighborSectionPos);
                            }
                        }
                    }
                }
            }
            visibleSectionKeys.set(sectionsToRender);
        } catch (Exception e) {
            // Em caso de erro (concurrent modification, etc), não crasha, apenas ignora
            // O próximo tick tentará novamente
        }
    }
    
    public boolean isSectionVisible(int sectionX, int sectionY, int sectionZ) {
        LongSet visible = visibleSectionKeys.get();
        // Se o conjunto estiver vazio (início do jogo), retorne TRUE para evitar tela infinita
        if (visible == null || visible.isEmpty()) return true;
        return visible.contains(BlockPos.asLong(sectionX, sectionY, sectionZ));
    }

    public void clear() {
        this.visibleSectionKeys.set(new LongOpenHashSet());
        this.lastUpdateTime = 0;
    }
}