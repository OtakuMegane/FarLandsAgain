package com.minefit.xerxestireiron.farlandsagain.v1_12_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.ReflectionHelper;

import net.minecraft.server.v1_12_R1.ChunkGenerator;
import net.minecraft.server.v1_12_R1.ChunkProviderServer;
import net.minecraft.server.v1_12_R1.WorldServer;

public class LoadFarlands {
    private final World world;
    private final WorldServer nmsWorld;
    private final String worldName;
    private String originalGenName;
    private final Messages messages;
    private ChunkProviderServer chunkServer;
    private ChunkGenerator originalGenerator;
    private final ConfigurationSection worldConfig;
    public final ConfigValues configValues;
    private boolean enabled = false;
    private String worldType;
    private final boolean isPaper;

    public LoadFarlands(World world, ConfigurationSection worldConfig, boolean isPaper, String pluginName) {
        this.world = world;
        this.worldConfig = worldConfig;
        this.worldName = this.world.getName();
        this.isPaper = isPaper;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.messages = new Messages(pluginName);
        this.configValues = new ConfigValues(this.worldName, this.worldConfig, this.isPaper);
        this.chunkServer = this.nmsWorld.getChunkProviderServer();
        this.originalGenerator = this.chunkServer.chunkGenerator;
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
        this.worldType = this.nmsWorld.N().name();
        overrideGenerator();
    }

    public void restoreGenerator() {
        if (this.enabled) {
            if (!setGenerator(this.originalGenerator)) {
                this.messages.restoreFailed(this.worldName);
            }

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
        } else if (originalGenName.equals("ChunkProviderFlat")) {
            this.messages.providerFlat(this.worldName);
            return;
        }

        if (!isRecognizedGenerator(environment, this.originalGenName)) {
            this.messages.unknownGenerator(this.worldName, originalGenName);
            return;
        }

        if (environment == Environment.NORMAL) {
                FLAChunkProviderGenerate generator = new FLAChunkProviderGenerate(this.nmsWorld, worldSeed, genFeatures,
                        genOptions, this.configValues);
                this.enabled = setGenerator(generator);
        } else if (environment == Environment.NETHER) {
            FLAChunkProviderHell generator = new FLAChunkProviderHell(this.nmsWorld, genFeatures, worldSeed,
                    this.configValues);
            this.enabled = setGenerator(generator);
        } else if (environment == Environment.THE_END) {
            FLAChunkProviderTheEnd generator = new FLAChunkProviderTheEnd(this.nmsWorld, genFeatures, worldSeed,
                    this.nmsWorld.worldProvider.h(), this.configValues);
            this.enabled = setGenerator(generator);
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

    private boolean setGenerator(ChunkGenerator generator) {
        try {
            Field chunkGenerator = this.chunkServer.getClass().getDeclaredField("chunkGenerator");
            chunkGenerator.setAccessible(true);
            ReflectionHelper.setFinal(chunkGenerator, this.chunkServer, generator);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
