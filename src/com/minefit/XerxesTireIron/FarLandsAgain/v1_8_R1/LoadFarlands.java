package com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R1;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;

import com.minefit.XerxesTireIron.FarLandsAgain.Errors;
import com.minefit.XerxesTireIron.FarLandsAgain.FarLandsAgain;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R1.FLAChunkProviderGenerate;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R1.FLAChunkProviderHell;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R1.FLAChunkProviderTheEnd;

import net.minecraft.server.v1_8_R1.WorldServer;

public class LoadFarlands {
    private FarLandsAgain plugin;
    private Logger logger = Logger.getLogger("Minecraft");
    private World world;
    private WorldServer nmsWorld;
    private Errors errors;

    public LoadFarlands(FarLandsAgain instance, World world) {
        plugin = instance;
        this.world = world;
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.errors = new Errors(plugin);
        overrideGenerator();
    }

    public void overrideGenerator() {
        String worldName = this.world.getName();
        long worldSeed = world.getSeed();
        String existingGenerator = this.nmsWorld.chunkProviderServer.chunkProvider.getClass().getSimpleName();
        boolean genFeatures = this.nmsWorld.getWorldData().shouldGenerateMapFeatures();
        String genOptions = this.nmsWorld.getWorldData().getGeneratorOptions();
        Environment environment = this.world.getEnvironment();

        if (environment == Environment.NORMAL) {
            if (!existingGenerator.equals("NormalChunkGenerator")) {
                this.errors.unknownGenerator(worldName);
                return;
            }

            nmsWorld.chunkProviderServer.chunkProvider = new FLAChunkProviderGenerate(nmsWorld, worldSeed, genFeatures, genOptions);
        } else if (environment == Environment.NETHER) {
            if (!existingGenerator.equals("NetherChunkGenerator")) {
                this.errors.unknownGenerator(worldName);
                return;
            }

            nmsWorld.chunkProviderServer.chunkProvider = new FLAChunkProviderHell(nmsWorld, genFeatures, worldSeed);
        } else if (environment == Environment.THE_END) {
            if (!existingGenerator.equals("SkyLandsChunkGenerator")) {
                this.errors.unknownGenerator(worldName);
                return;
            }

            nmsWorld.chunkProviderServer.chunkProvider = new FLAChunkProviderTheEnd(nmsWorld, worldSeed);
        } else {
            this.errors.unknownEnvironment(worldName, environment.toString());
        }

        logger.info("[" + plugin.getName() + "] Far Lands enabled for world '" + world.getName() + "'!");
    }
}
