package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.Block; // Importar Block para acesso ao getBlock()
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Remova as importações desnecessárias para o construtor
// import java.util.Map;
// import net.minecraft.state.property.Property;

/**
 * Mixin para BlockState para otimizar blocos transparentes, especialmente folhas.
 * Quando as folhas estão distantes, elas podem ser tratadas como opacas para reduzir o overhead de renderização.
 * Compatível com Minecraft 1.21.5 e Sodium.
 */
@Mixin(BlockState.class)
// REMOVIDO: Não estenda AbstractBlock.AbstractBlockState e não tenha um construtor.
// O Mixin lida com a herança e inicialização automaticamente para classes abstratas.
public abstract class TransparentBlockStateMixin {

    /**
     * Injeta no início do método `isOpaqueFullCube` para potencialmente fazer com que
     * `LeavesBlock` (blocos de folhas) distantes reportem como cubos cheios opacos.
     * Isso pode reduzir o overdraw e simplificar a renderização, fazendo com que o motor
     * de renderização (e o Sodium) os trate como blocos sólidos à distância.
     *
     * Target Method Signature (Yarn 1.21.5): isOpaqueFullCube(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z
     */
    @Inject(
        method = "isOpaqueFullCube(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z", // CORRIGIDO: Removido "In" do nome do método
        at = @At("HEAD"),
        cancellable = true // Permite que o mixin cancele a execução do método original
    )
    private void barium$isOpaqueFullCube(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // 'this' refere-se à instância de BlockState sendo processada.
        // É necessário fazer um cast para BlockState para acessar getBlock().
        Block selfBlock = ((BlockState)(Object)this).getBlock();

        // Aplica a otimização apenas se o bloco for uma instância de LeavesBlock
        if (selfBlock instanceof LeavesBlock) {
            // Verifica com o otimizador se as folhas nesta posição devem ser simplificadas
            if (TransparentBlockOptimizer.shouldSimplifyLeaves(world, pos)) {
                // Se sim, força o retorno para 'true', tratando as folhas como opacas
                cir.setReturnValue(true);
            }
        }
    }
}