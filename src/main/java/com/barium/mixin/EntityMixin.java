package com.barium.mixin;

import com.barium.optimization.EntityTickOptimizer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        World world = entity.getWorld();
        
        // Verifica se a entidade deve ser congelada
        if (EntityTickOptimizer.shouldFreezeEntity(entity, world)) {
            // Verifica se deve ser atualizada neste tick
            if (!EntityTickOptimizer.shouldTickFrozenEntity(entity)) {
                ci.cancel(); // Cancela o tick desta entidade
            }
        }
    }
}
