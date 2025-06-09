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
        // Marcador de contexto para a otimização de brilho de encantamento
    public static boolean isRenderingDroppedItem = false;
    public static Entity currentlyRenderingEntity = null;

    /**
     * Determina se o conteúdo de uma moldura deve ser renderizado com base na distância.
     */
    public static boolean shouldRenderItemFrameContent(Entity itemFrameEntity) {
        if (!BariumConfig.ENABLE_ITEM_FRAME_CULLING) return true;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        double distanceSq = itemFrameEntity.getPos().squaredDistanceTo(camera.getPos());
        return distanceSq <= BariumConfig.ITEM_FRAME_CULL_DISTANCE_SQ;
    }

    /**
     * Determina se o nome de uma entidade deve ser renderizado.
     */
    public static boolean shouldRenderNameTag(Entity entity) {
        if (!BariumConfig.ENABLE_NAME_TAG_CULLING) return true;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        double distanceSq = entity.getPos().squaredDistanceTo(camera.getPos());
        return distanceSq <= BariumConfig.NAME_TAG_CULL_DISTANCE_SQ;
    }

    /**
     * Determina se o brilho de um item no chão deve ser renderizado.
     */
    public static boolean shouldRenderDroppedItemGlint() {
        if (!isRenderingDroppedItem || !BariumConfig.ENABLE_DROPPED_ITEM_GLINT_CULLING) {
            return true;
        }
        if (currentlyRenderingEntity == null) return true;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        double distanceSq = currentlyRenderingEntity.getPos().squaredDistanceTo(camera.getPos());
        return distanceSq <= BariumConfig.DROPPED_ITEM_GLINT_CULL_DISTANCE_SQ;
    }
}