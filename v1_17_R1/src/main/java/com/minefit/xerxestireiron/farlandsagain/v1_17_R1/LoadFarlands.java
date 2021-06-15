package com.minefit.xerxestireiron.farlandsagain.v1_17_R1;

import java.lang.reflect.Field;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.ReflectionHelper;

import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorOctaves;

public class LoadFarlands {
    private final World world;
    private final WorldServer nmsWorld;
    private final String worldName;
    private String originalGenName;
    private final Messages messages;
    private ChunkGenerator originalGenerator;
    private final ConfigurationSection worldConfig;
    private ChunkProviderServer chunkServer;
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
        this.chunkServer = (ChunkProviderServer) this.nmsWorld.getChunkProvider();
        this.originalGenerator = this.chunkServer.getChunkGenerator();
        this.originalGenName = this.originalGenerator.getClass().getSimpleName();
        overrideGenerator();
    }

    public void restoreGenerator() {
        /*if (!setGenerator(this.originalGenerator)) {
            this.messages.restoreFailed(this.worldName);
        }*/
    }

    public void overrideGenerator() {
        Environment environment = this.world.getEnvironment();

        //NOTE: Flat map type does not use noise generators for terrain so Far Lands can't be done there
        // The equivalent would just be a solid block anyway
        boolean enabled = false;

        if (this.nmsWorld.isFlatWorld()) {
            this.messages.providerFlat(this.worldName);
            return;
        } else if (!isRecognizedGenerator(environment, this.originalGenName)) {
            this.messages.unknownGenerator(this.worldName, this.originalGenName);
            return;
        } else {
            int divisor = (environment == Environment.THE_END) ? 8 : 4;

                /*if (this.isPaper) {
                    enabled = setGenerator(new FLA_ChunkGeneratorAbstract_Paper(chunkManager, this.nmsWorld.getSeed(),
                            h, this.configValues, this.originalGenerator, divisor));
                } else {
                    enabled = setGenerator(new FLA_ChunkGeneratorAbstract(chunkManager, this.nmsWorld.getSeed(), h,
                            this.configValues, this.originalGenerator, divisor));
                }*/


            try {
                Field uField = ReflectionHelper.getField(this.originalGenerator.getClass(), "u", true);
                uField.setAccessible(true);
                NoiseSampler sampler = (NoiseSampler) uField.get(this.originalGenerator);

                Field blendedNoiseField = ReflectionHelper.getField(sampler.getClass(), "h", true);
                blendedNoiseField.setAccessible(true);
                BlendedNoise blendedNoise = (BlendedNoise) blendedNoiseField.get(sampler);

                Field aField = ReflectionHelper.getField(blendedNoise.getClass(), "a", true);
                aField.setAccessible(true);
                NoiseGeneratorOctaves minLimitNoise = (NoiseGeneratorOctaves) aField.get(blendedNoise);

                Field bField = ReflectionHelper.getField(blendedNoise.getClass(), "b", true);
                bField.setAccessible(true);
                NoiseGeneratorOctaves maxLimitNoise = (NoiseGeneratorOctaves) bField.get(blendedNoise);

                Field cField = ReflectionHelper.getField(blendedNoise.getClass(), "c", true);
                cField.setAccessible(true);
                NoiseGeneratorOctaves mainNoise = (NoiseGeneratorOctaves) cField.get(blendedNoise);

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
