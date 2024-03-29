package com.minefit.xerxestireiron.farlandsagain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    protected final Messages messages = new Messages(this.getName());
    private HashMap<String, ManageFarLands> manageWorlds;
    private final ServerVersion serverVersion = new ServerVersion(this);
    private final List<String> compatibleVersions = Arrays.asList("v1_12_R1", "v1_14_R1", "v1_15_R1", "v1_16_R1",
            "v1_16_R2", "v1_16_R3", "v1_17_R1", "v1_18_R1");

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.name = getServer().getClass().getPackage().getName();
        this.version = this.name.substring(this.name.lastIndexOf('.') + 1);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.manageWorlds = new HashMap<>();

        if (!this.serverVersion.compatibleVersion(this.compatibleVersions)) {
            this.messages.incompatibleVersion();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Catches the /reload command or other things that may bypass the
        // WorldInitEvent
        for (World world : Bukkit.getWorlds()) {
            prepareWorld(world);
        }

        this.messages.pluginReady();
    }

    public boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        // Let's clean up and put the original generators back in place
        for (String worldName : this.manageWorlds.keySet()) {
            this.manageWorlds.get(worldName).restoreGenerator();
        }

        this.messages.pluginDisable();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event) {
        prepareWorld(event.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldLoadEvent event) {
        prepareWorld(event.getWorld());
    }

    public void prepareWorld(World world) {
        String worldName = world.getName();

        if (this.getConfig().getBoolean("worlds." + worldName + ".enabled", false)
                && !this.manageWorlds.containsKey(worldName)) {
            this.manageWorlds.put(worldName, new ManageFarLands(world, this));
        }
    }
}