package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.client.render.FrameGraphBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(FrameGraphBuilder.class)
public abstract class FrameGraphBuilderMixin {

    @Shadow
    private final List<?> passes;

    // Remove a verificação de recursos que consome 40%+ da CPU
    @Inject(method = "checkResources", at = @At("HEAD"), cancellable = true)
    private void barium$disableResourceChecking(Collection<?> passes, CallbackInfo ci) {
        // Se a otimização estiver ligada, cancelamos a validação pesada.
        // Isso é seguro em produção.
        if (BariumConfig.C.ENABLE_AGGRESSIVE_OPTIMIZATION) {
            ci.cancel();
        }
    }

    // Otimiza a alocação da lista para evitar redimensionamento de array a cada frame
    @Redirect(
        method = "run(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/FrameGraphBuilder$Profiler;)V",
        at = @At(value = "NEW", target = "java/util/ArrayList")
    )
    private ArrayList<?> barium$optimizeListAllocation(int initialCapacity) {
        return new ArrayList<>(this.passes.size());
    }
}
