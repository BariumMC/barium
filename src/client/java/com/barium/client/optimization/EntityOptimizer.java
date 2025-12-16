// --- Substitua o conteúdo em: src/client/java/com/barium/client/optimization/EntityOptimizer.java ---
package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

public class EntityOptimizer {

    /**
     * Lógica de otimização de renderização de entidade baseada na distância e frustum.
     * Retorna 'true' se a entidade deve ser renderizada, 'false' caso contrário.
     *
     * @param entity  A entidade a ser verificada.
     * @param cameraX Posição X da câmera.
     * @param cameraY Posição Y da câmera.
     * @param cameraZ Posição Z da câmera.
     * @param frustum O frustum da câmera, pode ser null.
     * @return true se a entidade estiver dentro da distância de renderização e no frustum.
     */
    public static boolean shouldRender(Entity entity, double cameraX, double cameraY, double cameraZ, net.minecraft.client.render.Frustum frustum) {

        // Verificação 0: Não otimizar entidades importantes ou que o jogador está usando.
        if (entity.isPlayer() || entity.hasPassengers() || entity.hasVehicle() || entity.isGlowing()) {
            return true;
        }
        if (entity instanceof EnderDragonEntity || entity instanceof BoatEntity || entity instanceof MinecartEntity) {
            return true;
        }

        // Verificação 1: Otimização por Distância
        double distanceSq = entity.squaredDistanceTo(cameraX, cameraY, cameraZ);
        if (distanceSq > BariumConfig.C.MAX_ENTITY_RENDER_DISTANCE_SQ) {
            return false; // Entidade está muito longe. Não renderizar.
        }

        // Verificação 2: Frustum Culling
        if (BariumConfig.C.ENABLE_ENTITY_FRUSTUM_CULLING && frustum != null) {
            if (!frustum.isVisible(entity.getBoundingBox())) {
                return false; // Entidade está fora do frustum.
            }
        }

        return true;
    }

    /**
     * Método legado para compatibilidade.
     */
    public static boolean shouldRenderByDistance(Entity entity, double cameraX, double cameraY, double cameraZ) {
        return shouldRender(entity, cameraX, cameraY, cameraZ, null);
    }
}