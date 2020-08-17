package com.minefit.xerxestireiron.farlandsagain.v1_16_R2;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;

import net.minecraft.server.v1_16_R2.ChunkProviderServer;
import net.minecraft.server.v1_16_R2.GeneratorSettingBase;
import net.minecraft.server.v1_16_R2.IRegistry;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.RegistryGeneration;
import net.minecraft.server.v1_16_R2.ResourceKey;
import net.minecraft.server.v1_16_R2.StructureFeatures;
import net.minecraft.server.v1_16_R2.BiomeBase;
import net.minecraft.server.v1_16_R2.BiomeDecoratorGroups;
import net.minecraft.server.v1_16_R2.BiomeRegistry;
import net.minecraft.server.v1_16_R2.BiomeSettingsGeneration;
import net.minecraft.server.v1_16_R2.Biomes;
import net.minecraft.server.v1_16_R2.ChunkGenerator;
import net.minecraft.server.v1_16_R2.WorldChunkManager;
import net.minecraft.server.v1_16_R2.WorldGenCarvers;
import net.minecraft.server.v1_16_R2.WorldGenFeatureCompositeConfiguration;
import net.minecraft.server.v1_16_R2.WorldGenFeatureConfigured;
import net.minecraft.server.v1_16_R2.WorldGenStage;
import net.minecraft.server.v1_16_R2.WorldGenSurfaceComposites;
import net.minecraft.server.v1_16_R2.WorldServer;

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
        this.isPaper = Bukkit.getName().contains("Paper");
        overrideGenerator();
    }

    public void restoreGenerator() {
        if (!setGenerator(this.originalGenerator)) {
            this.messages.restoreFailed(this.worldName);
        }
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
            // The End needs a divisor of 8 for whatever reason
            int divisor = (environment == Environment.THE_END) ? 8 : 4;
            WorldChunkManager chunkManager = this.chunkServer.chunkGenerator.getWorldChunkManager();

            try {
                Field hField = ReflectionHelper.getField(this.originalGenerator.getClass(), "h", true);
                hField.setAccessible(true);
                Supplier<GeneratorSettingBase> h;
                h = (Supplier<GeneratorSettingBase>) hField.get(this.originalGenerator);

                if (this.isPaper) {
                    enabled = setGenerator(new FLA_ChunkGeneratorAbstract_Paper(chunkManager, this.nmsWorld.getSeed(),
                            h, this.configValues, this.originalGenerator));
                } else {
                    enabled = setGenerator(new FLA_ChunkGeneratorAbstract(chunkManager, this.nmsWorld.getSeed(), h,
                            this.configValues, this.originalGenerator, divisor));
                }

            } catch (Exception e) {
                e.printStackTrace();
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
            return originalGenName.equals("ChunkGeneratorAbstract") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.NETHER) {
            return originalGenName.equals("ChunkGeneratorAbstract") || originalGenName.equals("TimedChunkGenerator");
        } else if (environment == Environment.THE_END) {
            return originalGenName.equals("ChunkGeneratorAbstract") || originalGenName.equals("TimedChunkGenerator");
        }

        return false;
    }

    private boolean setGenerator(ChunkGenerator generator) {
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
