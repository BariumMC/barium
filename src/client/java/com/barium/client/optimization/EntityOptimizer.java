// --- Substitua o conteúdo em: src/client/java/com/barium/client/optimization/EntityOptimizer.java ---
package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

public class EntityOptimizer {

    /**
     * Lógica de otimização de renderização de entidade baseada APENAS na distância.
     * Retorna 'true' se a entidade deve ser renderizada, 'false' caso contrário.
     *
     * @param entity  A entidade a ser verificada.
     * @param cameraX Posição X da câmera.
     * @param cameraY Posição Y da câmera.
     * @param cameraZ Posição Z da câmera.
     * @return true se a entidade estiver dentro da distância de renderização.
     */
    public static boolean shouldRenderByDistance(Entity entity, double cameraX, double cameraY, double cameraZ) {

        // Verificação 0: Não otimizar entidades importantes ou que o jogador está usando.
        if (entity.isPlayer() || entity.hasPassengers() || entity.hasVehicle() || entity.isGlowing()) {
            return true;
        }
        if (entity instanceof EnderDragonEntity || entity instanceof BoatEntity || entity instanceof MinecartEntity) {
            return true;
        }

        // Verificação 1: Otimização por Distância
        // CORREÇÃO: Substituído o método getPos() por um cálculo de distância direto.
        double distanceSq = entity.squaredDistanceTo(cameraX, cameraY, cameraZ);
        if (distanceSq > BariumConfig.C.MAX_ENTITY_RENDER_DISTANCE_SQ) {
            return false; // Entidade está muito longe. Não renderizar.
        }

        // Se a entidade passou pela verificação de distância, ela pode ser renderizada.
        // O frustum culling será feito pelo próprio Minecraft.
        return true;
    }
}