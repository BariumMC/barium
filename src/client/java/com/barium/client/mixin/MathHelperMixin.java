package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MathHelper.class)
public class MathHelperMixin {

    private static final float[] SIN_TABLE_FAST = new float[65536];

    static {
        for (int i = 0; i < 65536; ++i) {
            SIN_TABLE_FAST[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
        }
    }

    /**
     * @author PedrixzZDev
     * @reason Otimização de seno usando tabela de lookup rápida
     */
    @Overwrite
    public static float sin(float value) {
        if (!BariumConfig.C.ENABLE_FAST_MATH) {
            return (float) Math.sin(value);
        }
        return SIN_TABLE_FAST[(int) (value * 10430.378f) & 0xFFFF];
    }

    /**
     * @author Barium
     * @reason Otimização de cosseno usando tabela de lookup rápida
     */
    @Overwrite
    public static float cos(float value) {
        if (!BariumConfig.C.ENABLE_FAST_MATH) {
            return (float) Math.cos(value);
        }
        return SIN_TABLE_FAST[(int) (value * 10430.378f + 16384.0f) & 0xFFFF];
    }
}