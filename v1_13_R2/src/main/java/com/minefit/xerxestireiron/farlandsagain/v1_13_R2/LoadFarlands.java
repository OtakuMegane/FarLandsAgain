package com.minefit.xerxestireiron.farlandsagain.v1_13_R2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;

import net.minecraft.server.v1_13_R2.ChunkProviderServer;
import net.minecraft.server.v1_13_R2.BiomeBase;
import net.minecraft.server.v1_13_R2.BiomeHell;
import net.minecraft.server.v1_13_R2.EnumCreatureType;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.WorldGenDecorator;
import net.minecraft.server.v1_13_R2.WorldGenFeatureChanceDecoratorRangeConfiguration;
import net.minecraft.server.v1_13_R2.WorldGenFeatureComposite;
import net.minecraft.server.v1_13_R2.WorldGenFeatureConfiguration;
import net.minecraft.server.v1_13_R2.WorldGenFeatureDecoratorConfiguration;
import net.minecraft.server.v1_13_R2.WorldGenFeatureMushroomConfiguration;
import net.minecraft.server.v1_13_R2.BiomeBase.BiomeMeta;
import net.minecraft.server.v1_13_R2.BiomeLayout;
import net.minecraft.server.v1_13_R2.Biomes;
import net.minecraft.server.v1_13_R2.Blocks;
import net.minecraft.server.v1_13_R2.ChunkGenerator;
import net.minecraft.server.v1_13_R2.ChunkGeneratorType;
import net.minecraft.server.v1_13_R2.ChunkTaskScheduler;
import net.minecraft.server.v1_13_R2.GeneratorSettingsEnd;
import net.minecraft.server.v1_13_R2.GeneratorSettingsFlat;
import net.minecraft.server.v1_13_R2.GeneratorSettingsNether;
import net.minecraft.server.v1_13_R2.GeneratorSettingsOverworld;
import net.minecraft.server.v1_13_R2.IRegistry;
import net.minecraft.server.v1_13_R2.WorldGenMushrooms;
import net.minecraft.server.v1_13_R2.WorldGenStage;
import net.minecraft.server.v1_13_R2.WorldGenStage.Decoration;
import net.minecraft.server.v1_13_R2.WorldGenerator;
import net.minecraft.server.v1_13_R2.WorldServer;

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
        this.chunkServer = this.nmsWorld.getChunkProviderServer();
        this.originalGenerator = this.nmsWorld.getChunkProviderServer().chunkGenerator;
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
        this.worldType = this.nmsWorld.S().name();
        System.out.println(this.worldType);
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
            FLAChunkProviderGenerate generator = null;

            if (this.worldType.equals("default")) {
                fixes(false);
                GeneratorSettingsOverworld generatorsettingsoverworld = new GeneratorSettingsOverworld();
                generator = new FLAChunkProviderGenerate(this.nmsWorld,
                        BiomeLayout.c
                                .a(BiomeLayout.c.b().a(generatorsettingsoverworld).a(this.nmsWorld.getWorldData())),
                        generatorsettingsoverworld, this.configValues);
                this.enabled = setGenerator(generator);
                fixes(false);
            }
        } else if (environment == Environment.NETHER) {
            GeneratorSettingsNether generatorsettingsnether = new GeneratorSettingsNether();
            generatorsettingsnether.a(Blocks.NETHERRACK.getBlockData());
            generatorsettingsnether.b(Blocks.LAVA.getBlockData());
            FLAChunkProviderHell generator = new FLAChunkProviderHell(this.nmsWorld,
                    BiomeLayout.b.a(BiomeLayout.b.b().a(Biomes.j)), generatorsettingsnether, this.configValues);
            this.enabled = setGenerator(generator);
        } else if (environment == Environment.THE_END) {
            GeneratorSettingsEnd generatorsettingsend = new GeneratorSettingsEnd();
            generatorsettingsend.a(Blocks.END_STONE.getBlockData());
            generatorsettingsend.b(Blocks.AIR.getBlockData());
            generatorsettingsend.a(this.nmsWorld.worldProvider.d());
            FLAChunkProviderTheEnd generator = new FLAChunkProviderTheEnd(this.nmsWorld,
                    BiomeLayout.d.a(BiomeLayout.d.b().a(this.nmsWorld.getSeed())), generatorsettingsend,
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
            setFinal(chunkGenerator, this.chunkServer, generator);

            Field scheduler = this.chunkServer.getClass().getDeclaredField("chunkScheduler");
            scheduler.setAccessible(true);
            ChunkTaskScheduler taskScheduler = (ChunkTaskScheduler) scheduler.get(this.chunkServer);

            Field schedulerGenerator = taskScheduler.getClass().getDeclaredField("d");
            schedulerGenerator.setAccessible(true);
            setFinal(schedulerGenerator, taskScheduler, generator);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean fixes(boolean restore) {
        try {
            for (MinecraftKey biomeKey : IRegistry.BIOME.keySet()) {
                BiomeBase biome = IRegistry.BIOME.get(biomeKey);
                WorldGenMushrooms shroomsInstance = null;

                if (restore) {
                    shroomsInstance = new WorldGenMushrooms();
                } else {
                    shroomsInstance = new FLAWorldGenMushrooms();
                }

                Field aW = BiomeBase.class.getDeclaredField("aW");
                aW.setAccessible(true);
                Map<WorldGenStage.Decoration, List<WorldGenFeatureComposite>> decorationMap = (Map<WorldGenStage.Decoration, List<WorldGenFeatureComposite>>) aW
                        .get(biome);

                for (Entry<Decoration, List<WorldGenFeatureComposite>> dec : decorationMap.entrySet()) {
                    for (WorldGenFeatureComposite ent : dec.getValue()) {
                        Field a = WorldGenFeatureComposite.class.getDeclaredField("a");
                        a.setAccessible(true);
                        String decName = a.get(ent).getClass().getSimpleName();

                        if (!decName.equals("WorldGenMushrooms")) {
                            continue;
                        }

                        Field b = WorldGenFeatureComposite.class.getDeclaredField("b");
                        b.setAccessible(true);
                        Field c = WorldGenFeatureComposite.class.getDeclaredField("c");
                        c.setAccessible(true);
                        Field d = WorldGenFeatureComposite.class.getDeclaredField("d");
                        d.setAccessible(true);
                        WorldGenFeatureComposite test = new WorldGenFeatureComposite(
                                (WorldGenerator) shroomsInstance, (WorldGenFeatureConfiguration) b.get(ent),
                                (WorldGenDecorator) c.get(ent), (WorldGenFeatureDecoratorConfiguration) d.get(ent));
                        List<WorldGenFeatureComposite> newCompList = new ArrayList();
                        newCompList.add(test);
                        dec.setValue(newCompList);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
