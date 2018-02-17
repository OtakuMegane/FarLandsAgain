package com.minefit.XerxesTireIron.FarLandsAgain;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ManageFarLands {

    private final FarLandsAgain plugin;
    private final World world;
    private com.minefit.XerxesTireIron.FarLandsAgain.v1_10_R1.LoadFarlands LF10R1;
    private com.minefit.XerxesTireIron.FarLandsAgain.v1_11_R1.LoadFarlands LF11R1;
    private com.minefit.XerxesTireIron.FarLandsAgain.v1_12_R1.LoadFarlands LF12R1;

    public ManageFarLands(World world, FarLandsAgain instance) {
        this.plugin = instance;
        this.world = world;
        ConfigurationSection worldConfig = this.plugin.getConfig().getConfigurationSection("worlds." + this.world.getName());

        if (this.plugin.version.equals("v1_10_R1")) {
            this.LF10R1 = new com.minefit.XerxesTireIron.FarLandsAgain.v1_10_R1.LoadFarlands(this.world, worldConfig, this.plugin.getName());
        } else if (this.plugin.version.equals("v1_11_R1")) {
            this.LF11R1 = new com.minefit.XerxesTireIron.FarLandsAgain.v1_11_R1.LoadFarlands(this.world, worldConfig, this.plugin.getName());
        } else if (this.plugin.version.equals("v1_12_R1")) {
            this.LF12R1 = new com.minefit.XerxesTireIron.FarLandsAgain.v1_12_R1.LoadFarlands(this.world, worldConfig, this.plugin.getName());
        }
    }

    // Always good to clean up when disabling a plugin
    // Especially if it's a /reload command
    public void restoreGenerator() {
if (this.plugin.version.equals("v1_10_R1")) {
            this.LF10R1.restoreGenerator();
        } else if (this.plugin.version.equals("v1_11_R1")) {
            this.LF11R1.restoreGenerator();
        } else if (this.plugin.version.equals("v1_12_R1")) {
            this.LF12R1.restoreGenerator();
        }
    }
}
