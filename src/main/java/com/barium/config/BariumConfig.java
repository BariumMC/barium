package com.barium.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import com.barium.BariumMod; // Importar BariumMod para o LOGGER

/**
 * Configuração central para o mod Barium.
 * Contém todas as configurações para os diferentes sistemas de otimização.
 * Implementa ConfigData para ser reconhecido pelo AutoConfig/Cloth Config.
 */
@Config(name = "barium_optimization")
public class BariumConfig implements ConfigData {

}