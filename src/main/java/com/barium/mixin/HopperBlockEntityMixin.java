package com.barium.mixin;

import com.barium.optimization.BlockTickOptimizer;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        HopperBlockEntity hopper = (HopperBlockEntity)(Object)this;
        
        // Aplica otimizações específicas para hoppers
        if (!BlockTickOptimizer.shouldTickHopper(hopper, hopper.getPos())) {
            ci.cancel(); // Cancela o tick deste hopper
        }
    }
}
