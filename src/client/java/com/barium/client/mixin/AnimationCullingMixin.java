package com.barium.client.mixin;

import com.barium.client.optimization.AnimationCullingOptimizer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para integrar o otimizador de culling de animações
 */
@Mixin(targets = {"net.minecraft.client.render.block.BlockAnimator", "net.minecraft.client.render.block.entity.BlockEntityAnimationManager"})
public class AnimationCullingMixin {
    
    /**
     * Injeta no método de atualização de animações para aplicar culling
     */
    @Inject(
        method = {"tick", "update", "animate"},
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void onAnimationTick(CallbackInfo ci) {
        // Obtém o contador de ticks atual
        int tickCounter = MinecraftClient.getInstance().world != null ? 
                (int) MinecraftClient.getInstance().world.getTime() : 0;
                
        // Verifica se a animação deve ser atualizada neste tick
        // Como não temos acesso direto ao bloco aqui, usamos uma verificação global
        // Em um mod real, isso seria implementado com mais precisão
        if (!AnimationCullingOptimizer.shouldUpdateAnimation(null, null, tickCounter)) {
            ci.cancel(); // Pula a atualização da animação
        }
    }
    
    /**
     * Injeta em métodos específicos de animação de blocos
     */
    @Inject(
        method = {"shouldAnimate", "isAnimating"},
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private void onShouldAnimate(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (pos != null && cir.getReturnValueZ()) {
            // Obtém o contador de ticks atual
            int tickCounter = MinecraftClient.getInstance().world != null ? 
                    (int) MinecraftClient.getInstance().world.getTime() : 0;
                    
            // Verifica se a animação deve ser atualizada
            if (!AnimationCullingOptimizer.shouldUpdateAnimation(pos, state, tickCounter)) {
                cir.setReturnValue(false); // Não anima este bloco
            }
        }
    }
}
