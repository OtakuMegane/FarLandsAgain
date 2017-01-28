package com.minefit.XerxesTireIron.FarLandsAgain.v1_11_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;

import com.minefit.XerxesTireIron.FarLandsAgain.Messages;
import com.minefit.XerxesTireIron.FarLandsAgain.FarLandsAgain;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_11_R1.FLAChunkProviderGenerate;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_11_R1.FLAChunkProviderHell;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_11_R1.FLAChunkProviderTheEnd;

import net.minecraft.server.v1_11_R1.ChunkGenerator;
import net.minecraft.server.v1_11_R1.WorldServer;

public class LoadFarlands {
    private final FarLandsAgain plugin;
    private final World world;
    private final WorldServer nmsWorld;
    private final Messages messages;
    private ChunkGenerator originalGenerator;

    public LoadFarlands(FarLandsAgain instance, World world) {
        this.plugin = instance;
        this.world = world;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.messages = new Messages(plugin);
        overrideGenerator();
    }

    public void restoreGenerator() {
        try {
            Field cp = net.minecraft.server.v1_11_R1.ChunkProviderServer.class.getDeclaredField("chunkGenerator");
            cp.setAccessible(true);
            setFinal(cp, this.originalGenerator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void overrideGenerator() {
        ConfigurationSection worldConfig = this.plugin.getConfig()
                .getConfigurationSection("worlds." + this.world.getName());
        String worldName = this.world.getName();
        this.originalGenerator = this.nmsWorld.getChunkProviderServer().chunkGenerator;
        String originalGenName = this.originalGenerator.getClass().getSimpleName();
        boolean genFeatures = this.nmsWorld.getWorldData().shouldGenerateMapFeatures();
        String genOptions = this.nmsWorld.getWorldData().getGeneratorOptions();
        Environment environment = this.world.getEnvironment();

        if (originalGenName.equals("FLAChunkProviderGenerate") || originalGenName.equals("FLAChunkProviderHell")
                || originalGenName.equals("FLAChunkProviderTheEnd")) {
            this.messages.alreadyEnabled(worldName);
            return;
        }

        try {
            Field cp = net.minecraft.server.v1_11_R1.ChunkProviderServer.class.getDeclaredField("chunkGenerator");
            cp.setAccessible(true);

            if (environment == Environment.NORMAL) {
                if (!originalGenName.equals("NormalChunkGenerator") && !originalGenName.equals("TimedChunkGenerator")) {
                    this.messages.unknownGenerator(worldName, originalGenName);
                    return;
                }

                FLAChunkProviderGenerate generator = new FLAChunkProviderGenerate(worldConfig, this.nmsWorld,
                        this.nmsWorld.getSeed(), genFeatures, genOptions, this.plugin);
                setFinal(cp, generator);
            } else if (environment == Environment.NETHER) {
                if (!originalGenName.equals("NetherChunkGenerator") && !originalGenName.equals("TimedChunkGenerator")) {
                    this.messages.unknownGenerator(worldName, originalGenName);
                    return;
                }

                FLAChunkProviderHell generator = new FLAChunkProviderHell(worldConfig, this.nmsWorld, genFeatures,
                        this.nmsWorld.getSeed(), this.plugin);
                setFinal(cp, generator);
            } else if (environment == Environment.THE_END) {
                if (!originalGenName.equals("SkyLandsChunkGenerator") && !originalGenName.equals("TimedChunkGenerator")) {
                    this.messages.unknownGenerator(worldName, originalGenName);
                    return;
                }

                FLAChunkProviderTheEnd generator = new FLAChunkProviderTheEnd(worldConfig, this.nmsWorld, genFeatures,
                        this.nmsWorld.getSeed(), this.nmsWorld.worldProvider.h());
                setFinal(cp, generator);
            } else {
                this.messages.unknownEnvironment(worldName, environment.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.messages.enabledSuccessfully(worldName);
    }

    public void setFinal(Field field, Object obj) throws Exception {
        field.setAccessible(true);

        Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(this.nmsWorld.getChunkProviderServer(), obj);
    }
}
