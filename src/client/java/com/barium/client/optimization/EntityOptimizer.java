package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityOptimizer {

    // CORREÇÃO: A assinatura do método foi corrigida para aceitar as coordenadas da câmera diretamente.
    public static boolean shouldRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ) {
        if (!BariumConfig.C.ENABLE_ENTITY_CULLING) {
            return true;
        }

        if (entity.isPlayer() && entity.isInvisible()) {
             return false;
        }
        if (entity.hasPassenger(MinecraftClient.getInstance().player)) {
            return false;
        }

        // Usa as coordenadas da entidade e da câmera para calcular a distância.
        double distanceSq = entity.getPos().squaredDistanceTo(cameraX, cameraY, cameraZ);

        return distanceSq <= BariumConfig.C.MAX_ENTITY_RENDER_DISTANCE_SQ;
    }
}