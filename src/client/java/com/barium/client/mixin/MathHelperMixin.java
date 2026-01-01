package com.barium.client.mixin;

import com.barium.config.BariumConfig;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = MathHelper.class, priority = 2000)
public abstract class MathHelperMixin {

    // 2π / 65536
    private static final float RAD_TO_INDEX = 10430.378350470452f;
    private static final int SIN_MASK = 0xFFFF;

    private static final float[] SIN_TABLE = new float[65536];

    static {
        for (int i = 0; i < 65536; i++) {
            SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
        }
    }

    /**
     * @author PedrixzZDev
     * @reason Fast sine using LUT (real performance optimization)
     */
    @Overwrite
    public static float sin(float radians) {
        if (!BariumConfig.C.ENABLE_FAST_MATH) {
            return MathHelper.sin(radians); // mantém compatibilidade exata
        }

        return SIN_TABLE[(int) (radians * RAD_TO_INDEX) & SIN_MASK];
    }

    /**
     * @author PedrixzZDev
     * @reason Fast cosine using LUT (real performance optimization)
     */
    @Overwrite
    public static float cos(float radians) {
        if (!BariumConfig.C.ENABLE_FAST_MATH) {
            return MathHelper.cos(radians);
        }

        // cos(x) = sin(x + π/2)
        return SIN_TABLE[(int) (radians * RAD_TO_INDEX + 16384.0f) & SIN_MASK];
    }
}
