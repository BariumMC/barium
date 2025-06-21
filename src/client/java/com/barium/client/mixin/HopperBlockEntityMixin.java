// --- Substitua o conteúdo em: src/client/java/com/barium/client/mixin/HopperBlockEntityMixin.java ---
package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    /**
     * A solução final e correta para otimizar funis no lado do cliente.
     * Injetamos no início do método `clientTick`, que é público e estático.
     * Nosso método handler também é estático, o que resolve todos os erros de compilação anteriores.
     * O próprio método `clientTick` nos fornece todos os parâmetros de que precisamos (world, pos).
     */
    @Inject(
        method = "clientTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/HopperBlockEntity;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void barium$cullHopperClientTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        // Verificação inicial: só executa se a otimização estiver ligada.
        if (!BariumConfig.C.ENABLE_HOPPER_TICK_CULLING) {
            return;
        }

        // Pega o jogador do cliente.
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return; // Segurança: se não houver jogador, não faz nada.
        }

        // Calcula a distância ao quadrado entre o jogador e o funil.
        double distanceSq = client.player.getPos().squaredDistanceTo(pos.toCenterPos());

        // Se a distância for maior que a configurada, cancela o tick do funil.
        // Isso impede animações e outras lógicas do lado do cliente.
        if (distanceSq > BariumConfig.C.HOPPER_TICK_CULLING_DISTANCE_SQ) {
            ci.cancel();
        }
    }
}