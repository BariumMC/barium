// src/client/java/com/barium/client/mixin/accesor/BakedModelInvoker.java
package com.barium.client.mixin.accesor;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BakedModel.class)
public interface BakedModelInvoker {
    // Permite chamar o método getQuads de BakedModel
    @Invoker("getQuads")
    List<BakedQuad> invokeGetQuads(BlockState state, Direction face, Random random);
}