package com.barium.client.mixin;

import com.barium.client.optimization.EntityOptimizer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    // Mantemos o @Shadow para obter acesso seguro ao dispatcher e, consequentemente, à câmera.
    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    /**
     * Injeta no início do método 'shouldRender' para adicionar nossa lógica de culling por distância.
     * Esta é uma abordagem mais robusta e compatível do que injetar no método 'render',
     * pois evita conflitos de assinatura com mods como o Lithium.
     *
     * @param entity A entidade a ser verificada.
     * @param cir O CallbackInfo que nos permite modificar o valor de retorno do método.
     */
    @Inject(
        method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$cullDistantEntity(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        // A câmera pode ser nula durante a inicialização.
        if (this.dispatcher.camera == null) {
            return;
        }

        // Se nossa otimização decidir que a entidade não deve ser renderizada,
        // nós forçamos o método a retornar 'false' e cancelamos sua execução.
        if (!EntityOptimizer.shouldRenderEntity(entity, this.dispatcher.camera)) {
            cir.setReturnValue(false);
        }
    }
}