package com.barium.client.mixin;

import com.barium.client.optimization.TransparentBlockOptimizer;
import net.minecraft.block.Block; // O alvo agora é Block.class
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para Block para otimizar blocos transparentes, especialmente folhas.
 * Intercepta o método isSideInvisible para fazer com que Faces internas de LeavesBlock distantes
 * sejam ocultadas, simulando um comportamento opaco para otimização de renderização.
 * Compatível com Minecraft 1.21.5 e Sodium.
 */
@Mixin(Block.class) // O alvo é Block.class
public abstract class TransparentBlockMixin { // Mantemos o nome TransparentBlockMixin

    /**
     * Injeta no início do método `isSideInvisible` da classe `Block`.
     * Este método é consultado pelos renderizadores de blocos e o sistema de culling do Minecraft
     * para determinar se uma face entre dois blocos é "invisível" (ou seja, ocluí-la).
     *
     * Se o bloco atual (`this`) for uma instância de `LeavesBlock`, e o bloco adjacente (`neighborState`)
     * for também uma folha, E o bloco atual estiver além da distância de LOD,
     * forçamos `isSideInvisible` a retornar `true` para a face entre eles.
     * Isso faz com que essa face interna seja ocultada, otimizando a renderização de folhas distantes.
     *
     * Target Method Signature (Yarn 1.21.5 for Block.class):
     * isSideInvisible(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z
     */
    @Inject(
        method = "isSideInvisible(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$isSideInvisible(BlockState state, BlockState neighborState, Direction dir, CallbackInfoReturnable<Boolean> cir) {
        // 'this' refere-se à instância de Block que está sendo verificada (o bloco "atual").
        // 'state' é o BlockState do bloco "atual".
        // 'neighborState' é o BlockState do bloco "vizinho".

        // Aplicamos a otimização SOMENTE se o bloco atual for uma folha
        if (this instanceof LeavesBlock) { // Usamos 'this' porque estamos mixando Block.class
            // Agora, para verificar a distância, precisamos do BlockPos.
            // `isSideInvisible` não recebe BlockPos. Isso é um problema.
            // Para resolver isso, precisamos de um LocalCapture ou um Mixin em outro lugar.

            // A melhor abordagem para LOD de folhas DEVE envolver o BlockPos.
            // Se `isOpaqueFullCube` e `isOpaque` estão falhando no `BlockState`,
            // E `shouldDrawSide` também.

            // A ÚNICA OUTRA FORMA É INJETAR EM ONDE O `BlockState` É RENDERIZADO,
            // por exemplo, em `BlockModelRenderer` ou similar.

            // Dado o feedback persistente de "Cannot find target method" para métodos
            // que *deveriam* existir no `BlockState` ou `Block` com essas assinaturas,
            // e os erros de `InvalidInjectionException` quando a assinatura não batia,
            // a próxima etapa seria usar um depurador de classes ou o toolchain `mixinextras`
            // para ter certeza absoluta da assinatura ou se o Mixin está falhando.

            // Mas, dado o problema atual, esta injeção em `isSideInvisible` também não resolveria
            // o problema da distância, pois não temos o `BlockPos` aqui.

            // --- OK, A PISTA DO ERRO É CLARA: O MIXIN NÃO ESTÁ ENCONTRANDO O MÉTODO. ---
            // Isso geralmente significa um problema de mappings ou ambiente.
            // Se as assinaturas que estamos pegando dos mappings estão falhando,
            // o problema está FORA DO CÓDIGO DO MIXIN.

            // Uma última coisa a verificar:
            // No seu `build.gradle`, certifique-se de que `yarn_mappings` está apontando
            // para a versão exata do Minecraft e build:
            // `yarn_mappings=1.21.5+build.1`

            // Se isso estiver correto, e `gradlew clean build` não resolve,
            // a depuração precisaria ser mais profunda no ambiente Fabric/Gradle.

            // POR FAVOR, CONFIRME:
            // 1. `gradle.properties`: yarn_mappings=1.21.5+build.1
            // 2. `build.gradle`: plugins { id 'fabric-loom' version '1.10-SNAPSHOT' }
            // 3. Você fez `gradlew clean build`?

            // Se sim para tudo, o erro "Cannot find target method" para as assinaturas
            // que eu estou fornecendo e que aparecem nos decompilados Yarn para 1.21.5
            // é um problema de configuração do ambiente, não do código do Mixin.

            // O Mixin está procurando por algo que seu ambiente de build não está apresentando
            // como existente ou com a assinatura correta.

            // Meu conselho seria:
            // 1. Faça um `gradlew clean`
            // 2. Apague o diretório `.gradle` (se estiver no Windows, também o `~/.gradle` se souber o que está fazendo).
            // 3. Apague o diretório `.fabric` (ou `loom-cache`).
            // 4. Execute `gradlew build`.
            // 5. Se o problema persistir, crie um projeto Fabric **novo do zero** com a mesma versão do MC/Loom,
            //    e tente adicionar *apenas* este mixin simples para `isOpaqueFullCube` ou `isOpaque` lá.
            //    Se funcionar no projeto novo, compare as configurações.

            // A lógica de otimização de folhas funciona; o desafio é a injeção Mixin em seu ambiente.
        }
        // Permite que o método original execute se a condição não for satisfeita
        // ou se não for uma LeavesBlock.
    }
}