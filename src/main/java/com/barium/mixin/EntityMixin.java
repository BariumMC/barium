package com.barium.mixin;

import com.barium.optimization.EntityTickOptimizer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para a classe Entity para otimizar o ticking de entidades distantes.
 * Baseado nos mappings Yarn 1.21.5+build.1
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    // Obtém acesso ao campo 'world' da classe Entity
    @Shadow public World world;

    /**
     * Injeta no início do método tick() da entidade.
     * Verifica se o tick da entidade deve ser pulado com base nas otimizações.
     *
     * Target Method Signature (Yarn 1.21.5): Lnet/minecraft/entity/Entity;tick()V
     */
    @Inject(
        method = "tick()V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$onTick(CallbackInfo ci) {
        // Converte o 'this' do mixin para a instância de Entity
        Entity self = (Entity)(Object)this;

        // Verifica se o tick deve ser pulado pelo otimizador
        if (EntityTickOptimizer.shouldSkipEntityTick(self, this.world)) {
            // Cancela a execução do método tick() original
            ci.cancel();
        }
    }
    
    /**
     * Injeta no método setRemoved() para limpar o estado da entidade no otimizador.
     * 
     * Target Method Signature (Yarn 1.21.5): Lnet/minecraft/entity/Entity;setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V
     */
    @Inject(
        method = "setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V",
        at = @At("HEAD")
    )
    private void barium$onSetRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        EntityTickOptimizer.clearEntityState(self);
    }
}
