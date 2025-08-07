package com.barium.client.mixin;

import com.barium.client.render.BariumRenderManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin de Tomada de Controle (VERSÃO FINAL SIMPLIFICADA E COMPILÁVEL)
 * 
 * Foca exclusivamente em interceptar o agendamento de rebuilds, que é um
 * ponto de injeção estável e confirmado.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    // Hook para limpar nosso manager quando o mundo muda.
    @Inject(method = "setWorld", at = @At("HEAD"))
    private void barium$onSetWorld(@Nullable ClientWorld newWorld, CallbackInfo ci) {
        BariumRenderManager.getInstance().onWorldChange(newWorld);
    }

    /**
     * Esta é a tomada de controle.
     * Nós interceptamos o pedido para reconstruir um chunk, passamos para nosso sistema,
     * e impedimos o Minecraft de fazer o trabalho.
     */
    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("HEAD"), cancellable = true)
    private void barium$takeOverRebuildScheduling(int x, int y, int z, boolean isPriority, CallbackInfo ci) {
        BariumRenderManager.getInstance().scheduleRebuild(x, y, z, isPriority);
        ci.cancel();
    }
}