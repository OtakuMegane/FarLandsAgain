package com.minefit.xerxestireiron.farlandsagain.v1_13_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;

import net.minecraft.server.v1_13_R1.ChunkProviderServer;
import net.minecraft.server.v1_13_R1.BiomeLayout;
import net.minecraft.server.v1_13_R1.Biomes;
import net.minecraft.server.v1_13_R1.Blocks;
import net.minecraft.server.v1_13_R1.ChunkGenerator;
import net.minecraft.server.v1_13_R1.ChunkGeneratorType;
import net.minecraft.server.v1_13_R1.ChunkTaskScheduler;
import net.minecraft.server.v1_13_R1.GeneratorSettingsEnd;
import net.minecraft.server.v1_13_R1.GeneratorSettingsNether;
import net.minecraft.server.v1_13_R1.GeneratorSettingsOverworld;
import net.minecraft.server.v1_13_R1.WorldServer;

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

    public LoadFarlands(World world, ConfigurationSection worldConfig, String pluginName) {
        this.world = world;
        this.worldConfig = worldConfig;
        this.worldName = this.world.getName();
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.messages = new Messages(pluginName);
        this.configValues = new ConfigValues(this.worldName, this.worldConfig);
        this.chunkServer = this.nmsWorld.getChunkProviderServer();
        this.originalGenerator = this.nmsWorld.getChunkProviderServer().chunkGenerator;
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
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
        }

        if (!isRecognizedGenerator(environment, this.originalGenName)) {
            this.messages.unknownGenerator(this.worldName, originalGenName);
            return;
        }

        if (environment == Environment.NORMAL) {
            GeneratorSettingsOverworld generatorsettingsoverworld = new GeneratorSettingsOverworld();
            FLAChunkProviderGenerate generator = new FLAChunkProviderGenerate(this.nmsWorld,
                    BiomeLayout.d
                            .a(BiomeLayout.d.a().a(new GeneratorSettingsOverworld()).a(this.nmsWorld.getWorldData())),
                    generatorsettingsoverworld, this.configValues);
            this.enabled = setGenerator(generator);
        } else if (environment == Environment.NETHER) {
            GeneratorSettingsNether generatorsettingsnether = new GeneratorSettingsNether();
            generatorsettingsnether.a(Blocks.NETHERRACK.getBlockData());
            generatorsettingsnether.b(Blocks.LAVA.getBlockData());
            FLAChunkProviderHell generator = new FLAChunkProviderHell(this.nmsWorld,
                    BiomeLayout.c.a(BiomeLayout.c.a().a(Biomes.j)), generatorsettingsnether, this.configValues);
            this.enabled = setGenerator(generator);
        } else if (environment == Environment.THE_END) {
            GeneratorSettingsEnd generatorsettingsend = (GeneratorSettingsEnd) ChunkGeneratorType.d.a();
            generatorsettingsend.a(Blocks.END_STONE.getBlockData());
            generatorsettingsend.b(Blocks.AIR.getBlockData());
            generatorsettingsend.a(this.nmsWorld.worldProvider.d());
            FLAChunkProviderTheEnd generator = new FLAChunkProviderTheEnd(this.nmsWorld,
                    BiomeLayout.e.a(BiomeLayout.e.a().a(this.nmsWorld.getSeed())), generatorsettingsend,
                    this.configValues);
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

    private boolean setGenerator(ChunkGenerator<?> generator) {
        try {
            Field chunkGenerator = this.chunkServer.getClass().getDeclaredField("chunkGenerator");
            chunkGenerator.setAccessible(true);
            setFinal(chunkGenerator, generator, this.chunkServer);

            Field scheduler = this.chunkServer.getClass().getDeclaredField("f");
            scheduler.setAccessible(true);
            ChunkTaskScheduler taskScheduler = (ChunkTaskScheduler) scheduler.get(this.chunkServer);

            Field schedulerGenerator = taskScheduler.getClass().getDeclaredField("d");
            schedulerGenerator.setAccessible(true);
            setFinal(schedulerGenerator, generator, taskScheduler);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void setFinal(Field field, Object instance, Object obj) throws Exception {
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(instance, obj);
    }
}
