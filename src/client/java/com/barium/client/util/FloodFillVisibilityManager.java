package com.barium.client.util;

import com.barium.client.BariumClient;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
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
    private static final long UPDATE_INTERVAL_MS = 250;

    public void update(MinecraftClient client) {
        // CORREÇÃO 25w45a: client.cameraEntity é privado. Use client.getCameraEntity().
        Entity cameraEntity = client.getCameraEntity();
        if (client.world == null || cameraEntity == null) return;
        
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdateTime) < UPDATE_INTERVAL_MS) return;

        if (visibilityTask != null && !visibilityTask.isDone()) return;
        
        lastUpdateTime = currentTime;
        visibilityTask = BariumClient.RENDER_THREAD_POOL.submit(() -> runFloodFill(client.world, cameraEntity.getBlockPos()));
    }
    
    private void runFloodFill(World world, BlockPos startPos) {
        LongSet sectionsToRender = new LongOpenHashSet();
        Queue<BlockPos> queue = new ArrayDeque<>();

        BlockPos startSectionPos = new BlockPos(
            startPos.getX() >> 4,
            startPos.getY() >> 4,
            startPos.getZ() >> 4
        );

        sectionsToRender.add(startSectionPos.asLong());
        queue.add(startSectionPos);

        while(!queue.isEmpty()){
            BlockPos currentSectionPos = queue.poll();
            
            for(Direction direction : Direction.values()){
                BlockPos neighborSectionPos = currentSectionPos.add(direction.getVector());

                if (!sectionsToRender.contains(neighborSectionPos.asLong())) {
                    BlockPos ourSectionOrigin = new BlockPos(currentSectionPos.getX() * 16, currentSectionPos.getY() * 16, currentSectionPos.getZ() * 16);
                    if (!ChunkCullingUtils.isNeighboringFaceOpaque(world, ourSectionOrigin, direction)) {
                        sectionsToRender.add(neighborSectionPos.asLong());
                        queue.add(neighborSectionPos);
                    }
                }
            }
        }
        visibleSectionKeys.set(sectionsToRender);
    }
    
    public boolean isSectionVisible(int sectionX, int sectionY, int sectionZ) {
        return visibleSectionKeys.get().contains(BlockPos.asLong(sectionX, sectionY, sectionZ));
    }

    public void clear() {
        this.visibleSectionKeys.set(new LongOpenHashSet());
        this.lastUpdateTime = 0;
    }
}