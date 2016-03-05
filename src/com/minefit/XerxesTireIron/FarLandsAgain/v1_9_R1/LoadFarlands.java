package com.minefit.XerxesTireIron.FarLandsAgain.v1_9_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

import com.minefit.XerxesTireIron.FarLandsAgain.Errors;
import com.minefit.XerxesTireIron.FarLandsAgain.FarLandsAgain;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_9_R1.FLAChunkProviderGenerate;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_9_R1.FLAChunkProviderHell;
import com.minefit.XerxesTireIron.FarLandsAgain.v1_9_R1.FLAChunkProviderTheEnd;

import net.minecraft.server.v1_9_R1.WorldServer;

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
        String existingGenerator = this.nmsWorld.getChunkProviderServer().chunkGenerator.getClass().getSimpleName();
        boolean genFeatures = this.nmsWorld.getWorldData().shouldGenerateMapFeatures();
        String genOptions = this.nmsWorld.getWorldData().getGeneratorOptions();
        Environment environment = this.world.getEnvironment();

        try {
            Field cp = net.minecraft.server.v1_9_R1.ChunkProviderServer.class.getDeclaredField("chunkGenerator");
            cp.setAccessible(true);

            if (environment == Environment.NORMAL) {
                if (!existingGenerator.equals("NormalChunkGenerator")) {
                    this.errors.unknownGenerator(worldName);
                    return;
                }

                FLAChunkProviderGenerate generator = new FLAChunkProviderGenerate(nmsWorld, nmsWorld.getSeed(), genFeatures,
                        genOptions);
                setFinal(cp, generator);
            } else if (environment == Environment.NETHER) {
                if (!existingGenerator.equals("NetherChunkGenerator")) {
                    this.errors.unknownGenerator(worldName);
                    return;
                }

                FLAChunkProviderHell generator = new FLAChunkProviderHell(nmsWorld, genFeatures, nmsWorld.getSeed());
                setFinal(cp, generator);
            } else if (environment == Environment.THE_END) {
                if (!existingGenerator.equals("SkyLandsChunkGenerator")) {
                    this.errors.unknownGenerator(worldName);
                    return;
                }

                FLAChunkProviderTheEnd generator = new FLAChunkProviderTheEnd(nmsWorld, genFeatures, nmsWorld.getSeed());
                setFinal(cp, generator);
            } else {
                this.errors.unknownEnvironment(worldName, environment.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("[" + plugin.getName() + "] Far Lands enabled for world '" + world.getName() + "'!");
    }

    public void setFinal(Field field, Object obj) throws Exception {
        field.setAccessible(true);

        Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(nmsWorld.getChunkProviderServer(), obj);
    }
}
