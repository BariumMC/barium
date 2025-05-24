package com.barium.client.mixin.text;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import net.minecraft.util.profiler.Profiler; // Já deve estar importado
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.barium.client.mixin.accessor.MinecraftClientAccessor; // Adicionar este import

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

    @Inject(method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
            at = @At("HEAD"))
    private void barium$profileTextDrawStart(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int light, int overlay, CallbackInfoReturnable<Integer> cir) {
        if (BariumConfig.getInstance().TEXT_RENDERING_OPTIMIZATIONS.ENABLE_TEXT_PROFILING) {
            // CORREÇÃO: Usar o accessor
            Profiler profiler = ((MinecraftClientAccessor)MinecraftClient.getInstance()).getProfiler_();
            if (profiler != null) {
                profiler.push("barium_text_draw");
            }
        }
    }

    @Inject(method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
            at = @At("RETURN"))
    private void barium$profileTextDrawEnd(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int light, int overlay, CallbackInfoReturnable<Integer> cir) {
        if (BariumConfig.getInstance().TEXT_RENDERING_OPTIMIZATIONS.ENABLE_TEXT_PROFILING) {
            // CORREÇÃO: Usar o accessor
            Profiler profiler = ((MinecraftClientAccessor)MinecraftClient.getInstance()).getProfiler_();
            if (profiler != null) {
                profiler.pop();
            }
        }
    }
}