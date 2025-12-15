package com.barium.client.mixin;

import com.barium.client.util.FastMatrixUtil;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPart.class)
public class FastModelPartMixin {
    @Shadow public float pivotX;
    @Shadow public float pivotY;
    @Shadow public float pivotZ;

    @Shadow public float yaw;
    @Shadow public float pitch;
    @Shadow public float roll;

    @Shadow public float xScale;
    @Shadow public float yScale;
    @Shadow public float zScale;

    /**
     * @author JellySquid (Sodium) / Ported to Barium
     * @reason Aplica transformações de matriz muito mais rápido evitando alocações de Quaternion
     */
    @Overwrite
    public void rotate(MatrixStack matrices) {
        // Aplica translação do pivô
        matrices.translate(this.pivotX * (1.0F / 16.0F), this.pivotY * (1.0F / 16.0F), this.pivotZ * (1.0F / 16.0F));

        // Aplica rotação otimizada se houver rotação
        if (this.pitch != 0.0F || this.yaw != 0.0F || this.roll != 0.0F) {
            // Usa o utilitário rápido portado do Sodium
            FastMatrixUtil.rotateZYX(matrices.peek(), this.roll, this.yaw, this.pitch);
        }

        // Aplica escala
        if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
            matrices.scale(this.xScale, this.yScale, this.zScale);
        }
    }
}