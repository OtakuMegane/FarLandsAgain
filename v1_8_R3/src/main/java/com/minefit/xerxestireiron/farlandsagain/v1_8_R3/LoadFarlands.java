package com.minefit.xerxestireiron.farlandsagain.v1_8_R3;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;

import com.minefit.xerxestireiron.farlandsagain.v1_8_R3.FLAChunkProviderGenerate;
import com.minefit.xerxestireiron.farlandsagain.v1_8_R3.FLAChunkProviderHell;
import com.minefit.xerxestireiron.farlandsagain.v1_8_R3.FLAChunkProviderTheEnd;

import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.IChunkProvider;
import net.minecraft.server.v1_8_R3.WorldServer;

public class LoadFarlands {
    private final World world;
    private final WorldServer nmsWorld;
    private final String worldName;
    private String originalGenName;
    private final Messages messages;
    private IChunkProvider originalGenerator;
    private final ConfigurationSection worldConfig;
    private ChunkProviderServer chunkProviderServer;
    private boolean enabled = false;
    public final ConfigValues configValues;

    public LoadFarlands(World world, ConfigurationSection worldConfig, String pluginName) {
        this.world = world;
        this.worldConfig = worldConfig;
        this.worldName = this.world.getName();
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.messages = new Messages(pluginName);
        this.configValues = new ConfigValues(this.worldName, this.worldConfig);
        this.chunkProviderServer = this.nmsWorld.chunkProviderServer;
        this.originalGenerator = this.chunkProviderServer.chunkProvider;
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
        overrideGenerator();
    }

    public void restoreGenerator() {
        if (this.enabled) {
            this.chunkProviderServer.chunkProvider = this.originalGenerator;
            this.enabled = false;
        }
    }

    public void overrideGenerator() {
        boolean genFeatures = this.nmsWorld.getWorldData().shouldGenerateMapFeatures();
        long worldSeed = this.nmsWorld.getSeed();
        Environment environment = this.world.getEnvironment();
        String genOptions = this.nmsWorld.getWorldData().getGeneratorOptions();

        if (originalGenName.equals("FLAChunkProviderGenerate") || originalGenName.equals("FLAChunkProviderHell")
                || originalGenName.equals("FLAChunkProviderTheEnd")) {
            this.messages.alreadyEnabled(this.worldName);
            return;
        }

        if (!isRecognizedGenerator(environment, this.originalGenName)) {
            this.messages.unknownGenerator(this.worldName, originalGenName);
            return;
        }

        if (environment == Environment.NORMAL) {
            this.chunkProviderServer.chunkProvider = new FLAChunkProviderGenerate(this.nmsWorld, worldSeed, genFeatures,
                    genOptions, this.configValues);
        } else if (environment == Environment.NETHER) {
            this.chunkProviderServer.chunkProvider = new FLAChunkProviderHell(this.nmsWorld, genFeatures, worldSeed,
                    this.configValues);
        } else if (environment == Environment.THE_END) {
            this.chunkProviderServer.chunkProvider = new FLAChunkProviderTheEnd(this.nmsWorld, genFeatures, worldSeed,
                    this.nmsWorld.worldProvider.h(), this.configValues);
        } else {
            this.enabled = false;
            this.messages.unknownEnvironment(this.worldName, environment.toString());
        }

        if (this.enabled) {
            this.messages.enableSuccess(this.worldName);
        } else {
            this.messages.enableFailed(this.worldName);
        }
    }

    private boolean isRecognizedGenerator(Environment environment, String originalGenName) {
        if (environment == Environment.NORMAL) {
            return originalGenName.equals("NormalChunkGenerator") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.NETHER) {
            return originalGenName.equals("NetherChunkGenerator") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.THE_END) {
            return originalGenName.equals("SkyLandsChunkGenerator") || originalGenName.equals("TimedChunkGenerator");
        }

        return false;
    }
}
