package com.minefit.xerxestireiron.farlandsagain.v1_10_R1;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigValues {

    private final ConfigurationSection worldConfig;
    public final PaperSpigot paperSpigot;
    public final int farLandsLowX;
    public final int farLandsLowZ;
    public final int farLandsHighX;
    public final int farLandsHighZ;

    public ConfigValues(String worldName, ConfigurationSection worldConfig) {
        this.worldConfig = worldConfig;
        this.paperSpigot = new PaperSpigot(worldName, Bukkit.getName().contains("Paper"));
        this.farLandsLowX = this.worldConfig.getInt("lowX", -12550824);
        this.farLandsLowZ = this.worldConfig.getInt("lowZ", -12550824);
        this.farLandsHighX = this.worldConfig.getInt("highX", 12550824);
        this.farLandsHighZ = this.worldConfig.getInt("highZ", 12550824);
    };
}
