package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para ChestBlockEntityRenderer para aplicar culling baseado em distância para baús.
 */
@Mixin(ChestBlockEntityRenderer.class)
public abstract class ChestBlockEntityRendererMixin {

    /**
     * Injeta no início do método de renderização do baú para verificar se ele deve ser renderizado.
     * Se o baú estiver além da distância de renderização configurada, o método de renderização original é cancelado.
     *
     * @param chestBlockEntity A instância de ChestBlockEntity a ser renderizada.
     * @param f Delta de tick.
     * @param matrixStack Pilha de matrizes para transformações de renderização.
     * @param vertexConsumerProvider Provedor de consumidores de vértice.
     * @param i Light UV.
     * @param j Overlay UV.
     * @param ci CallbackInfo para cancelar a execução original.
     */
    @Inject(
        method = "render(Lnet/minecraft/block/entity/ChestBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void barium$renderChestHead(ChestBlockEntity chestBlockEntity, float f, MatrixStack matrixStack,
                                         VertexConsumerProvider vertexConsumerProvider, int i, int j,
                                         CallbackInfo ci) {
        // Verifica se a otimização de renderização de baús está ativada na configuração
        if (!BariumConfig.ENABLE_CHEST_RENDER_OPTIMIZATION) {
            return; // Não cancela a renderização se a otimização estiver desativada
        }

        // Obtém a posição atual da câmera do jogador
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        // Obtém a posição central do baú no mundo
        Vec3d chestPos = Vec3d.ofCenter(chestBlockEntity.getPos());

        // Calcula a distância quadrada entre a câmera e o baú
        double distanceSq = cameraPos.squaredDistanceTo(chestPos);

        // Calcula a distância máxima de renderização quadrada a partir da configuração
        double maxRenderDistanceSq = (double)(BariumConfig.CHEST_RENDER_DISTANCE * BariumConfig.CHEST_RENDER_DISTANCE);

        // Se o baú estiver além da distância máxima configurada, pular sua renderização
        if (distanceSq > maxRenderDistanceSq) {
            ci.cancel(); // Cancela a execução do método de renderização original, otimizando o desempenho.
        }
    }
}