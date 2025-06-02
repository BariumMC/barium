package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;

import java.io.IOException;

/**
 * Otimizador de texturas, responsável por converter formatos de imagem para reduzir o uso de memória da GPU.
 * Atualmente focado na conversão de RGBA para RGB565.
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

        // Se a imagem já estiver em RGB565, não fazemos nada.
        if (originalImage.getFormat() == NativeImage.Format.RGB565) {
            return originalImage;
        }

        // A otimização atual é a conversão para RGB565.
        // Adicione outras lógicas de otimização de textura aqui (ex: compressão DXT, etc.)
        // se necessário, com base no formato original.
        if (originalImage.getFormat() == NativeImage.Format.RGBA) {
            return convertToRGB565(originalImage);
        }

        // Se o formato não é RGBA e não há outra otimização específica, retorna a original.
        return originalImage;
    }

    /**
     * Converte uma NativeImage do formato RGBA para RGB565.
     * RGB565 usa 16 bits por pixel (5 bits para R, 6 para G, 5 para B), descartando o canal alfa.
     * Isso reduz o uso de memória em 50% para texturas RGBA (32 bits para 16 bits).
     *
     * @param originalImage A imagem RGBA a ser convertida.
     * @return Uma nova NativeImage no formato RGB565.
     */
    private static NativeImage convertToRGB565(NativeImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        try {
            // Cria uma nova imagem com o formato RGB565
            NativeImage newImage = new NativeImage(NativeImage.Format.RGB565, width, height, false);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int originalColor = originalImage.getPixelColor(x, y);

                    // Extrai os componentes RGBA (Minecraft usa ARGB ou BGRA internamente, mas NativeImage.getPixelColor normaliza para RGBA)
                    // Os métodos getRed, getGreen, getBlue, getAlpha são estáticos em NativeImage para interpretar o int.
                    int r = NativeImage.getRed(originalColor);
                    int g = NativeImage.getGreen(originalColor);
                    int b = NativeImage.getBlue(originalColor);
                    // O canal alfa é descartado em RGB565

                    // Converte RGBA (0-255) para RGB565 (R:5 bits, G:6 bits, B:5 bits)
                    // R: (r / 255.0) * 31 (shift left 11)
                    // G: (g / 255.0) * 63 (shift left 5)
                    // B: (b / 255.0) * 31
                    int r5 = (int) (r / 255.0F * 31.0F) & 0x1F;
                    int g6 = (int) (g / 255.0F * 63.0F) & 0x3F;
                    int b5 = (int) (b / 255.0F * 31.0F) & 0x1F;

                    // Combina os bits no formato RGB565
                    int rgb565Color = (r5 << 11) | (g6 << 5) | b5;

                    // Define a cor no novo formato
                    newImage.setPixelColor(x, y, rgb565Color);
                }
            }

            // É CRUCIAL fechar a imagem original para liberar sua memória nativa.
            // A nova imagem será gerenciada pelo ciclo de vida normal do Minecraft.
            originalImage.close();

            BariumMod.LOGGER.debug("Converted texture to RGB565: {}x{}", width, height);
            return newImage;

        } catch (IOException e) {
            BariumMod.LOGGER.error("Failed to convert texture to RGB565: {}", e.getMessage());
            // Em caso de erro, retornamos a imagem original para evitar um crash
            return originalImage;
        } catch (Exception e) {
            BariumMod.LOGGER.error("Unexpected error during texture conversion to RGB565: {}", e.getMessage());
            return originalImage;
        }
    }
}