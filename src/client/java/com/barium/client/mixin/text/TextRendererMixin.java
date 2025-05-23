package com.barium.client.mixin.text;

import com.barium.config.BariumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

    // O TextRenderer do Minecraft já é otimizado internamente para batching via VertexConsumerProvider.
    // Melhorias significativas no texto geralmente exigem uma mudança no pipeline de renderização (e.g., fontes SDF),
    // o que Mixins não conseguem fazer diretamente.
    // Este Mixin adiciona hooks de profiling para depuração.

    /**
     * Injeta no início do método principal de desenho de texto para iniciar uma seção de profiling.
     * @param text O texto a ser desenhado.
     * @param x Posição X.
     * @param y Posição Y.
     * @param color Cor do texto.
     * @param shadow Com sombra ou não.
     * @param matrix Matriz de transformação.
     * @param vertexConsumers Provedor de consumidores de vértice.
     * @param layerType Tipo de camada de texto.
     * @param light Nível de luz.
     * @param overlay Overlay de cor.
     * @param cir CallbackInfoReturnable.
     */
    @Inject(method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
            at = @At("HEAD"))
    private void barium$profileTextDrawStart(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int light, int overlay, CallbackInfoReturnable<Integer> cir) {
        if (BariumConfig.getInstance().TEXT_RENDERING_OPTIMIZATIONS.ENABLE_TEXT_PROFILING) {
            Profiler profiler = MinecraftClient.getInstance().getProfiler();
            if (profiler != null) {
                profiler.push("barium_text_draw");
            }
        }
    }

    /**
     * Injeta no retorno do método principal de desenho de texto para finalizar a seção de profiling.
     * @param text O texto a ser desenhado.
     * @param x Posição X.
     * @param y Posição Y.
     * @param color Cor do texto.
     * @param shadow Com sombra ou não.
     * @param matrix Matriz de transformação.
     * @param vertexConsumers Provedor de consumidores de vértice.
     * @param layerType Tipo de camada de texto.
     * @param light Nível de luz.
     * @param overlay Overlay de cor.
     * @param cir CallbackInfoReturnable.
     */
    @Inject(method = "draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
            at = @At("RETURN"))
    private void barium$profileTextDrawEnd(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int light, int overlay, CallbackInfoReturnable<Integer> cir) {
        if (BariumConfig.getInstance().TEXT_RENDERING_OPTIMIZATIONS.ENABLE_TEXT_PROFILING) {
            Profiler profiler = MinecraftClient.getInstance().getProfiler();
            if (profiler != null) {
                profiler.pop();
            }
        }
    }
}