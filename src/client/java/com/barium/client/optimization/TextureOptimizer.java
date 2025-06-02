package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;

import java.io.IOException;

/**
 * Otimizador de texturas, responsável por converter formatos de imagem para reduzir o uso de memória da GPU.
 * Atualmente focado na conversão de RGBA (32-bit) para RGB (24-bit).
 * Baseado nos mappings Yarn 1.21.5+build.1.
 */
public class TextureOptimizer {

    private static boolean isSodiumLoaded = false;

    /**
     * Inicializa o TextureOptimizer.
     * Verifica se o mod Sodium está carregado para decidir a estratégia de otimização.
     */
    public static void init() {
        BariumMod.LOGGER.info("Inicializando TextureOptimizer");
        isSodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
        if (isSodiumLoaded) {
            BariumMod.LOGGER.info("Sodium detectado. As otimizações de textura do Barium podem ser menos agressivas, a menos que FORCE_LOW_RES_TEXTURES esteja ativado.");
        }
    }

    /**
     * Tenta otimizar uma NativeImage convertendo-a para um formato de menor resolução
     * se as configurações permitirem e Sodium não estiver presente/forçado.
     *
     * @param originalImage A NativeImage original a ser otimizada.
     * @return A NativeImage otimizada (ou a original se nenhuma otimização for aplicada).
     */
    public static NativeImage optimizeTexture(NativeImage originalImage) {
        if (!BariumConfig.ENABLE_TEXTURE_OPTIMIZATION || originalImage == null) {
            return originalImage; // Otimização desativada ou imagem nula
        }

        // Se Sodium está carregado e não estamos forçando baixa resolução,
        // assume que Sodium já fará otimizações melhores ou incompatíveis.
        if (isSodiumLoaded && !BariumConfig.FORCE_LOW_RES_TEXTURES) {
            return originalImage;
        }

        // O objetivo é reduzir de 32-bit RGBA. Se já não for RGBA, retorna a original.
        // O formato RGBA tem 4 bytes por pixel.
        if (originalImage.getFormat() != NativeImage.Format.RGBA) {
            return originalImage;
        }

        // A otimização atual é a conversão para RGB (24-bit) de RGBA (32-bit).
        // Isso economiza 1 byte por pixel, descartando o canal alfa.
        return convertToRGB(originalImage);
    }

    /**
     * Converte uma NativeImage do formato RGBA (32-bit) para RGB (24-bit).
     * Isso reduz o uso de memória em 25% para texturas RGBA (4 bytes para 3 bytes por pixel).
     * O canal alfa é descartado.
     *
     * @param originalImage A imagem RGBA a ser convertida.
     * @return Uma nova NativeImage no formato RGB.
     */
    private static NativeImage convertToRGB(NativeImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        try {
            // Cria uma nova imagem com o formato RGB (24-bit)
            // NativeImage.Format.RGB utiliza 3 bytes por pixel.
            NativeImage newImage = new NativeImage(NativeImage.Format.RGB, width, height, false);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Usa getPixelColor(x, y) para obter o valor do pixel (int)
                    // Este método retorna a cor no formato ARGB (0xAARRGGBB).
                    int originalColor = originalImage.getPixelColor(x, y); // CORRIGIDO: getPixelColor

                    // Extrai os componentes RGBA manualmente do int
                    int r = (originalColor >> 16) & 0xFF; // Componente Vermelho
                    int g = (originalColor >> 8) & 0xFF;  // Componente Verde
                    int b = (originalColor >> 0) & 0xFF;  // Componente Azul
                    // O canal Alfa é descartado para o formato RGB

                    // Combina os componentes R, G, B em um único int para o formato RGB (0x00RRGGBB)
                    int newColorInt = (r << 16) | (g << 8) | b;

                    // Usa setPixelColor(x, y, color) para definir o pixel
                    newImage.setPixelColor(x, y, newColorInt); // CORRIGIDO: setPixelColor
                }
            }

            // É CRUCIAL fechar a imagem original para liberar sua memória nativa,
            // pois uma nova imagem foi criada para substituí-la.
            originalImage.close();

            BariumMod.LOGGER.debug("Converted texture from RGBA to RGB (24-bit): {}x{}", width, height);
            return newImage;

        } catch (IOException e) {
            BariumMod.LOGGER.error("Failed to convert texture to RGB: {}", e.getMessage());
            return originalImage;
        } catch (Exception e) {
            BariumMod.LOGGER.error("Unexpected error during texture conversion to RGB: {}", e.getMessage());
            return originalImage;
        }
    }
}