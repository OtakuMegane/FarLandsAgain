package com.minefit.XerxesTireIron.FarLandsAgain;

import java.util.logging.Logger;

public class Messages {
    private final String pluginName;
    private final Logger logger = Logger.getLogger("Minecraft");

    public Messages(String pluginName) {
        this.pluginName = pluginName;
    }

    public void unknownGenerator(String worldName, String generatorName) {
        this.logger.info("[" + this.pluginName + " Error] The world '" + worldName + "' does not have a recognized generator.");
        this.logger.info("[" + this.pluginName + " Error] A custom generator may already be in place or Mojang changed something.");
        this.logger.info("[" + this.pluginName + " Error] The generator detected is: '" + generatorName + "'.");
        this.logger.info("[" + this.pluginName + " Error] For safety, Far Lands will not be enabled for this world.");
    }

    public void unknownEnvironment(String worldName, String environment) {
        this.logger.info("[" + this.pluginName + " Error] The world '" + worldName + "' does not have a recognized environment type.");
        this.logger.info("[" + this.pluginName + " Error] The environment detected is: '" + environment + "'.");
        this.logger.info("[" + this.pluginName + " Error] Cannot enable Far Lands for this world.");
    }

    public void incompatibleVersion() {
        this.logger.info("[" + this.pluginName + " Error] This version of Minecraft is not supported. Disabling plugin.");
    }

    public void enabledSuccessfully(String worldName) {
        this.logger.info("[" + this.pluginName + " Success] Far Lands have been enabled for '" + worldName + "'!");
    }

    public void pluginReady() {
        this.logger.info("[" + this.pluginName + "] Everything is ready to go!");
    }

    public void pluginDisable() {
        this.logger.info("[" + this.pluginName + "] " + pluginName + " now disabled.");
    }

    public void alreadyEnabled(String worldName) {
        this.logger.info("[" + this.pluginName + " Success] Far Lands appears to already be enabled for this world.");
        enabledSuccessfully(worldName);
    }
}
