package com.barium.client.util;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.barium.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class RendererDetector {

    private static boolean detected = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (detected) return;
            tryDetectAndApply();
        });
    }

    private static void tryDetectAndApply() {
        try {
            String renderer = GL11.glGetString(GL11.GL_RENDERER);
            if (renderer == null) return;
            String lower = renderer.toLowerCase();
            if (lower.contains("llvmpipe") || lower.contains("softpipe") || lower.contains("software")) {
                BariumMod.LOGGER.warn("Detected software renderer: {}. Applying LLVMpipe presets.", renderer);
                applyLLVMPipePresets();
                ConfigManager.saveConfig();
                detected = true;
            } else {
                // Not an llvmpipe renderer; mark detection as done to avoid repeated checks.
                detected = true;
            }
        } catch (Throwable t) {
            // GL context might not yet be available; ignore and try again later.
        }
    }

    private static void applyLLVMPipePresets() {
        BariumConfig.C.ENABLE_LLVMPIPE_MODE = true;

        // Conservative presets for software rasterizers
        BariumConfig.C.DISABLE_TRANSLUCENT_RENDERING = true;
        BariumConfig.C.DISABLE_TEXTURE_ANIMATIONS = true;
        BariumConfig.C.ENABLE_HALF_RESOLUTION_ENTITY_OUTLINES = true;
        BariumConfig.C.ENABLE_PARTICLE_OPTIMIZATION = true;
        BariumConfig.C.ENABLE_GLOBAL_PARTICLE_LIMIT = true;
        BariumConfig.C.MAX_GLOBAL_PARTICLES = Math.max(512, BariumConfig.C.MAX_GLOBAL_PARTICLES / 4);
        BariumConfig.C.MAX_CHUNK_UPLOADS_PER_FRAME = 1;
        BariumConfig.C.MIPMAP_LEVEL_OVERRIDE = Math.max(2, BariumConfig.C.MIPMAP_LEVEL_OVERRIDE);
        BariumConfig.C.ENABLE_CHUNK_UPDATE_THROTTLING = true;
        BariumConfig.C.ENABLE_FOREST_SECTION_CULLING = true;
        BariumConfig.C.ENABLE_EMPTY_CHUNK_SECTION_CULLING = true;
        BariumConfig.C.ENABLE_OCCLUSION_CULLING = true;
        BariumConfig.C.ENABLE_PARTICLE_FRUSTUM_CULLING = true;
        BariumConfig.C.REDUCE_AMBIENT_PARTICLES = true;
        BariumConfig.C.ENABLE_ENTITY_CULLING = true;

        BariumMod.LOGGER.info("LLVMpipe presets applied to configuration.");
    }
}
