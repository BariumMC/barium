package com.barium.client.mixin;

import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Direction.class)
public class FastDirectionMixin {
    /**
     * @author JellySquid (Sodium) / Ported to Barium
     * @reason Evita loops e produtos escalares caros substituindo por comparações absolutas simples.
     */
    @Overwrite
    public static Direction getFacing(float x, float y, float z) {
        float yM = Math.abs(y);
        float zM = Math.abs(z);
        float xM = Math.abs(x);

        if (yM >= zM) {
            if (yM >= xM) {
                // Y é o maior eixo
                return y <= 0 ? Direction.DOWN : Direction.UP;
            }
            // ZM <= YM < XM -> X é maior
        } else {
            if (zM >= xM) {
                // Z é o maior eixo
                return z <= 0 ? Direction.NORTH : Direction.SOUTH;
            }
            // YM < ZM < XM -> X é maior
        }

        // X é o maior eixo
        return x <= 0 ? Direction.WEST : Direction.EAST;
    }
}