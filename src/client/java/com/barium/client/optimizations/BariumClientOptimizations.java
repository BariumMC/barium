package com.barium.client.optimizations;

import com.barium.config.BariumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.Frustum; // Importação da Frustum para culling (opcional, mas bom ter)
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Camera; // Importe a classe Camera

@Environment(EnvType.CLIENT)
public class BariumClientOptimizations {

    /**
     * Determina se uma entidade deve ser ignorada (culled) para renderização com base na distância.
     *
     * @param entity A entidade a ser verificada.
     * @param camera A câmera atual do jogador, para calcular a distância.
     * @return true se a entidade deve ser culled (não renderizada), false caso contrário.
     */
    public static boolean shouldCullEntity(Entity entity, Camera camera) {
        BariumConfig config = BariumConfig.getConfig();

        // Se a otimização de culling não estiver habilitada, não culle.
        if (!config.clientOptimizations.entityCulling.enableEntityCulling) {
            return false;
        }

        // Obtém a distância de culling da configuração.
        int cullingDistance = config.clientOptimizations.entityCulling.cullingDistance;

        // Calcula a distância ao quadrado da entidade para a câmera.
        // Usamos distância ao quadrado para evitar a operação de raiz quadrada, que é custosa.
        double distanceSqr = entity.distanceToSqr(
            camera.getPosition().x,
            camera.getPosition().y,
            camera.getPosition().z
        );

        // Se a entidade estiver mais distante do que a distância de culling configurada (ao quadrado),
        // deve ser culled.
        return distanceSqr > (double)cullingDistance * cullingDistance;
    }

    // Você pode adicionar outros métodos de otimização de cliente aqui no futuro.
    // Ex:
    // public static void optimizeBlockRendering(...) { ... }
}