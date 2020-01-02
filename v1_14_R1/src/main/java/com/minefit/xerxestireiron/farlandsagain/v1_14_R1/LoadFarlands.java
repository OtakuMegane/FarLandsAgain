package com.minefit.xerxestireiron.farlandsagain.v1_14_R1;

import java.lang.reflect.Field;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.v1_14_R1.FLAChunkProviderGenerate;

import net.minecraft.server.v1_14_R1.ChunkProviderServer;
import net.minecraft.server.v1_14_R1.BiomeLayout;
import net.minecraft.server.v1_14_R1.Biomes;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.ChunkGenerator;
import net.minecraft.server.v1_14_R1.GeneratorSettingsEnd;
import net.minecraft.server.v1_14_R1.GeneratorSettingsNether;
import net.minecraft.server.v1_14_R1.GeneratorSettingsOverworld;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.minecraft.server.v1_14_R1.BiomeBase;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.MinecraftKey;

public class LoadFarlands {
    private final World world;
    private final WorldServer nmsWorld;
    private final String worldName;
    private String originalGenName;
    private final Messages messages;
    private ChunkGenerator<?> originalGenerator;
    private final ConfigurationSection worldConfig;
    private ChunkProviderServer chunkServer;
    private boolean enabled = false;
    public final ConfigValues configValues;
    private String worldType;

    public LoadFarlands(World world, ConfigurationSection worldConfig, String pluginName) {
        this.world = world;
        this.worldConfig = worldConfig;
        this.worldName = this.world.getName();
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.messages = new Messages(pluginName);
        this.configValues = new ConfigValues(this.worldName, this.worldConfig);
        this.chunkServer = (ChunkProviderServer) this.nmsWorld.getChunkProvider();
        this.originalGenerator = this.chunkServer.getChunkGenerator();
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
        this.worldType = this.nmsWorld.P().name();
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
        Environment environment = this.world.getEnvironment();

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
            FLAChunkProviderGenerate generator = null;

            if (this.worldType.equals("default")) {
                GeneratorSettingsOverworld generatorsettingsoverworld = new GeneratorSettingsOverworld();
                generator = new FLAChunkProviderGenerate(this.nmsWorld,
                        BiomeLayout.c
                                .a(BiomeLayout.c.a().a(generatorsettingsoverworld).a(this.nmsWorld.getWorldData())),
                        generatorsettingsoverworld, this.configValues, this.originalGenerator);
                this.enabled = setGenerator(generator);
            } else if (this.worldType.equals("buffet")) {
                MinecraftKey biomeKey = new MinecraftKey(
                        this.world.getEmptyChunkSnapshot(0, 0, true, true).getBiome(0, 0).getKey().getKey());
                BiomeBase[] biomeBase = new BiomeBase[] { IRegistry.BIOME.get(biomeKey) };
                GeneratorSettingsOverworld generatorsettingsoverworld = new GeneratorSettingsOverworld();
                generator = new FLAChunkProviderGenerate(this.nmsWorld,
                        BiomeLayout.a.a(BiomeLayout.a.a().a(biomeBase).a(biomeBase.length)), generatorsettingsoverworld,
                        this.configValues, this.originalGenerator);
                this.enabled = setGenerator(generator);
            }
        } else if (environment == Environment.NETHER) {
            GeneratorSettingsNether generatorsettingsnether = new GeneratorSettingsNether();
            generatorsettingsnether.a(Blocks.NETHERRACK.getBlockData());
            generatorsettingsnether.b(Blocks.LAVA.getBlockData());
            FLAChunkProviderHell generator = new FLAChunkProviderHell(this.nmsWorld,
                    BiomeLayout.b.a(BiomeLayout.b.a().a(Biomes.NETHER)), generatorsettingsnether, this.configValues,
                    this.originalGenerator);
            this.enabled = setGenerator(generator);
        } else if (environment == Environment.THE_END) {
            GeneratorSettingsEnd generatorsettingsend = new GeneratorSettingsEnd();
            generatorsettingsend.a(Blocks.END_STONE.getBlockData());
            generatorsettingsend.b(Blocks.AIR.getBlockData());
            generatorsettingsend.a(this.nmsWorld.worldProvider.d());
            FLAChunkProviderTheEnd generator = new FLAChunkProviderTheEnd(this.nmsWorld,
                    BiomeLayout.d.a(BiomeLayout.d.a().a(this.nmsWorld.getSeed())), generatorsettingsend,
                    this.configValues, this.originalGenerator);
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
            return originalGenName.equals("ChunkProviderGenerate") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.NETHER) {
            return originalGenName.equals("ChunkProviderHell") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.THE_END) {
            return originalGenName.equals("ChunkProviderTheEnd") || originalGenName.equals("TimedChunkGenerator");
        }

        return false;
    }

    private boolean setGenerator(ChunkGenerator<?> generator) {
        try {
            Field chunkGenerator = ReflectionHelper.getField(this.chunkServer.getClass(), "chunkGenerator", true);
            chunkGenerator.setAccessible(true);
            ReflectionHelper.setFinal(chunkGenerator, this.chunkServer, generator);

            Field chunkMapGenerator = ReflectionHelper.getField(this.chunkServer.playerChunkMap.getClass(),
                    "chunkGenerator", true);
            chunkMapGenerator.setAccessible(true);
            ReflectionHelper.setFinal(chunkMapGenerator, this.chunkServer.playerChunkMap, generator);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
