package com.minefit.XerxesTireIron.FarLandsAgain;

import java.util.logging.Logger;

import com.minefit.XerxesTireIron.FarLandsAgain.FarLandsAgain;

public class Messages {
    private FarLandsAgain plugin;
    private String pluginName;
    private Logger logger = Logger.getLogger("Minecraft");

    public Messages(FarLandsAgain instance) {
        this.plugin = instance;
        this.pluginName = plugin.getName();
    }

    public void unknownGenerator(String worldName, String generatorName) {
        logger.info("[" + this.pluginName + " Error] The world '" + worldName + "' does not have a recognized generator.");
        logger.info("[" + this.pluginName + " Error] A custom generator may already be in place or Mojang changed something.");
        logger.info("[" + this.pluginName + " Error] The generator detected is: '" + generatorName + "'.");
        logger.info("[" + this.pluginName + " Error] For safety, Far Lands will not be enabled for this world.");
    }

    public void unknownEnvironment(String worldName, String environment) {
        logger.info("[" + this.pluginName + " Error] The world '" + worldName + "' does not have a recognized environment type.");
        logger.info("[" + this.pluginName + " Error] The environment detected is: '" + environment + "'.");
        logger.info("[" + this.pluginName + " Error] Cannot enable Far Lands for this world.");
    }

    public void incompatibleVersion() {
        logger.info("[" + this.pluginName + " Error] This version of Minecraft is not supported. Disabling plugin.");
    }

    public void enabledSuccessfully(String worldName) {
        logger.info("[" + this.pluginName + " Success] Far Lands have been enabled for '" + worldName +  "'!");
    }

    public void pluginReady()
    {
        logger.info("[" + this.pluginName + "] Everything is ready to go!");
    }

    public void pluginDisable()
    {
        logger.info("[" + pluginName + "] " + pluginName + " now disabled.");
    }

    public void alreadyEnabled(String worldName)
    {
        logger.info("[" + this.pluginName + " Success] Far Lands appears to already be enabled for this world.");
        enabledSuccessfully(worldName);
    }
}
