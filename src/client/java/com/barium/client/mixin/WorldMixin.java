package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {

    /**
     * Injeta no início do método que gera partículas de ambiente (fumaça de tocha, etc).
     * Este método existe na classe World e é chamado pelo ClientWorld.
     */
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void barium$reduceAmbientParticles(int x, int y, int z, CallbackInfo ci) {
        // Verifica se o mundo é do lado do cliente para não afetar servidores
        if (!((World)(Object)this).isClient) return;

        if (!BariumConfig.C.REDUCE_AMBIENT_PARTICLES) return;

        if (((World)(Object)this).getTime() % 2 == 0) {
            ci.cancel();
        }
    }
}