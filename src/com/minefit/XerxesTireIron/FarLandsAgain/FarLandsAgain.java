package com.minefit.XerxesTireIron.FarLandsAgain;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FarLandsAgain extends JavaPlugin implements Listener {
    private String name;
    private String version;
    private String pluginName;
    private Logger logger = Logger.getLogger("Minecraft");
    private Errors errors = new Errors(this);
    private HashMap<String, Boolean> worldsDone;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        name = getServer().getClass().getPackage().getName();
        version = name.substring(name.lastIndexOf('.') + 1);
        this.pluginName = this.getName();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.worldsDone = new HashMap<String, Boolean>();

        if (!version.equals("v1_8_R1") && !version.equals("v1_8_R2") && !version.equals("v1_8_R3") && !version.equals("v1_9_R1")) {
            errors.incompatibleVersion();
        } else {
            logger.info("[" + this.pluginName + "] Everything is ready to go!");
        }

        // Catches the /reload command or other things that may bypass the WorldInitEvent
        for (World world : Bukkit.getWorlds()) {
            if (this.getConfig().getBoolean("worlds." + world.getName() + ".enabled", false)) {
                initFarLands(world);
            }
        }
    }

    @Override
    public void onDisable() {
        logger.info("[" + pluginName + "] " + pluginName + " disabled!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();

        if (this.getConfig().getBoolean("worlds." + world.getName() + ".enabled", false)) {
            initFarLands(world);
        }
    }

    public void initFarLands(World world) {
        String worldName = world.getName();

        if (worldsDone.containsKey(worldName) && worldsDone.get(worldName)) {
            return;
        }

        if (version.equals("v1_8_R1")) {
            new com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R1.LoadFarlands(this, world);
            this.worldsDone.put(worldName, true);
        } else if (version.equals("v1_8_R2")) {
            new com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R2.LoadFarlands(this, world);
            this.worldsDone.put(worldName, true);
        } else if (version.equals("v1_8_R3")) {
            new com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R3.LoadFarlands(this, world);
            this.worldsDone.put(worldName, true);
        } else if (version.equals("v1_9_R1")) {
            new com.minefit.XerxesTireIron.FarLandsAgain.v1_9_R1.LoadFarlands(this, world);
            this.worldsDone.put(worldName, true);
        }
    }
}