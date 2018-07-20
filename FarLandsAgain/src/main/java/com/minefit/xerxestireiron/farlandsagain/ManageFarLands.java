package com.minefit.xerxestireiron.farlandsagain;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ManageFarLands {

    private final FarLandsAgain plugin;
    private final World world;
    private com.minefit.xerxestireiron.farlandsagain.v1_12_R1.LoadFarlands LF12R1;
    private com.minefit.xerxestireiron.farlandsagain.v1_13_R1.LoadFarlands LF13R1;

    public ManageFarLands(World world, FarLandsAgain instance) {
        this.plugin = instance;
        this.world = world;
        ConfigurationSection worldConfig = this.plugin.getConfig()
                .getConfigurationSection("worlds." + this.world.getName());

        if (this.plugin.version.equals("v1_12_R1")) {
            this.LF12R1 = new com.minefit.xerxestireiron.farlandsagain.v1_12_R1.LoadFarlands(this.world, worldConfig,
                    this.plugin.getName());
        } else if (this.plugin.version.equals("v1_13_R1")) {
            this.LF13R1 = new com.minefit.xerxestireiron.farlandsagain.v1_13_R1.LoadFarlands(this.world, worldConfig,
                    this.plugin.getName());
        }
    }

    // Always good to clean up when disabling a plugin
    // Especially if it's a /reload command
    public void restoreGenerator() {
        if (this.plugin.version.equals("v1_12_R1")) {
            this.LF12R1.restoreGenerator();
        } else if (this.plugin.version.equals("v1_13_R1")) {
            this.LF13R1.restoreGenerator();
        }
    }
}
