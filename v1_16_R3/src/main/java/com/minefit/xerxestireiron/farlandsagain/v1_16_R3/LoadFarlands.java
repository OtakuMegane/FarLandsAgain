package com.minefit.xerxestireiron.farlandsagain.v1_16_R3;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import com.minefit.xerxestireiron.farlandsagain.Messages;
import com.minefit.xerxestireiron.farlandsagain.ReflectionHelper;

import net.minecraft.server.v1_16_R3.ChunkProviderServer;
import net.minecraft.server.v1_16_R3.GeneratorSettingBase;
import net.minecraft.server.v1_16_R3.ChunkGenerator;
import net.minecraft.server.v1_16_R3.WorldChunkManager;
import net.minecraft.server.v1_16_R3.WorldServer;

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
                            h, this.configValues, this.originalGenerator, divisor));
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
            ReflectionHelper.fieldSetter(chunkGenerator, this.chunkServer, generator);

            Field chunkMapGenerator = ReflectionHelper.getField(this.chunkServer.playerChunkMap.getClass(),
                    "chunkGenerator", true);
            chunkMapGenerator.setAccessible(true);
            ReflectionHelper.fieldSetter(chunkMapGenerator, this.chunkServer.playerChunkMap, generator);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        return true;
    }
}
