package com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R1;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import com.minefit.XerxesTireIron.FarLandsAgain.FarLandsAgain;
import com.minefit.XerxesTireIron.FarLandsAgain.PaperSpigot;

import net.minecraft.server.v1_8_R1.BiomeBase;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.Chunk;
import net.minecraft.server.v1_8_R1.ChunkProviderHell;
import net.minecraft.server.v1_8_R1.ChunkSnapshot;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.IChunkProvider;
import net.minecraft.server.v1_8_R1.Material;
import net.minecraft.server.v1_8_R1.MathHelper;
import net.minecraft.server.v1_8_R1.World;
import net.minecraft.server.v1_8_R1.WorldGenBase;
import net.minecraft.server.v1_8_R1.WorldGenCavesHell;
import net.minecraft.server.v1_8_R1.WorldGenNether;

public class FLAChunkProviderHell extends ChunkProviderHell implements IChunkProvider {

    private final World h;
    private final boolean i;
    private final Random j;
    private double[] k = new double[256];
    private double[] l = new double[256];
    private double[] m = new double[256];
    private double[] n;
    private final NoiseGeneratorOctaves o;
    private final NoiseGeneratorOctaves p;
    private final NoiseGeneratorOctaves q;
    private final NoiseGeneratorOctaves r;
    private final NoiseGeneratorOctaves s;
    public final NoiseGeneratorOctaves a;
    public final NoiseGeneratorOctaves b;
    private final WorldGenNether B;
    private final WorldGenBase C;
    double[] c;
    double[] d;
    double[] e;
    double[] f;
    double[] g;

    private final FarLandsAgain plugin;
    private final PaperSpigot paperSpigot;

    public FLAChunkProviderHell(ConfigurationSection config, World world, boolean flag, long i, FarLandsAgain instance) {
        super(world, flag, i);
        this.plugin = instance;
        this.B = new WorldGenNether();
        this.C = new WorldGenCavesHell();
        this.h = world;
        this.i = flag;
        this.j = new Random(i);
        this.o = new NoiseGeneratorOctaves(config, this.j, 16);
        this.p = new NoiseGeneratorOctaves(config, this.j, 16);
        this.q = new NoiseGeneratorOctaves(config, this.j, 8);
        this.r = new NoiseGeneratorOctaves(config, this.j, 4);
        this.s = new NoiseGeneratorOctaves(config, this.j, 4);
        this.a = new NoiseGeneratorOctaves(config, this.j, 10);
        this.b = new NoiseGeneratorOctaves(config, this.j, 16);
        world.b(63);

        this.paperSpigot = new PaperSpigot(this.plugin, world.worldData.getName());
    }

    @Override
    public void a(int i, int j, ChunkSnapshot chunksnapshot) {
        byte b0 = 4;
        byte b1 = 32;
        int k = b0 + 1;
        byte b2 = 17;
        int l = b0 + 1;

        this.n = this.a(this.n, i * b0, 0, j * b0, k, b2, l);

        for (int i1 = 0; i1 < b0; ++i1) {
            for (int j1 = 0; j1 < b0; ++j1) {
                for (int k1 = 0; k1 < 16; ++k1) {
                    double d0 = 0.125D;
                    double d1 = this.n[((i1 + 0) * l + j1 + 0) * b2 + k1 + 0];
                    double d2 = this.n[((i1 + 0) * l + j1 + 1) * b2 + k1 + 0];
                    double d3 = this.n[((i1 + 1) * l + j1 + 0) * b2 + k1 + 0];
                    double d4 = this.n[((i1 + 1) * l + j1 + 1) * b2 + k1 + 0];
                    double d5 = (this.n[((i1 + 0) * l + j1 + 0) * b2 + k1 + 1] - d1) * d0;
                    double d6 = (this.n[((i1 + 0) * l + j1 + 1) * b2 + k1 + 1] - d2) * d0;
                    double d7 = (this.n[((i1 + 1) * l + j1 + 0) * b2 + k1 + 1] - d3) * d0;
                    double d8 = (this.n[((i1 + 1) * l + j1 + 1) * b2 + k1 + 1] - d4) * d0;

                    for (int l1 = 0; l1 < 8; ++l1) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i2 = 0; i2 < 4; ++i2) {
                            double d14 = 0.25D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int j2 = 0; j2 < 4; ++j2) {
                                IBlockData iblockdata = null;

                                if (k1 * 8 + l1 < b1) {
                                    iblockdata = Blocks.LAVA.getBlockData();
                                }

                                if (d15 > 0.0D) {
                                    iblockdata = Blocks.NETHERRACK.getBlockData();
                                }

                                int k2 = i2 + i1 * 4;
                                int l2 = l1 + k1 * 8;
                                int i3 = j2 + j1 * 4;

                                chunksnapshot.a(k2, l2, i3, iblockdata);
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }

    }

    @Override
    public void b(int i, int j, ChunkSnapshot chunksnapshot) {
        byte b0 = 64;
        double d0 = 0.03125D;

        this.k = this.r.a(this.k, i * 16, j * 16, 0, 16, 16, 1, d0, d0, 1.0D);
        this.l = this.r.a(this.l, i * 16, 109, j * 16, 16, 1, 16, d0, 1.0D, d0);
        this.m = this.s.a(this.m, i * 16, j * 16, 0, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                boolean flag = this.k[k + l * 16] + this.j.nextDouble() * 0.2D > 0.0D;
                boolean flag1 = this.l[k + l * 16] + this.j.nextDouble() * 0.2D > 0.0D;
                int i1 = (int) (this.m[k + l * 16] / 3.0D + 3.0D + this.j.nextDouble() * 0.25D);
                int j1 = -1;
                IBlockData iblockdata = Blocks.NETHERRACK.getBlockData();
                IBlockData iblockdata1 = Blocks.NETHERRACK.getBlockData();

                for (int k1 = 127; k1 >= 0; --k1) {
                    if (k1 < 127 - (this.paperSpigot.generateFlatBedrock ? 0 : this.j.nextInt(5))
                            && k1 > (this.paperSpigot.generateFlatBedrock ? 0 : this.j.nextInt(5))) {
                        IBlockData iblockdata2 = chunksnapshot.a(l, k1, k);

                        if (iblockdata2.getBlock() != null && iblockdata2.getBlock().getMaterial() != Material.AIR) {
                            if (iblockdata2.getBlock() == Blocks.NETHERRACK) {
                                if (j1 == -1) {
                                    if (i1 <= 0) {
                                        iblockdata = null;
                                        iblockdata1 = Blocks.NETHERRACK.getBlockData();
                                    } else if (k1 >= b0 - 4 && k1 <= b0 + 1) {
                                        iblockdata = Blocks.NETHERRACK.getBlockData();
                                        iblockdata1 = Blocks.NETHERRACK.getBlockData();
                                        if (flag1) {
                                            iblockdata = Blocks.GRAVEL.getBlockData();
                                            iblockdata1 = Blocks.NETHERRACK.getBlockData();
                                        }

                                        if (flag) {
                                            iblockdata = Blocks.SOUL_SAND.getBlockData();
                                            iblockdata1 = Blocks.SOUL_SAND.getBlockData();
                                        }
                                    }

                                    if (k1 < b0 && (iblockdata == null || iblockdata.getBlock().getMaterial() == Material.AIR)) {
                                        iblockdata = Blocks.LAVA.getBlockData();
                                    }

                                    j1 = i1;
                                    if (k1 >= b0 - 1) {
                                        chunksnapshot.a(l, k1, k, iblockdata);
                                    } else {
                                        chunksnapshot.a(l, k1, k, iblockdata1);
                                    }
                                } else if (j1 > 0) {
                                    --j1;
                                    chunksnapshot.a(l, k1, k, iblockdata1);
                                }
                            }
                        } else {
                            j1 = -1;
                        }
                    } else {
                        chunksnapshot.a(l, k1, k, Blocks.BEDROCK.getBlockData());
                    }
                }
            }
        }

    }

    @Override
    public Chunk getOrCreateChunk(int i, int j) {
        this.j.setSeed(i * 341873128712L + j * 132897987541L);
        ChunkSnapshot chunksnapshot = new ChunkSnapshot();

        this.a(i, j, chunksnapshot);
        this.b(i, j, chunksnapshot);
        this.C.a(this, this.h, i, j, chunksnapshot);
        if (this.i) {
            this.B.a(this, this.h, i, j, chunksnapshot);
        }

        Chunk chunk = new Chunk(this.h, chunksnapshot, i, j);
        BiomeBase[] abiomebase = this.h.getWorldChunkManager().getBiomeBlock((BiomeBase[]) null, i * 16, j * 16, 16, 16);
        byte[] abyte = chunk.getBiomeIndex();

        for (int k = 0; k < abyte.length; ++k) {
            abyte[k] = (byte) abiomebase[k].id;
        }

        chunk.l();
        return chunk;
    }

    private double[] a(double[] adouble, int i, int j, int k, int l, int i1, int j1) {
        if (adouble == null) {
            adouble = new double[l * i1 * j1];
        }

        double d0 = 684.412D;
        double d1 = 2053.236D;

        this.f = this.a.a(this.f, i, j, k, l, 1, j1, 1.0D, 0.0D, 1.0D);
        this.g = this.b.a(this.g, i, j, k, l, 1, j1, 100.0D, 0.0D, 100.0D);
        this.c = this.q.a(this.c, i, j, k, l, i1, j1, d0 / 80.0D, d1 / 60.0D, d0 / 80.0D);
        this.d = this.o.a(this.d, i, j, k, l, i1, j1, d0, d1, d0);
        this.e = this.p.a(this.e, i, j, k, l, i1, j1, d0, d1, d0);
        int k1 = 0;
        double[] adouble1 = new double[i1];

        int l1;

        for (l1 = 0; l1 < i1; ++l1) {
            adouble1[l1] = Math.cos(l1 * 3.141592653589793D * 6.0D / i1) * 2.0D;
            double d2 = l1;

            if (l1 > i1 / 2) {
                d2 = i1 - 1 - l1;
            }

            if (d2 < 4.0D) {
                d2 = 4.0D - d2;
                adouble1[l1] -= d2 * d2 * d2 * 10.0D;
            }
        }

        for (l1 = 0; l1 < l; ++l1) {
            for (int i2 = 0; i2 < j1; ++i2) {
                double d3 = 0.0D;

                for (int j2 = 0; j2 < i1; ++j2) {
                    double d4 = 0.0D;
                    double d5 = adouble1[j2];
                    double d6 = this.d[k1] / 512.0D;
                    double d7 = this.e[k1] / 512.0D;
                    double d8 = (this.c[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d8 < 0.0D) {
                        d4 = d6;
                    } else if (d8 > 1.0D) {
                        d4 = d7;
                    } else {
                        d4 = d6 + (d7 - d6) * d8;
                    }

                    d4 -= d5;
                    double d9;

                    if (j2 > i1 - 4) {
                        d9 = (j2 - (i1 - 4)) / 3.0F;
                        d4 = d4 * (1.0D - d9) + -10.0D * d9;
                    }

                    if (j2 < d3) {
                        d9 = (d3 - j2) / 4.0D;
                        d9 = MathHelper.a(d9, 0.0D, 1.0D);
                        d4 = d4 * (1.0D - d9) + -10.0D * d9;
                    }

                    adouble[k1] = d4;
                    ++k1;
                }
            }
        }

        return adouble;
    }

    @Override
    public Chunk getChunkAt(BlockPosition blockposition) {
        return this.getOrCreateChunk(blockposition.getX() >> 4, blockposition.getZ() >> 4);
    }

    @Override
    public void recreateStructures(Chunk chunk, int i, int j) {
        if (this.paperSpigot.generateFortress) {
            this.B.a(this, this.h, i, j, (ChunkSnapshot) null);
        }
    }
}
