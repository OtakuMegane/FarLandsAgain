package com.minefit.XerxesTireIron.FarLandsAgain;

public class PaperSpigot {
    private final FarLandsAgain plugin;

    public PaperSpigot(FarLandsAgain instance) {
        this.plugin = instance;
    }

    public com.destroystokyo.paper.PaperWorldConfig getPaperWorldConfig(String worldName) {
        return new com.destroystokyo.paper.PaperWorldConfig(worldName, new org.spigotmc.SpigotWorldConfig(worldName));
    }
}
