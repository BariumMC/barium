package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class EntityOptimizer {

    public static boolean shouldRenderEntity(Entity entity, Camera camera) {
        if (!BariumConfig.C.ENABLE_ENTITY_OPTIMIZATION || !BariumConfig.C.ENABLE_ENTITY_CULLING) {
            return true;
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (entity == player || entity.hasPassenger(player)) {
            return true;
        }

        Vec3d cameraPos = camera.getPos();
        double distanceSq = entity.getPos().squaredDistanceTo(cameraPos);

        return distanceSq <= BariumConfig.C.MAX_ENTITY_RENDER_DISTANCE_SQ;
    }
}