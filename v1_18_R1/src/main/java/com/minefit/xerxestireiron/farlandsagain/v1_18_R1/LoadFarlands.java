package com.minefit.xerxestireiron.farlandsagain.v1_18_R1;

import java.lang.reflect.Field;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.ReflectionHelper;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom.Algorithm;
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
        this.serverChunkCache = this.nmsWorld.getChunkSource();
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
        boolean enabled = false;

        // NOTE: Flat map type does not use noise generators for terrain so Far Lands can't be done there
        // The equivalent would just be a solid block anyway
        if (this.nmsWorld.isFlat()) {
            this.messages.providerFlat(this.worldName);
            return;
        } else if (!isRecognizedGenerator(environment, this.originalGenName)) {
            this.messages.unknownGenerator(this.worldName, this.originalGenName);
            return;
        } else {
            //int divisor = (environment == Environment.THE_END) ? 8 : 4;
            int divisor = (environment == Environment.THE_END) ? 8 : 1;
            NoiseBasedChunkGenerator noiseBasedChunkGenerator = (NoiseBasedChunkGenerator) this.originalGenerator;
            Algorithm al = noiseBasedChunkGenerator.settings.get().getRandomSource();
            // This is as good as we got for now. Still not sure it matters given how it's used.
            RandomSource randomSource = al.newInstance(this.nmsWorld.getSeed());
            NoiseSamplingSettings noiseSamplingSettings = noiseBasedChunkGenerator.settings.get().noiseSettings()
                    .noiseSamplingSettings();
            NoiseSampler noiseSampler = (NoiseSampler) noiseBasedChunkGenerator.climateSampler();

            try {
                Field blendedNoiseField = ReflectionHelper.getField(noiseSampler.getClass(), "q", true);
                blendedNoiseField.setAccessible(true);
                BlendedNoise blendedNoise = (BlendedNoise) blendedNoiseField.get(noiseSampler);
                String blendedNoiseName = blendedNoise.getClass().getSimpleName();

                if (blendedNoiseName.equals("BlendedNoise")) {
                    Field minLimitNoiseField = ReflectionHelper.getField(blendedNoise.getClass(), "a", true);
                    minLimitNoiseField.setAccessible(true);
                    PerlinNoise minLimitNoise = (PerlinNoise) minLimitNoiseField.get(blendedNoise);

                    Field maxLimitNoiseField = ReflectionHelper.getField(blendedNoise.getClass(), "b", true);
                    maxLimitNoiseField.setAccessible(true);
                    PerlinNoise maxLimitNoise = (PerlinNoise) maxLimitNoiseField.get(blendedNoise);

                    Field mainNoiseField = ReflectionHelper.getField(blendedNoise.getClass(), "c", true);
                    mainNoiseField.setAccessible(true);
                    PerlinNoise mainNoise = (PerlinNoise) mainNoiseField.get(blendedNoise);

                    Field cellWidthField = ReflectionHelper.getField(blendedNoise.getClass(), "h", true);
                    cellWidthField.setAccessible(true);
                    int cellWidth = cellWidthField.getInt(blendedNoise);

                    Field cellHeightField = ReflectionHelper.getField(blendedNoise.getClass(), "i", true);
                    cellHeightField.setAccessible(true);
                    int cellHeight = cellHeightField.getInt(blendedNoise);

                    FLA_BlendedNoise newBlendedNoise = new FLA_BlendedNoise(minLimitNoise, maxLimitNoise, mainNoise,
                            noiseSamplingSettings, cellWidth, cellHeight, randomSource, this.configValues, divisor);
                    ReflectionHelper.fieldSetter(blendedNoiseField, noiseSampler, newBlendedNoise);
                    enabled = true;
                } else {
                    this.messages.unknownNoise(worldName, blendedNoiseName);
                    return;
                }
            } catch (Throwable t) {
                t.printStackTrace();
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
