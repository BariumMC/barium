// --- Substitua o conteúdo em: src/client/java/com/barium/client/optimization/EntityOptimizer.java ---
package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;

public class EntityOptimizer {

    /**
     * Lógica de otimização de renderização de entidade em múltiplos estágios.
     * Retorna 'true' se a entidade deve ser renderizada, 'false' caso contrário.
     *
     * @param entity A entidade a ser verificada.
     * @param frustum O frustum (cone de visão) da câmera.
     * @param cameraX Posição X da câmera.
     * @param cameraY Posição Y da câmera.
     * @param cameraZ Posição Z da câmera.
     * @return true se a entidade passar em todas as verificações.
     */
    public static boolean shouldRenderEntity(Entity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        
        // Verificação 0: Não otimizar entidades importantes ou que o jogador está usando.
        if (entity.isPlayer() || entity.hasPassengers() || entity.hasVehicle() || entity.isGlowing()) {
            return true;
        }
        if (entity instanceof EnderDragonEntity || entity instanceof BoatEntity || entity instanceof MinecartEntity) {
            return true;
        }

        // Verificação 1: Otimização por Distância (a mais barata)
        // Calcula a distância ao quadrado (mais rápido que a raiz quadrada).
        double distanceSq = entity.getPos().squaredDistanceTo(cameraX, cameraY, cameraZ);
        if (distanceSq > BariumConfig.C.MAX_ENTITY_RENDER_DISTANCE_SQ) {
            return false; // Entidade está muito longe. Não renderizar.
        }

        // Verificação 2: Otimização por Frustum (um pouco mais cara, mas muito eficaz)
        // Verifica se a "caixa" da entidade está dentro do cone de visão da câmera.
        Box boundingBox = entity.getVisibilityBoundingBox();
        if (!frustum.isVisible(boundingBox)) {
            return false; // Entidade não está na tela. Não renderizar.
        }

        // Se a entidade passou por todas as verificações, ela deve ser renderizada.
        return true;
    }
}