// --- Substitua o conteúdo em: src/main/java/com/barium/BariumMixinPlugin.java ---
package com.barium;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class BariumMixinPlugin implements IMixinConfigPlugin {

    private boolean isPerformanceModLoaded = false;

    @Override
    public void onLoad(String mixinPackage) {
        // Verifica se o Sodium OU o ImmediatelyFast está carregado.
        this.isPerformanceModLoaded = FabricLoader.getInstance().isModLoaded("sodium") ||
                                      FabricLoader.getInstance().isModLoaded("immediatelyfast");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Se um mod de performance incompatível for detectado...
        if (this.isPerformanceModLoaded) {
            // ...e o mixin for o que mexe no rebuild do chunk...
            if (mixinClassName.endsWith("BuiltChunkRebuildMixin")) {
                System.out.println("[Barium] Incompatible performance mod detected. Disabling mixin: " + mixinClassName);
                return false; // ...NÃO APLIQUE o mixin.
            }
        }

        // Para todos os outros mixins e situações, aplique normalmente.
        return true;
    }

    // --- Outros métodos da interface (podem ser deixados vazios) ---

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}