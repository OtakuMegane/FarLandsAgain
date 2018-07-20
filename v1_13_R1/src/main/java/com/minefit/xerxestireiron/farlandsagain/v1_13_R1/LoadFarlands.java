package com.minefit.xerxestireiron.farlandsagain.v1_13_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;

import net.minecraft.server.v1_13_R1.BiomeBase;
import net.minecraft.server.v1_13_R1.BiomeLayout;
import net.minecraft.server.v1_13_R1.BiomeLayoutOverworldConfiguration;
import net.minecraft.server.v1_13_R1.BiomeLayoutTheEndConfiguration;
import net.minecraft.server.v1_13_R1.Biomes;
import net.minecraft.server.v1_13_R1.Blocks;
import net.minecraft.server.v1_13_R1.ChunkGenerator;
import net.minecraft.server.v1_13_R1.ChunkGeneratorType;
import net.minecraft.server.v1_13_R1.ChunkTaskScheduler;
import net.minecraft.server.v1_13_R1.GeneratorSettingsEnd;
import net.minecraft.server.v1_13_R1.GeneratorSettingsNether;
import net.minecraft.server.v1_13_R1.GeneratorSettingsOverworld;
import net.minecraft.server.v1_13_R1.IChunkLoader;
import net.minecraft.server.v1_13_R1.SchedulerBatch;
import net.minecraft.server.v1_13_R1.WorldGenMushrooms;
import net.minecraft.server.v1_13_R1.WorldProvider;
import net.minecraft.server.v1_13_R1.WorldServer;

public class LoadFarlands {
    private final World world;
    private final WorldServer nmsWorld;
    private final String worldName;
    private String originalGenName;
    private WorldProvider worldProvider;
    private final Messages messages;
    private ChunkGenerator<?> originalGenerator;
    private final ConfigurationSection worldConfig;
    private boolean enabled = false;

    public LoadFarlands(World world, ConfigurationSection worldConfig, String pluginName) {
        this.world = world;
        this.worldConfig = worldConfig;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.worldName = this.world.getName();
        //this.configValues = new ConfigValues(this.worldName, this.worldConfig);
        this.messages = new Messages(pluginName);
        this.originalGenerator = this.nmsWorld.getChunkProviderServer().chunkGenerator;
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
        this.worldProvider = this.nmsWorld.worldProvider;
        overrideGenerator();
    }

    public void restoreGenerator() {
        if (this.enabled) {
            try {
                Field cp = net.minecraft.server.v1_13_R1.ChunkProviderServer.class.getDeclaredField("chunkGenerator");
                cp.setAccessible(true);
                setFinal(cp, this.originalGenerator, this.nmsWorld.getChunkProviderServer());
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.enabled = false;
        }
    }

    public void overrideGenerator() {
        String worldName = this.world.getName();
        Environment environment = this.world.getEnvironment();

        //String genOptions = this.nmsWorld.getWorldData().getGeneratorOptions();

        if (originalGenName.equals("FLAChunkProviderGenerate") || originalGenName.equals("FLAChunkProviderHell")
                || originalGenName.equals("FLAChunkProviderTheEnd")) {
            this.messages.alreadyEnabled(worldName);
            return;
        }

        try {
            // Fixes placing mushrooms outside of range when changing height + work around hardcoded values
            Field ah = net.minecraft.server.v1_13_R1.WorldGenerator.class.getDeclaredField("ah");
            ah.setAccessible(true);
            setFinal(ah, new FLAWorldGenMushrooms(), null);
            ah.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //BiomeBase.t();

        try {
            if (environment == Environment.NORMAL) {
                if (!originalGenName.equals("NormalChunkGenerator") && !originalGenName.equals("TimedChunkGenerator")) {
                    this.messages.unknownGenerator(worldName, originalGenName);
                    return;
                }

                GeneratorSettingsOverworld generatorsettingsoverworld = new GeneratorSettingsOverworld();
                FLAChunkProviderGenerate generator = new FLAChunkProviderGenerate(this.nmsWorld,
                        BiomeLayout.d.a(((BiomeLayoutOverworldConfiguration) BiomeLayout.d.a())
                                .a(new GeneratorSettingsOverworld()).a(this.nmsWorld.getWorldData())),
                        generatorsettingsoverworld, this.worldConfig);
                this.enabled = setGenerator(generator);
            } else if (environment == Environment.NETHER) {
                if (!originalGenName.equals("NetherChunkGenerator") && !originalGenName.equals("TimedChunkGenerator")) {
                    this.messages.unknownGenerator(worldName, originalGenName);
                    return;
                }

                GeneratorSettingsNether generatorsettingsnether = new GeneratorSettingsNether();
                generatorsettingsnether.a(Blocks.NETHERRACK.getBlockData());
                generatorsettingsnether.b(Blocks.LAVA.getBlockData());
                FLAChunkProviderHell generator = new FLAChunkProviderHell(this.nmsWorld,
                        BiomeLayout.c.a(BiomeLayout.c.a().a(Biomes.j)), generatorsettingsnether, this.worldConfig);
                this.enabled = setGenerator(generator);
            } else if (environment == Environment.THE_END) {
                if (!originalGenName.equals("SkyLandsChunkGenerator")
                        && !originalGenName.equals("TimedChunkGenerator")) {
                    this.messages.unknownGenerator(worldName, originalGenName);
                    return;
                }

                GeneratorSettingsEnd generatorsettingsend = (GeneratorSettingsEnd) ChunkGeneratorType.d.a();
                generatorsettingsend.a(Blocks.END_STONE.getBlockData());
                generatorsettingsend.b(Blocks.AIR.getBlockData());
                generatorsettingsend.a(this.nmsWorld.worldProvider.d());
                FLAChunkProviderTheEnd generator = new FLAChunkProviderTheEnd(this.nmsWorld,
                        BiomeLayout.e
                                .a(((BiomeLayoutTheEndConfiguration) BiomeLayout.e.a()).a(this.nmsWorld.getSeed())),
                        generatorsettingsend, this.worldConfig);
                this.enabled = setGenerator(generator);
            } else {
                this.messages.unknownEnvironment(worldName, environment.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.enabled) {
            this.messages.enableSuccess(this.worldName);
        } else {
            this.messages.enableFailed(this.worldName);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean setGenerator(ChunkGenerator<?> generator) {
        try {
            Field chunkGenerator = net.minecraft.server.v1_13_R1.ChunkProviderServer.class
                    .getDeclaredField("chunkGenerator");
            chunkGenerator.setAccessible(true);
            setFinal(chunkGenerator, generator, this.nmsWorld.getChunkProviderServer());

            Field chunkLoader = net.minecraft.server.v1_13_R1.ChunkProviderServer.class.getDeclaredField("chunkLoader");
            chunkLoader.setAccessible(true);
            IChunkLoader ichunkLoader = (IChunkLoader) chunkLoader.get(this.nmsWorld.getChunkProviderServer());
            ChunkTaskScheduler newScheduler = new ChunkTaskScheduler(0, this.nmsWorld, generator, ichunkLoader,
                    this.nmsWorld);

            Field scheduler = net.minecraft.server.v1_13_R1.ChunkProviderServer.class.getDeclaredField("f");
            scheduler.setAccessible(true);
            setFinal(scheduler, newScheduler, this.nmsWorld.getChunkProviderServer());
            Field g = net.minecraft.server.v1_13_R1.ChunkProviderServer.class.getDeclaredField("g");
            g.setAccessible(true);
            setFinal(g, new SchedulerBatch(newScheduler), this.nmsWorld.getChunkProviderServer());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void setFinal(Field field, Object obj, Object instance) throws Exception {
        field.setAccessible(true);

        Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(instance, obj);
    }
}
