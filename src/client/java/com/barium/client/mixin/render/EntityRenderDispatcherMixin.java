package com.barium.client.mixin.render;

import com.barium.config.BariumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    // O @Inject é colocado no início da execução do método shouldRender
    // e permite que o nosso código decida se o método original deve prosseguir.
    @Inject(method = "shouldRender", at = "HEAD", cancellable = true)
    // Suprimindo o warning "InvalidMemberReference" porque o Mixin pode não resolver o método 'shouldRender'
    // em tempo de compilação sem as dependências completas do Minecraft.
    @SuppressWarnings("InvalidMemberReference")
    private <E extends Entity> void barium$onShouldRender(E entity, CallbackInfoReturnable<Boolean> cir) {
        BariumConfig config = BariumConfig.getConfig();

        // Se a otimização de culling não estiver habilitada, permita que o método original continue.
        if (!config.clientOptimizations.entityCulling.enableEntityCulling) {
            return;
        }

        // Obtém a distância de culling da configuração.
        int cullingDistance = config.clientOptimizations.entityCulling.cullingDistance;

        // Verifica a distância entre a entidade e a câmera de renderização.
        // O método 'distanceToSqr' retorna a distância ao quadrado para evitar cálculos de raiz quadrada,
        // que são caros. Precisamos comparar com o quadrado da distância de culling.
        double distanceSqr = entity.distanceToSqr(
            ((EntityRenderDispatcher)(Object)this).camera.getPosition().x,
            ((EntityRenderDispatcher)(Object)this).camera.getPosition().y,
            ((EntityRenderDispatcher)(Object)this).camera.getPosition().z
        );

        // Se a entidade estiver mais distante do que a distância de culling configurada (ao quadrado),
        // cancelamos o método original e retornamos 'false' (não renderizar).
        if (distanceSqr > cullingDistance * cullingDistance) {
            cir.setReturnValue(false);
            cir.cancel(); // Cancela a execução do método original
        }
        // Se a entidade estiver próxima o suficiente, permita que o método original continue para que
        // outras verificações de renderização do Minecraft (como frustum culling) possam ser feitas.
    }
}