package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin {

    /**
     * Intercepta a finalização de um buffer de desenho.
     * Se o BariumRenderManager estiver ativo, nós impedimos o buffer vanilla
     * de ser finalizado, efetivamente o esvaziando.
     */
    @Inject(method = "end", at = @At("HEAD"), cancellable = true)
    private void barium$preventVanillaChunkDraw(CallbackInfoReturnable<BuiltBuffer> cir) {
        if (BariumRenderManager.getInstance().isActive()) {
            cir.setReturnValue(null);
        }
    }
}