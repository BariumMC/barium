package com.barium.client.mixin;

import com.barium.client.optimization.TextureOptimizer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin para TextureUtil para interceptar e otimizar texturas antes de serem carregadas na GPU.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
@Mixin(TextureUtil.class)
public class TextureUtilMixin {

    /**
     * Intercepta a NativeImage no método uploadImage para permitir otimização (conversão de formato).
     *
     * Target Class: net.minecraft.client.texture.TextureUtil
     * Target Method Signature (Yarn 1.21.5+build.1): uploadImage(ILnet/minecraft/client/texture/NativeImage;Z)V
     * Argument to modify: 'image' (o segundo argumento, que é um NativeImage)
     */
    @ModifyVariable(
        method = "uploadImage(ILnet/minecraft/client/texture/NativeImage;Z)V",
        at = @At("HEAD"),
        argsOnly = true // Aplica a modificação apenas nos argumentos do método
    )
    private NativeImage barium$optimizeImageBeforeUpload(NativeImage image) {
        // Chamamos nosso otimizador para processar a imagem.
        // O otimizador pode retornar a mesma imagem, uma nova imagem otimizada ou null.
        // Se retornar null, o método original pode falhar ou ter comportamento inesperado,
        // então é crucial que TextureOptimizer sempre retorne uma NativeImage válida.
        return TextureOptimizer.optimizeTexture(image);
    }
}