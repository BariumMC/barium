package com.barium.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import com.barium.BariumMod; // Importar BariumMod para o LOGGER (ainda necessário para o método getConfig)

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 * Implementa ConfigData para ser reconhecido pelo AutoConfig/Cloth Config.
 */
@Config(name = "barium_optimization")
public class BariumConfig implements ConfigData {

    @ConfigEntry.Category("client_optimizations")
    @ConfigEntry.Gui.TransitiveObject
    public ClientOptimizations clientOptimizations = new ClientOptimizations();

    public static class ClientOptimizations implements ConfigData {
        @ConfigEntry.Category("entity_culling")
        @ConfigEntry.Gui.TransitiveObject
        public EntityCullingOptions entityCulling = new EntityCullingOptions();

        public static class EntityCullingOptions implements ConfigData {
            @ConfigEntry.Gui.Tooltip
            public boolean enableEntityCulling = true;

            @ConfigEntry.Gui.Tooltip(count = 2)
            @ConfigEntry.BoundedDiscrete(min = 32, max = 256)
            public int cullingDistance = 64; // Distância padrão para culling
        }
    }

    // Método de registro para a configuração (chamado em BariumMod)
    public static BariumConfig getConfig() {
        // Se a configuração ainda não foi registrada, registre-a.
        // Isso é uma medida de segurança, idealmente já deve ter sido chamada em BariumMod.onInitialize()
        if (AutoConfig.getConfigHolder(BariumConfig.class) == null) {
            AutoConfig.register(BariumConfig.class, JanksonConfigSerializer::new);
            BariumMod.LOGGER.info("Configuração do Barium registrada tardiamente (problema potencial, mas corrigido).");
        }
        return AutoConfig.getConfigHolder(BariumConfig.class).getConfig();
    }
}