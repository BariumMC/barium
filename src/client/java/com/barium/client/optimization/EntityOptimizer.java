package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class EntityOptimizer {

    /**
     * Determina se uma entidade deve ser renderizada com base na distância.
     * @param entity A entidade a ser verificada.
     * @param camera A câmera do jogo.
     * @return false se a entidade estiver muito longe para ser renderizada.
     */
    public static boolean shouldRenderEntity(Entity entity, Camera camera) {
        if (!BariumConfig.ENABLE_ENTITY_OPTIMIZATION || !BariumConfig.ENABLE_ENTITY_CULLING) {
            return true;
        }

        // Nunca faça culling do jogador ou de seu veículo
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (entity == player || entity.hasPassenger(player)) {
            return true;
        }

        Vec3d cameraPos = camera.getPos();
        double distanceSq = entity.getPos().squaredDistanceTo(cameraPos);

        return distanceSq <= BariumConfig.MAX_ENTITY_RENDER_DISTANCE_SQ;
    }

    /**
     * Determina se a animação de uma entidade deve ser atualizada.
     * @param entity A entidade a ser verificada.
     * @return false se a entidade estiver muito longe para ser animada.
     */
    public static boolean shouldAnimateEntity(Entity entity) {
        if (!BariumConfig.ENABLE_ENTITY_OPTIMIZATION || !BariumConfig.ENABLE_ANIMATION_CULLING) {
            return true;
        }

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        double distanceSq = entity.getPos().squaredDistanceTo(cameraPos);

        return distanceSq <= BariumConfig.ANIMATION_CULL_DISTANCE_SQ;
    }
}