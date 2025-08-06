package com.barium.client.optimization;

public class EventLoopOptimizer {

    /**
     * Substitui o ineficaz 'Thread.yield()' por um 'Thread.sleep(1)'.
     * Isso libera o núcleo da CPU para o sistema operacional, reduzindo drasticamente
     * o uso de recursos durante a espera pelo próximo frame.
     */
    public static void smartYield() {
        try {
            // Dormir por 1 milissegundo é a maneira mais eficiente de ceder tempo de CPU.
            Thread.sleep(1L);
        } catch (InterruptedException e) {
            // Se o sono for interrompido, restauramos o estado de interrupção do thread.
            Thread.currentThread().interrupt();
        }
    }
}