package com.barium.client.mixin.render;

import com.barium.client.optimizations.BariumClientOptimizations; // Importe a nova classe de lógica
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity:
import net.minecraft.client.render.Camera; // Importe a classe Camera, se ainda não estiver
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow; // Importe Shadow para acessar campos privados/protegidos
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin { // Tornar abstrato é uma boa prática para mixins sem construtor

    // @Shadow permite que o Mixin acesse campos privados/protegidos da classe original.
    // Neste caso, precisamos da instância da câmera do EntityRenderDispatcher.
    @Shadow protected Camera camera;

    @Inject(method = "shouldRender", at = "HEAD", cancellable = true)
    @SuppressWarnings("InvalidMemberReference")
    private <E extends Entity> void barium$onShouldRender(E entity, CallbackInfoReturnable<Boolean> cir) {
        // Agora, o Mixin simplesmente chama o método na sua classe de lógica.
        // Passamos 'entity' (que já é um parâmetro do método shouldRender) e a 'camera' (acessada via @Shadow).
        if (BariumClientOptimizations.shouldCullEntity(entity, this.camera)) {
            cir.setReturnValue(false);
            cir.cancel(); // Cancela a execução do método original
        }
        // Se retornar false aqui, o Mixin permite que o método original continue.
    }
}