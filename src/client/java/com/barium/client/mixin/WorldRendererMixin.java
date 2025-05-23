package com.barium.client.mixin;

import com.barium.BariumMod;
import com.barium.client.optimization.ClientTerrainOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private MinecraftClient client;

    // A injeção em "renderBuiltChunk", "drawSection", "renderChunk" foi removida
    // pois o nome do método é instável e causa erros de compilação.
    // A lógica de culling de renderização será movida para BuiltChunkMixin.java

    // Este mixin (setupTerrain) ainda é válido e não deve causar problemas.
    @ModifyVariable(method = "setupTerrain", at = @At(value = "STORE", ordinal = 0), name = "builtChunk", allow = 1)
    private BuiltChunk barium$modifyBuiltChunkForRebuild(BuiltChunk builtChunk) {
        // Esta variável é um BuiltChunk, que será processado pelo BuiltChunkMixin
        // para decidir se deve ser reconstruído.
        return builtChunk;
    }
}