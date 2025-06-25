package com.barium.client.util;

import com.barium.config.BariumConfig;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos; // <-- IMPORT ADICIONADO AQUI
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.atomic.AtomicReference;

public class ChunkVisibilityManager {
    private static final ChunkVisibilityManager INSTANCE = new ChunkVisibilityManager();
    public static ChunkVisibilityManager getInstance() { return INSTANCE; }

    private static final int RAYS_TO_CAST = 128;
    private static final double MAX_RAY_DISTANCE = 96.0;

    private final AtomicReference<LongSet> visibleChunkKeys = new AtomicReference<>(new LongOpenHashSet());
    private long lastUpdateTime = 0;
    private Vec3d lastPlayerPos = Vec3d.ZERO;

    public void update(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        long now = System.currentTimeMillis();
        Vec3d playerPos = client.player.getPos();

        if (now - lastUpdateTime < 500 && playerPos.squaredDistanceTo(lastPlayerPos) < 256) {
            return;
        }

        this.lastUpdateTime = now;
        this.lastPlayerPos = playerPos;

        LongSet newVisibleChunks = new LongOpenHashSet();
        Vec3d cameraPos = client.cameraEntity.getEyePos();
        
        newVisibleChunks.add(new ChunkPos(client.player.getBlockPos()).toLong());

        double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
        double angleIncrement = Math.PI * 2.0 * goldenRatio;

        for (int i = 0; i < RAYS_TO_CAST; i++) {
            double t = (double) i / RAYS_TO_CAST;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;

            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);
            
            Vec3d direction = new Vec3d(x, y, z);
            Vec3d targetPos = cameraPos.add(direction.multiply(MAX_RAY_DISTANCE));

            RaycastContext context = new RaycastContext(cameraPos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player);
            BlockHitResult hitResult = client.world.raycast(context);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                newVisibleChunks.add(new ChunkPos(hitResult.getBlockPos()).toLong());
            } else if (hitResult.getType() == HitResult.Type.MISS) {
                // A linha com erro agora funcionará, pois BlockPos foi importado.
                newVisibleChunks.add(new ChunkPos(new BlockPos((int)targetPos.x, (int)targetPos.y, (int)targetPos.z)).toLong());
            }
        }
        
        visibleChunkKeys.set(newVisibleChunks);
    }

    public boolean isChunkPotentiallyVisible(int chunkX, int chunkZ) {
        LongSet visibleSet = visibleChunkKeys.get();
        if (visibleSet == null || visibleSet.isEmpty()) {
            return true;
        }
        return visibleSet.contains(ChunkPos.toLong(chunkX, chunkZ));
    }
}