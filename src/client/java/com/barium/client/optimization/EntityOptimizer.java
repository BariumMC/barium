package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityOptimizer {

    public static boolean shouldSkipRenderByDistance(Entity entity, double cameraX, double cameraY, double cameraZ) {
        // CORREÇÃO: Removida a verificação da variável ENABLE_ENTITY_OPTIMIZATION que não existia mais.
        if (!BariumConfig.C.ENABLE_ENTITY_CULLING) {
            return false;
        }

        if (entity.isPlayer() && entity.isInvisible()) {
             return false;
        }
        if (entity.hasPassenger(MinecraftClient.getInstance().player)) {
            return false;
        }

        Vec3d entityPos = entity.getPos();
        double distanceSq = entityPos.squaredDistanceTo(cameraX, cameraY, cameraZ);

        return distanceSq > BariumConfig.C.MAX_ENTITY_RENDER_DISTANCE_SQ;
    }
}