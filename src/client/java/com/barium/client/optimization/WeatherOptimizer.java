package com.barium.client.optimization;

import com.barium.config.BariumConfig;
import java.util.concurrent.ThreadLocalRandom;

public class WeatherOptimizer {

    /**
     * Determina se uma partícula de clima (chuva/neve) deve ser renderizada,
     * com base na densidade configurada.
     * @return false se a partícula deve ser pulada para reduzir a densidade.
     */
    public static boolean shouldRenderWeatherParticle() {
        if (!BariumConfig.ENABLE_WORLD_RENDERING_OPTIMIZATION || BariumConfig.WEATHER_DENSITY_LEVEL <= 0) {
            return true;
        }

        // Nível 1: Pula 1 em 4 (75% densidade)
        // Nível 2: Pula 2 em 4 (50% densidade)
        // Nível 3: Pula 3 em 4 (25% densidade)
        return ThreadLocalRandom.current().nextInt(4) >= BariumConfig.WEATHER_DENSITY_LEVEL;
    }
}