package com.barium.client.mixin;

import com.barium.client.optimization.EntityRenderingOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para EntityRenderDispatcher para otimizar a renderização de entidades no cliente.
 * Aplica culling e Level of Detail (LOD) com base na distância.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    /**
     * Injeta no início do método render() da entidade.
     * Verifica se a entidade deve ser renderizada e em qual nível de detalhe.
     *
     * Target Method Signature (Yarn 1.21.5): render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V
     */
    @Inject(
        method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onRenderEntity(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        EntityRenderingOptimizer.RenderLevel renderLevel = EntityRenderingOptimizer.getEntityRenderLevel(entity, camera);

        if (renderLevel == EntityRenderingOptimizer.RenderLevel.CULLED) {
            ci.cancel(); // Não renderiza a entidade
        }
        // Para RenderLevel.SIMPLIFIED:
        // No momento, 'SIMPLIFIED' apenas indica que a entidade está longe.
        // Uma implementação real de LOD envolveria a substituição do modelo/shader,
        // o que está fora do escopo de um mixin simples e requereria integração mais profunda.
        // Um "frame-skip" simples pode ser implementado aqui, mas pode causar stuttering visual.
        // Exemplo:
        // if (renderLevel == EntityRenderingOptimizer.RenderLevel.SIMPLIFIED && MinecraftClient.getInstance().world.getTime() % 2 != 0) {
        //     ci.cancel(); // Renderiza a cada 2 frames
        // }
    }
}