package com.barium.client.mixin;

import com.barium.client.optimization.TextureOptimizer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture; // Importação corrigida
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable; // Importar Nullable para a anotação do shadow field

/**
 * Mixin para NativeImageBackedTexture para interceptar e otimizar texturas antes de serem carregadas na GPU.
 * Baseado nos mappings Yarn 1.21.5+build.1.
 * O método `upload` é um ponto comum para NativeImages serem enviadas à GPU.
 */
@Mixin(NativeImageBackedTexture.class)
public abstract class TextureUtilMixin { // Mantive o nome do arquivo e classe para simplicidade, mas o alvo mudou

    // Shadow field para acessar o campo 'image' da NativeImageBackedTexture
    // Note: @Nullable é uma anotação, não faz parte da assinatura do tipo para lookup de mixin,
    // mas é bom para indicar o comportamento do campo.
    @Shadow @Nullable private NativeImage image;

    /**
     * Injeta no início do método `upload` da NativeImageBackedTexture.
     * Isso permite que a `NativeImage` interna seja otimizada (ex: convertida de RGBA para RGB)
     * antes de ser efetivamente enviada para a GPU.
     *
     * Target Class: net.minecraft.client.texture.NativeImageBackedTexture
     * Target Method Signature (Yarn 1.21.5+build.1): upload(IIIIZZZ)V
     * Parâmetros: (int x, int y, int width, int height, boolean blur, boolean clamp, boolean generateMipmaps)
     */
    @Inject(
        method = "upload(IIIIZZZ)V", // Targeting NativeImageBackedTexture's upload method
        at = @At("HEAD") // Inject at the very beginning
    )
    private void barium$optimizeImageBeforeUpload(int xOffset, int yOffset, int width, int height, boolean blur, boolean clamp, boolean generateMipmaps, CallbackInfo ci) {
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