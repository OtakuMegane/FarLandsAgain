package com.minefit.XerxesTireIron.FarLandsAgain;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FarLandsAgain extends JavaPlugin implements Listener {
    private String name;
    protected String version;
    private Messages messages = new Messages(this);
    private HashMap<String, ManageFarLands> manageWorlds;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        name = getServer().getClass().getPackage().getName();
        version = name.substring(name.lastIndexOf('.') + 1);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.manageWorlds = new HashMap<String, ManageFarLands>();

        if (!version.equals("v1_8_R1") && !version.equals("v1_8_R2") && !version.equals("v1_8_R3")
                && !version.equals("v1_9_R1") && !version.equals("v1_9_R2")) {
            messages.incompatibleVersion();
        } else {
            messages.pluginReady();
        }

        // Catches the /reload command or other things that may bypass the
        // WorldInitEvent
        for (World world : Bukkit.getWorlds()) {
            prepareWorld(world);
        }
    }

    @Override
    public void onDisable() {
        for (String worldName : this.manageWorlds.keySet()) {
            this.manageWorlds.get(worldName).restoreGenerator();
        }

        messages.pluginDisable();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        prepareWorld(world);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        prepareWorld(world);
    }

    public void prepareWorld(World world) {
        String worldName = world.getName();

        if (!this.getConfig().getBoolean("worlds." + worldName + ".enabled", false)) {
            return;
        }

        if (manageWorlds.containsKey(worldName)) {
            return;
        }

        this.manageWorlds.put(worldName, new ManageFarLands(world, this));
    }
}