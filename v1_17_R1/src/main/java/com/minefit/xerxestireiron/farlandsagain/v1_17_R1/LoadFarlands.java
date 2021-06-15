package com.minefit.xerxestireiron.farlandsagain.v1_17_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.ReflectionHelper;

import net.minecraft.core.QuartPos;
import net.minecraft.data.worldgen.WorldGenCarvers;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.levelgen.GeneratorSettingBase;
import net.minecraft.world.level.levelgen.NoiseModifier;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SeededRandom;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NoiseGenerator3Handler;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorOctaves;
import sun.misc.Unsafe;

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

    @SuppressWarnings("unchecked")
    public void overrideGenerator() {
        Environment environment = this.world.getEnvironment();

        if (this.originalGenName.equals("FLA_ChunkGeneratorAbstract")
                || this.originalGenName.equals("FLA_ChunkGeneratorAbstract_Paper")) {
            this.messages.alreadyEnabled(this.worldName);
            return;
        }

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
            WorldChunkManager chunkManager = this.chunkServer.d.getWorldChunkManager(); // Transition: chunkGenerator -> d



            // The End needs a divisor of 8 for whatever reason
            Supplier<GeneratorSettingBase> g;

            try {
                Field gField = ReflectionHelper.getField(this.originalGenerator.getClass(), "g", true);
                gField.setAccessible(true);
                g = (Supplier<GeneratorSettingBase>) gField.get(this.originalGenerator); // settings -> g

                /*if (this.isPaper) {
                    enabled = setGenerator(new FLA_ChunkGeneratorAbstract_Paper(chunkManager, this.nmsWorld.getSeed(),
                            h, this.configValues, this.originalGenerator, divisor));
                } else {
                    enabled = setGenerator(new FLA_ChunkGeneratorAbstract(chunkManager, this.nmsWorld.getSeed(), h,
                            this.configValues, this.originalGenerator, divisor));
                }*/

            } catch (Exception e) {
                e.printStackTrace();
                g = null;
            }

            // Should reproduce random state from here
            SeededRandom seededrandom = new SeededRandom(this.nmsWorld.getSeed());

            NoiseGeneratorOctaves noisegeneratoroctaves = new NoiseGeneratorOctaves(seededrandom, IntStream.rangeClosed(-15, 0));
            //NoiseGenerator3Handler noisegenerator3handler;

            /*if (noisesettings.l()) {
                SeededRandom seededrandom1 = new SeededRandom(this.nmsWorld.getSeed());

                seededrandom1.a(17292);
                noisegenerator3handler = new NoiseGenerator3Handler(seededrandom1);
            } else {
                noisegenerator3handler = null;
            }*/

            GeneratorSettingBase generatorsettingbase = (GeneratorSettingBase) g.get();
            NoiseSettings noisesettings = generatorsettingbase.b();
            NoiseSamplingSettings noiseSamplingSettings = new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D);



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

                FLA_BlendedNoise newBlendedNoise = new FLA_BlendedNoise(seededrandom);
                ReflectionHelper.setFinal(blendedNoiseField, sampler, newBlendedNoise);


                /*Field jField = ReflectionHelper.getField(sampler.getClass(), "j", true);
                jField.setAccessible(true);
                jField.set(sampler, noisegeneratoroctaves);
                Field dField = ReflectionHelper.getField(noisesettings.getClass(), "d", true); // noiseSamplingSettings -> d
                dField.setAccessible(true);
                ReflectionHelper.setFinal(dField, noisesettings, noiseSamplingSettings);*/
                enabled = true;
            } catch (Exception e) {
                e.printStackTrace();
                enabled = false;
            }

            // minY -> F
            // height -> G
            // logicalHeight -> H

// depthNoise -> j
// sampler -> u

           // NoiseSampler sampler = new NoiseSampler(worldChunkManager, cellWidth, cellHeight, cellCountY, noisesettings, blendednoise, noisegenerator3handler, noisegeneratoroctaves, (NoiseModifier) object);



            // End new way
        }

        if (enabled) {
            this.messages.enableSuccess(worldName);
        } else {
            this.messages.enableFailed(worldName);
        }
    }

    private void setFinalInt(Field field, Object instance, int value) throws Exception
    {
        try {
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setInt(instance, value);
        } catch (Exception e) {
            try {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                Unsafe unsafe = (Unsafe) theUnsafe.get(null);
                long offset = unsafe.objectFieldOffset(field);
                unsafe.putInt(instance, offset, value);
            } catch (Exception e1) {
                throw e1;
            }
        }
    }

    private boolean isRecognizedGenerator(Environment environment, String originalGenName) {
        if (environment == Environment.NORMAL) {
            return originalGenName.equals("ChunkGeneratorAbstract") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.NETHER) {
            return originalGenName.equals("ChunkGeneratorAbstract") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.THE_END) {
            return originalGenName.equals("ChunkGeneratorAbstract") || originalGenName.equals("TimedChunkGenerator");
        }

        return false;
    }

    /*private boolean setGenerator(ChunkGenerator generator) {
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
    }*/
}
