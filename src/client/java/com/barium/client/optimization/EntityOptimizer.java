package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

public class EntityOptimizer {

    public static boolean shouldRenderByDistance(Entity entity, double cameraX, double cameraY, double cameraZ) {
        if (entity.isPlayer() || entity.hasPassengers() || entity.hasVehicle() || entity.isGlowing()) {
            return true;
        }
        if (entity instanceof EnderDragonEntity || entity instanceof BoatEntity || entity instanceof MinecartEntity) {
            return true;
        }

        // CORREÇÃO 25w45a: Entity.getPos() foi removido. Use entity.squaredDistanceTo(...).
        double distanceSq = entity.squaredDistanceTo(cameraX, cameraY, cameraZ);
        if (distanceSq > BariumConfig.C.MAX_ENTITY_RENDER_DISTANCE_SQ) {
            return false;
        }

        return true;
    }
}