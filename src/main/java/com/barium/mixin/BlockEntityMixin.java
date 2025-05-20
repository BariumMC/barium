package com.barium.mixin;

import com.barium.optimization.BlockTickOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        BlockEntity blockEntity = (BlockEntity)(Object)this;
        
        // Verifica se é um hopper
        if (blockEntity instanceof HopperBlockEntity) {
            HopperBlockEntity hopper = (HopperBlockEntity)blockEntity;
            
            // Aplica otimizações específicas para hoppers
            if (!BlockTickOptimizer.shouldTickHopper(hopper, blockEntity.getPos())) {
                ci.cancel(); // Cancela o tick deste hopper
            }
        }
        // Outras otimizações para outros tipos de block entities podem ser adicionadas aqui
    }
}
