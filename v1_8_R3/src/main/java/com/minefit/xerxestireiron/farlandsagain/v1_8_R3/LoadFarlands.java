package com.minefit.xerxestireiron.farlandsagain.v1_8_R3;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.v1_8_R3.FLAChunkProviderGenerate;
import com.minefit.xerxestireiron.farlandsagain.v1_8_R3.FLAChunkProviderHell;
import com.minefit.xerxestireiron.farlandsagain.v1_8_R3.FLAChunkProviderTheEnd;

import net.minecraft.server.v1_8_R3.IChunkProvider;
import net.minecraft.server.v1_8_R3.WorldServer;

public class LoadFarlands {
    private final World world;
    private final WorldServer nmsWorld;
    private final Messages messages;
    private IChunkProvider originalGenerator;
    private final ConfigurationSection worldConfig;

    public LoadFarlands(World world, ConfigurationSection worldConfig, String pluginName) {
        this.world = world;
        this.worldConfig = worldConfig;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.messages = new Messages(pluginName);
        overrideGenerator();
    }

    public void restoreGenerator() {
        this.nmsWorld.chunkProviderServer.chunkProvider = this.originalGenerator;
    }

    public void overrideGenerator() {
        String worldName = this.world.getName();
        long worldSeed = this.world.getSeed();
        this.originalGenerator = this.nmsWorld.chunkProviderServer.chunkProvider;
        String originalGenName = this.originalGenerator.getClass().getSimpleName();
        boolean genFeatures = this.nmsWorld.getWorldData().shouldGenerateMapFeatures();
        String genOptions = this.nmsWorld.getWorldData().getGeneratorOptions();
        Environment environment = this.world.getEnvironment();

        if (originalGenName.equals("FLAChunkProviderGenerate") || originalGenName.equals("FLAChunkProviderHell")
                || originalGenName.equals("FLAChunkProviderTheEnd")) {
            this.messages.alreadyEnabled(worldName);
            return;
        }

        if (environment == Environment.NORMAL) {
            if (!originalGenName.equals("NormalChunkGenerator")) {
                this.messages.unknownGenerator(worldName, originalGenName);
                return;
            }

            this.nmsWorld.chunkProviderServer.chunkProvider = new FLAChunkProviderGenerate(this.nmsWorld, worldSeed, genFeatures,
                    genOptions, this.worldConfig);
        } else if (environment == Environment.NETHER) {
            if (!originalGenName.equals("NetherChunkGenerator")) {
                this.messages.unknownGenerator(worldName, originalGenName);
                return;
            }

            this.nmsWorld.chunkProviderServer.chunkProvider = new FLAChunkProviderHell(this.nmsWorld, genFeatures, worldSeed,
                    this.worldConfig);
        } else if (environment == Environment.THE_END) {
            if (!originalGenName.equals("SkyLandsChunkGenerator")) {
                this.messages.unknownGenerator(worldName, originalGenName);
                return;
            }

            this.nmsWorld.chunkProviderServer.chunkProvider = new FLAChunkProviderTheEnd(this.nmsWorld, genFeatures, worldSeed,
                    this.nmsWorld.worldProvider.h(), this.worldConfig);
        } else {
            this.messages.unknownEnvironment(worldName, environment.toString());
        }

        messages.enabledSuccessfully(worldName);
    }
}
