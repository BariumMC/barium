package com.barium.mixin;

import com.barium.optimization.ChunkSavingOptimizer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    
    @Inject(method = "save", at = @At("HEAD"))
    private void onSave(CallbackInfo ci) {
        ServerWorld world = (ServerWorld)(Object)this;
        
        // Processa o salvamento de chunks enfileirados
        ChunkSavingOptimizer.processChunkSaving(world);
    }
}
