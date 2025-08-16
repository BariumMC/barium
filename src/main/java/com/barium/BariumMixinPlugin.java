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
        this.isPerformanceModLoaded = FabricLoader.getInstance().isModLoaded("sodium") ||
                                      FabricLoader.getInstance().isModLoaded("immediatelyfast");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (this.isPerformanceModLoaded) {
            // CORREÇÃO: O nome do mixin a ser desativado estava incorreto.
            if (mixinClassName.endsWith("ChunkRenderMixin")) {
                System.out.println("[Barium] Incompatible performance mod detected. Disabling mixin: " + mixinClassName);
                return false;
            }
        }
        return true;
    }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}