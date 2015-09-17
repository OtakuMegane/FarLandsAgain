package com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R2;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;

import com.minefit.XerxesTireIron.FarLandsAgain.FarLandsAgain;

public class LoadFarlands
{
    private FarLandsAgain plugin;
    private Logger logger = Logger.getLogger("Minecraft");

    public LoadFarlands(FarLandsAgain instance, World world)
    {
        plugin = instance;

        net.minecraft.server.v1_8_R2.WorldServer nmsWorld = ((org.bukkit.craftbukkit.v1_8_R2.CraftWorld) world).getHandle();
        boolean genFeatures = nmsWorld.getWorldData().shouldGenerateMapFeatures();
        String genOptions = nmsWorld.getWorldData().getGeneratorOptions();

        if(world.getEnvironment() == Environment.NORMAL)
        {
            nmsWorld.chunkProviderServer.chunkProvider = new com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R2.FLAChunkProviderGenerate(nmsWorld, nmsWorld.getSeed(), genFeatures, genOptions);
        }
        else if(world.getEnvironment() == Environment.NETHER)
        {
            nmsWorld.chunkProviderServer.chunkProvider = new com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R2.FLAChunkProviderHell(nmsWorld, genFeatures, nmsWorld.getSeed());
        }
        else if(world.getEnvironment() == Environment.THE_END)
        {
            nmsWorld.chunkProviderServer.chunkProvider = new com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R2.FLAChunkProviderTheEnd(nmsWorld, nmsWorld.getSeed());
        }
        else
        {
            logger.info("[" + plugin.getName() + "] The world " + world.getName() + " does not have a recognized environment type. Far Lands not enabled.");
            return;
        }

        logger.info("[" + plugin.getName() + "] Far Lands enabled for world '" + world.getName() + "'!");
    }
}
