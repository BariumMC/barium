package com.barium.mixin;

import com.barium.optimization.BlockTickOptimizer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    // Injetando em um método de instância chamado "tick" que não recebe parâmetros e retorna void.
    // Este Mixin pressupõe que BlockEntity tenha um método de instância "public void tick()".
    @Inject(
        method = "tick",
        desc = "()V",     // Descritor para: void tick()
        at = @At("HEAD"),
        cancellable = true,
        require = 0       // Torna a injeção opcional; não falhará se o método não for encontrado.
                          // Remova ou ajuste se o método alvo for garantido.
    )
    private void onTick(CallbackInfo ci) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;

        if (blockEntity instanceof HopperBlockEntity) {
            HopperBlockEntity hopper = (HopperBlockEntity) blockEntity;

            // Supondo que BlockEntity tenha um método getBlockPos() ou similar.
            // Ajuste conforme necessário para obter a posição da entidade de bloco.
            if (!BlockTickOptimizer.shouldTickHopper(hopper, blockEntity.getBlockPos())) {
                 ci.cancel();
            }
        }
        // Outras otimizações para diferentes tipos de BlockEntity podem ser adicionadas aqui.
    }
}
