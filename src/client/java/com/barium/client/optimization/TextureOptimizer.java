package com.barium.client.optimization;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;

import java.io.IOException;
import java.nio.ByteBuffer; // Importar ByteBuffer

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

            // Acessa os buffers de pixel brutos
            ByteBuffer originalBuffer = originalImage.getBuffer();
            ByteBuffer newBuffer = newImage.getBuffer();

            // Garante que os buffers estão prontos para leitura/escrita
            originalBuffer.rewind();
            newBuffer.rewind();

            // Determine o número de bytes por pixel para o formato original e novo
            int originalBytesPerPixel = originalImage.getFormat().getPixelByteSize(); // Deve ser 4 para RGBA
            int newBytesPerPixel = newImage.getFormat().getPixelByteSize();         // Deve ser 3 para RGB

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Calcula o offset para o pixel na imagem original (em bytes)
                    int originalPixelOffset = (y * width + x) * originalBytesPerPixel;
                    // Calcula o offset para o pixel na nova imagem (em bytes)
                    int newPixelOffset = (y * width + x) * newBytesPerPixel;

                    // Lê os componentes RGBA do buffer original
                    // Nota: ByteBuffer.getInt() lê 4 bytes como um int. Assume Little Endian por padrão do NativeImage.
                    // O formato interno do Minecraft é ARGB (0xAARRGGBB) para getPixelColor, mas o buffer é frequentemente BGRA.
                    // Vamos ler bytes individuais para ser seguro e explícito.
                    originalBuffer.position(originalPixelOffset);
                    byte b_original = originalBuffer.get(); // B
                    byte g_original = originalBuffer.get(); // G
                    byte r_original = originalBuffer.get(); // R
                    originalBuffer.get(); // A (descartado)

                    // Converte os bytes para int (0-255)
                    int r = r_original & 0xFF;
                    int g = g_original & 0xFF;
                    int b = b_original & 0xFF;

                    // Escreve os componentes RGB no buffer da nova imagem
                    newBuffer.position(newPixelOffset);
                    newBuffer.put((byte)b); // B
                    newBuffer.put((byte)g); // G
                    newBuffer.put((byte)r); // R
                }
            }

            // É CRUCIAL fechar a imagem original para liberar sua memória nativa,
            // pois uma nova imagem foi criada para substituí-la.
            originalImage.close();

            BariumMod.LOGGER.debug("Converted texture from RGBA (32-bit) to RGB (24-bit): {}x{}", width, height);
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