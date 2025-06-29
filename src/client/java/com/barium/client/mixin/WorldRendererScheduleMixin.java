package com.barium.client.mixin;

import com.barium.client.util.ChunkVisibilityManager;
import com.barium.config.BariumConfig;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererScheduleMixin {

    @Shadow private ClientWorld world;

    /**
     * Esta é a implementação definitiva da otimização de seções.
     * Injetamos no início do método que agenda uma reconstrução de chunk.
     * Se a seção que seria reconstruída não for visível de acordo com nosso manager,
     * nós simplesmente cancelamos a chamada. A tarefa de reconstrução nunca é criada.
     */
    @Inject(
            method = "scheduleChunkRender(IIIZ)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void barium$preventSchedulingOfUnseenSections(int x, int y, int z, boolean important, CallbackInfo ci) {
        if (!BariumConfig.C.ENABLE_ADVANCED_SECTION_CULLING || this.world == null) {
            return;
        }

        // As coordenadas recebidas são de bloco, então as convertemos para coordenadas de seção.
        int sectionX = x >> 4;
        int sectionY = this.world.getSectionIndex(y);
        int sectionZ = z >> 4;

        // Pergunta ao manager se esta seção é visível.
        if (!ChunkVisibilityManager.getInstance().isSectionPotentiallyVisible(sectionX, sectionY, sectionZ)) {
            // Se não for, cancela o agendamento. Simples e eficaz.
            ci.cancel();
        }
    }
}