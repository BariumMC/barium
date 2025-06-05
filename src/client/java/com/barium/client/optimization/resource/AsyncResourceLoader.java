package com.barium.client.optimization.resource;

import com.barium.BariumMod;
import com.barium.config.BariumConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Gerenciador para carregamento assíncrono de recursos como texturas e modelos.
 * Utiliza um pool de threads para executar tarefas de carregamento em segundo plano.
 *
 * NOTA: A integração total com o pipeline de carregamento de recursos do Minecraft
 * pode exigir Mixins complexos em classes como `TextureManager` ou `ModelLoader`.
 * Este é um framework que oferece a capacidade de carregar assincronamente.
 */
public class AsyncResourceLoader {
    private static ExecutorService executorService;
    private static final String THREAD_NAME_FORMAT = "Barium-Resource-Loader-%d";

    /**
     * Inicializa o pool de threads para o carregamento assíncrono.
     * Deve ser chamado na inicialização do cliente do mod.
     */
    public static void init() {
        if (!BariumConfig.ENABLE_ASYNC_RESOURCE_LOADING) {
            BariumMod.LOGGER.info("Carregamento assíncrono de recursos desativado.");
            return;
        }
        if (executorService == null || executorService.isShutdown()) {
            // Cria um pool de threads com número de threads baseado nos cores da CPU,
            // mas com um limite para não sobrecarregar.
            int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4); // Max 4 threads
            executorService = Executors.newFixedThreadPool(numThreads,
                new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_FORMAT).setDaemon(true).build());
            BariumMod.LOGGER.info("AsyncResourceLoader inicializado com {} threads.", numThreads);
        }
    }

    /**
     * Envia uma tarefa de carregamento de recurso para ser executada assincronamente.
     * @param <T> O tipo do recurso a ser carregado.
     * @param supplier A função que executa o carregamento real do recurso.
     * @return Um Future que representará o resultado da tarefa.
     */
    public static <T> Future<T> submit(Supplier<T> supplier) {
        if (!BariumConfig.ENABLE_ASYNC_RESOURCE_LOADING || executorService == null || executorService.isShutdown()) {
            // Se o carregamento assíncrono estiver desativado ou o serviço não estiver pronto,
            // executa a tarefa no thread atual (sincronamente).
            return Executors.newSingleThreadExecutor().submit(supplier::get); // Retorna um Future para compatibilidade
        }
        return executorService.submit(supplier::get);
    }

    /**
     * Envia uma tarefa Runnable para ser executada assincronamente.
     * @param runnable A tarefa a ser executada.
     * @return Um Future que representa a conclusão da tarefa.
     */
    public static Future<?> submit(Runnable runnable) {
        if (!BariumConfig.ENABLE_ASYNC_RESOURCE_LOADING || executorService == null || executorService.isShutdown()) {
            return Executors.newSingleThreadExecutor().submit(runnable);
        }
        return executorService.submit(runnable);
    }

    /**
     * Desliga o pool de threads, esperando que as tarefas em andamento terminem.
     * Deve ser chamado no desligamento do cliente do mod.
     */
    public static void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            BariumMod.LOGGER.info("Desligando AsyncResourceLoader...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    BariumMod.LOGGER.warn("AsyncResourceLoader não terminou as tarefas em 60 segundos, forçando desligamento.");
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                BariumMod.LOGGER.error("AsyncResourceLoader interrompido durante o desligamento.", e);
            }
        }
    }
}
