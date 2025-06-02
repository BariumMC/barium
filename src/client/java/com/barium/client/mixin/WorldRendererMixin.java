package com.barium.client.mixin;

import com.barium.client.optimization.TextureOptimizer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin para NativeImageBackedTexture para interceptar e otimizar texturas antes de serem carregadas na GPU.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 * O método `upload()` (sem argumentos) é o ponto comum para a NativeImage ser enviada à GPU para a textura completa.
 */
@Mixin(NativeImageBackedTexture.class)
public abstract class TextureUtilMixin {

    // Shadow field para acessar o campo 'image' da NativeImageBackedTexture
    @Shadow @Nullable private NativeImage image;

    /**
     * Injeta no início do método `upload()` (sem argumentos) da NativeImageBackedTexture.
     * Isso permite que a `NativeImage` interna seja otimizada (ex: convertida de RGBA para RGB)
     * antes de ser efetivamente enviada para a GPU.
     *
     * Target Class: net.minecraft.client.texture.NativeImageBackedTexture
     * Target Method Signature (Yarn 1.21.5+build.1): upload()V
     */
    @Inject(
        method = "upload()V", // O método sem argumentos
        at = @At("HEAD") // Injeta no início
    )
    private void barium$optimizeImageBeforeUpload(CallbackInfo ci) {
        if (this.image != null) {
            // Chamamos nosso otimizador para processar a imagem.
            // O otimizador pode retornar a mesma imagem ou uma nova imagem otimizada.
            NativeImage optimizedImage = TextureOptimizer.optimizeTexture(this.image);

            // Se o otimizador retornou uma nova imagem (significando que a original foi fechada e substituída),
            // atualizamos o campo 'image' da instância de NativeImageBackedTexture.
            if (optimizedImage != this.image) {
                this.image = optimizedImage;
            }
        }
    }
}