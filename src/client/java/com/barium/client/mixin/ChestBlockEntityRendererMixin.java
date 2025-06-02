package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntityRenderer.class)
public abstract class ChestBlockEntityRendererMixin {

    @Unique
    private boolean barium$lastChestWasCulled = false;

    /**
     * Injeta antes da renderização de baús, aplicando otimização de culling baseada na distância.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void barium$renderChest(ChestBlockEntity chestBlockEntity, float tickDelta, MatrixStack matrixStack,
                                    VertexConsumerProvider vertexConsumerProvider, int light, int overlay,
                                    Vec3d cameraPosition, CallbackInfo ci) {
        if (!BariumConfig.ENABLE_CHEST_RENDER_OPTIMIZATION) {
            this.barium$lastChestWasCulled = false;
            return;
        }

        Vec3d chestPos = Vec3d.ofCenter(chestBlockEntity.getPos());
        double distanceSq = cameraPosition.squaredDistanceTo(chestPos);
        double maxRenderDistanceSq = BariumConfig.CHEST_RENDER_DISTANCE * BariumConfig.CHEST_RENDER_DISTANCE;

        if (distanceSq > maxRenderDistanceSq) {
            ci.cancel();
            this.barium$lastChestWasCulled = true;
        } else {
            this.barium$lastChestWasCulled = false;
        }
    }
}
