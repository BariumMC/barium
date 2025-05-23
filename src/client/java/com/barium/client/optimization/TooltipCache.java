package com.barium.client.optimization;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache simples para componentes de tooltip.
 * Nota: Pode ser complexo e propenso a bugs se o conteúdo mudar dinamicamente.
 * Uma abordagem mais robusta envolveria hashes mais detalhados ou até mesmo caching de texturas renderizadas.
 */
public class TooltipCache {
    // Usamos ConcurrentHashMap para segurança de thread, se necessário.
    // List<Text> e Optional<TooltipData> são chaves razoáveis se seus equals/hashCode forem consistentes.
    private static final Map<List<Text>, List<TooltipComponent>> TEXT_COMPONENTS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Optional<TooltipData>, List<TooltipComponent>> DATA_COMPONENTS_CACHE = new ConcurrentHashMap<>();

    /**
     * Retorna uma lista de TooltipComponents do cache, ou a cria e armazena se não estiver presente.
     * @param lines A lista de linhas de texto do tooltip.
     * @param data Dados adicionais do tooltip (e.g., efeitos de poção).
     * @return A lista de TooltipComponents.
     */
    public static List<TooltipComponent> getOrCreateTooltipComponents(List<Text> lines, Optional<TooltipData> data) {
        // Para uma otimização mais robusta, uma combinação das linhas e dados seria a chave.
        // Aqui, tratamos separadamente, o que pode levar a um cache menos eficaz se as linhas
        // mudarem mas os dados não, ou vice-versa.
        
        // Caching para a lista de linhas
        List<TooltipComponent> cachedComponents = TEXT_COMPONENTS_CACHE.get(lines);
        if (cachedComponents != null) {
            return cachedComponents;
        }

        // Caching para dados opcionais
        List<TooltipComponent> cachedDataComponents = DATA_COMPONENTS_CACHE.get(data);
        if (cachedDataComponents != null) {
            return cachedDataComponents;
        }

        // Se não estiver no cache, crie e armazene
        List<TooltipComponent> newComponents = Lists.newArrayList();
        if (data.isPresent()) {
            // TooltipComponent.of(TooltipData) pode criar múltiplos componentes (ex: para efeitos de poção).
            // A abordagem aqui é simplificada para um único componente.
            // Para ser totalmente preciso, o Minecraft cria uma lista de TooltipComponent
            // a partir dos dados e linhas.
            newComponents.add(TooltipComponent.of(data.get()));
        }
        for (Text text : lines) {
            newComponents.add(TooltipComponent.of(text));
        }

        // Armazene no cache
        TEXT_COMPONENTS_CACHE.put(lines, newComponents);
        if (data.isPresent()) {
            DATA_COMPONENTS_CACHE.put(data, newComponents);
        }

        return newComponents;
    }

    /**
     * Limpa o cache de tooltips.
     * Deve ser chamado quando a tela de inventário muda.
     */
    public static void clearCache() {
        TEXT_COMPONENTS_CACHE.clear();
        DATA_COMPONENTS_CACHE.clear();
    }
}