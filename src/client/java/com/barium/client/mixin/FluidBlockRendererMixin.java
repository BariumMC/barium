package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.block.FluidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlockRenderer.class)
public class FluidBlockRendererMixin {

    /**
     * Esta é uma otimização nova e estável para substituir as que falharam.
     * Ela impede que o jogo calcule as faces do bloco de fluido se as animações
     * de textura estiverem desativadas, congelando efetivamente a água e a lava.
     *
     * @param ci O CallbackInfo que nos permite cancelar o método.
     */
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void barium$freezeFluidRendering(CallbackInfoReturnable<Boolean> ci) {
        // Se a opção de desativar animações estiver ligada, nós cancelamos a renderização
        // do fluido, o que o impede de se mover.
        if (BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS) {
            ci.setReturnValue(false); // Retorna 'false' para indicar que nenhum vértice foi adicionado.
        }
    }
}