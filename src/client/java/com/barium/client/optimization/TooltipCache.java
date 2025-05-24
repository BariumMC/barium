package com.barium.client.optimization;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData; // CORREÇÃO: Importa TooltipData do pacote correto
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText; // ADICIONADO: Importa OrderedText

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache simples para componentes de tooltip.
 * Este cache armazena listas de TooltipComponent geradas a partir de linhas de texto e dados opcionais.
 * A otimização visa evitar a recomputação de componentes de tooltip para itens idênticos.
 *
 * NOTA: Para uma otimização mais robusta e complexa (e.g., caching de texturas renderizadas do tooltip),
 * seria necessária uma abordagem mais profunda e com maior risco de incompatibilidade.
 */
public class TooltipCache {
    // Usamos ConcurrentHashMap para segurança de thread, mas para este caso, HashMap seria suficiente.
    // O cache armazena a lista de TooltipComponents, usando a lista de Textos como chave.
    private static final Map<List<Text>, List<TooltipComponent>> TEXT_COMPONENTS_CACHE = new ConcurrentHashMap<>();
    
    // O cache armazena a lista de TooltipComponents, usando os dados opcionais como chave.
    private static final Map<Optional<TooltipData>, List<TooltipComponent>> DATA_COMPONENTS_CACHE = new ConcurrentHashMap<>();

    /**
     * Retorna uma lista de TooltipComponents do cache, ou a cria e armazena se não estiver presente.
     * Esta função tenta primeiro encontrar uma lista de componentes no cache. Se não encontrar,
     * ela os cria e os armazena para uso futuro.
     * 
     * @param lines A lista de linhas de texto do tooltip.
     * @param data Dados adicionais do tooltip (e.g., efeitos de poção, dados de mapa).
     * @return A lista de TooltipComponents.
     */
    public static List<TooltipComponent> getOrCreateTooltipComponents(List<Text> lines, Optional<TooltipData> data) {
        // Tenta buscar no cache de componentes baseados em texto
        List<TooltipComponent> cachedComponents = TEXT_COMPONENTS_CACHE.get(lines);
        if (cachedComponents != null) {
            return cachedComponents;
        }

        // Tenta buscar no cache de componentes baseados em dados opcionais
        List<TooltipComponent> cachedDataComponents = DATA_COMPONENTS_CACHE.get(data);
        if (cachedDataComponents != null) {
            return cachedDataComponents;
        }

        // Se não estiver no cache, crie a nova lista de componentes
        List<TooltipComponent> newComponents = Lists.newArrayList();
        
        // Se houver dados opcionais (como efeitos de poção ou mapas), adicione um componente para eles.
        // Nota: TooltipComponent.of(TooltipData) pode gerar um ou mais componentes, dependendo do tipo de dado.
        if (data.isPresent()) {
            newComponents.add(TooltipComponent.of(data.get()));
        }
        
        // Adicione um componente para cada linha de texto.
        // CORREÇÃO: Use text.asOrderedText() para converter Text em OrderedText, conforme a API atual.
        for (Text text : lines) {
            newComponents.add(TooltipComponent.of(text.asOrderedText())); 
        }

        // Armazene a lista recém-criada nos caches para uso futuro.
        TEXT_COMPONENTS_CACHE.put(lines, newComponents);
        if (data.isPresent()) {
            DATA_COMPONENTS_CACHE.put(data, newComponents);
        }

        return newComponents;
    }

    /**
     * Limpa o cache de tooltips.
     * Deve ser chamado quando a tela de inventário é aberta ou fechada, ou quando o contexto
     * dos itens muda significativamente (ex: troca de mundo).
     */
    public static void clearCache() {
        TEXT_COMPONENTS_CACHE.clear();
        DATA_COMPONENTS_CACHE.clear();
    }
}