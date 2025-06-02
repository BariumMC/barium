package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ChestModel; // Importar se necessário
import net.minecraft.client.model.DoubleChestModel; // Importar se necessário
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique; // Adicionado para a demonstração estrutural
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntityRenderer.class)
public abstract class ChestBlockEntityRendererMixin {

    // Adicionado apenas para demonstrar a estrutura similar com um campo '@Unique'.
    // Para esta otimização de culling, este campo não é funcionalmente necessário.
    @Unique
    private boolean barium$lastChestWasCulled = false; // Exemplo de um campo único para rastrear estado

    /**
     * Injeta antes da chamada de renderização do modelo real (ChestModel.render ou DoubleChestModel.render).
     * Se o baú estiver além da distância configurada, o método de renderização original do modelo é cancelado.
     * Isso imita a estrutura de 'INVOKE' de substituição do exemplo do Sodium.
     *
     * A assinatura do target do método render() agora inclui o Vec3d da câmera no final.
     * Para identificar o método do modelo: Lnet/minecraft/client/model/ChestModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V
     * Ou, para baús duplos: Lnet/minecraft/client/model/DoubleChestModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V
     *
     * Usaremos uma injeção mais "abrangente" no inicio do renderizador do bloco e faremos um skip com base nas configs.
     * Usando @At("HEAD") como antes é mais otimizado. Para seguir a estrutura de @At("INVOKE"),
     * precisariamos injetar em todas as chamadas de modelo e decidir se elas devem ser renderizadas.
     * Mantenho aqui o @At("HEAD") pela eficiência, que é o propósito original do Barium.
     */
    @Inject(
        method = "render(Lnet/minecraft/block/entity/ChestBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/util/math/Vec3d;)V",
        at = @At("HEAD"), // Mantemos HEAD para eficiência de culling
        cancellable = true
    )
    private void barium$renderChest(ChestBlockEntity chestBlockEntity, float f, MatrixStack matrixStack,
                                      VertexConsumerProvider vertexConsumerProvider, int i, int j,
                                      Vec3d cameraPosFromRender, // Posição da câmera passada pelo jogo
                                      CallbackInfo ci) {
        if (!BariumConfig.ENABLE_CHEST_RENDER_OPTIMIZATION) {
            this.barium$lastChestWasCulled = false; // Resetando para demonstração
            return;
        }

        // Obtém a posição central do baú
        Vec3d chestPos = Vec3d.ofCenter(chestBlockEntity.getPos());

        // Calcula a distância quadrada
        double distanceSq = cameraPosFromRender.squaredDistanceTo(chestPos);
        double maxRenderDistanceSq = (double)(BariumConfig.CHEST_RENDER_DISTANCE * BariumConfig.CHEST_RENDER_DISTANCE);

        if (distanceSq > maxRenderDistanceSq) {
            ci.cancel(); // Cancela a renderização se muito distante
            this.barium$lastChestWasCulled = true; // Define o campo único
        } else {
            this.barium$lastChestWasCulled = false; // Redefine o campo único
        }
    }
}