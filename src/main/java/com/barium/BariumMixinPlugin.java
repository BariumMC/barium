// --- Crie este novo arquivo em: src/main/java/com/barium/BariumMixinPlugin.java ---
// (Note: Coloque na pasta 'main', não 'client', pois o plugin é carregado antes)
package com.barium;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class BariumMixinPlugin implements IMixinConfigPlugin {

    private boolean isSodiumLoaded = false;

    @Override
    public void onLoad(String mixinPackage) {
        // Verifica se o Sodium está carregado assim que o plugin é inicializado
        this.isSodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Se o Sodium estiver carregado, desativa nosso mixin que conflita com ele.
        if (this.isSodiumLoaded && mixinClassName.endsWith("BuiltChunkRebuildMixin")) {
            System.out.println("[Barium] Sodium detected. Disabling incompatible mixin: " + mixinClassName);
            return false; // Não aplique este mixin
        }

        // Para todos os outros mixins, aplique normalmente.
        return true;
    }

    // --- Outros métodos da interface (podem ser deixados vazios) ---

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}