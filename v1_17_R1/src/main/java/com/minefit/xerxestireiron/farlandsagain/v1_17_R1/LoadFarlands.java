package com.minefit.xerxestireiron.farlandsagain.v1_17_R1;

import java.lang.reflect.Field;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.ReflectionHelper;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class LoadFarlands {
    private final World world;
    private final ServerLevel nmsWorld;
    private final String worldName;
    private String originalGenName;
    private final Messages messages;
    private ChunkGenerator originalGenerator;
    private final ConfigurationSection worldConfig;
    private ServerChunkCache serverChunkCache;
    public final ConfigValues configValues;
    private boolean isPaper;

    public LoadFarlands(World world, ConfigurationSection worldConfig, boolean isPaper, String pluginName) {
        this.world = world;
        this.worldConfig = worldConfig;
        this.worldName = this.world.getName();
        this.isPaper = isPaper;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.messages = new Messages(pluginName);
        this.configValues = new ConfigValues(this.worldName, this.worldConfig, this.isPaper);
        this.serverChunkCache = (ServerChunkCache) this.nmsWorld.getChunkProvider();
        this.originalGenerator = this.serverChunkCache.getGenerator();
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
        modifyGenerator();
    }

    public void restoreGenerator() {
        /*if (!setGenerator(this.originalGenerator)) {
            this.messages.restoreFailed(this.worldName);
        }*/
    }

    public void modifyGenerator() {
        Environment environment = this.world.getEnvironment();

        //NOTE: Flat map type does not use noise generators for terrain so Far Lands can't be done there
        // The equivalent would just be a solid block anyway
        boolean enabled = false;

        if (this.nmsWorld.isFlat()) {
            this.messages.providerFlat(this.worldName);
            return;
        } else if (!isRecognizedGenerator(environment, this.originalGenName)) {
            this.messages.unknownGenerator(this.worldName, this.originalGenName);
            return;
        } else {
            int divisor = (environment == Environment.THE_END) ? 8 : 4;

            try {
                Field samplerField = ReflectionHelper.getField(this.originalGenerator.getClass(), "u", true);
                samplerField.setAccessible(true);
                NoiseSampler sampler = (NoiseSampler) samplerField.get(this.originalGenerator);

                Field blendedNoiseField = ReflectionHelper.getField(sampler.getClass(), "h", true);
                blendedNoiseField.setAccessible(true);
                BlendedNoise blendedNoise = (BlendedNoise) blendedNoiseField.get(sampler);

                Field minLimitNoiseField = ReflectionHelper.getField(blendedNoise.getClass(), "a", true);
                minLimitNoiseField.setAccessible(true);
                PerlinNoise minLimitNoise = (PerlinNoise) minLimitNoiseField.get(blendedNoise);

                Field maxLimitNoiseField = ReflectionHelper.getField(blendedNoise.getClass(), "b", true);
                maxLimitNoiseField.setAccessible(true);
                PerlinNoise maxLimitNoise = (PerlinNoise) maxLimitNoiseField.get(blendedNoise);

                Field mainNoiseField = ReflectionHelper.getField(blendedNoise.getClass(), "c", true);
                mainNoiseField.setAccessible(true);
                PerlinNoise mainNoise = (PerlinNoise) mainNoiseField.get(blendedNoise);

                FLA_BlendedNoise newBlendedNoise = new FLA_BlendedNoise(minLimitNoise, maxLimitNoise, mainNoise, this.configValues, divisor);
                ReflectionHelper.setFinal(blendedNoiseField, sampler, newBlendedNoise);
                enabled = true;
            } catch (Exception e) {
                e.printStackTrace();
                enabled = false;
            }
        }

        if (enabled) {
            this.messages.enableSuccess(worldName);
        } else {
            this.messages.enableFailed(worldName);
        }
    }

    private boolean isRecognizedGenerator(Environment environment, String originalGenName) {
        if (environment == Environment.NORMAL) {
            return originalGenName.equals("ChunkGeneratorAbstract");
        } else if (environment == Environment.NETHER) {
            return originalGenName.equals("ChunkGeneratorAbstract");
        } else if (environment == Environment.THE_END) {
            return originalGenName.equals("ChunkGeneratorAbstract");
        }

        return false;
    }
}
