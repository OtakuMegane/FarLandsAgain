package com.minefit.xerxestireiron.farlandsagain.v1_15_R1;

import net.minecraft.server.v1_15_R1.ChunkProviderHell;
import net.minecraft.server.v1_15_R1.GeneratorSettingsNether;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.NoiseGeneratorOctaves;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldChunkManager;

public class FLAChunkProviderHell extends ChunkProviderHell {

    private final ConfigValues configValues;
    private final int lowX;
    private final int lowZ;
    private final int highX;
    private final int highZ;
    private final NoiseGeneratorOctaves o;
    private final NoiseGeneratorOctaves p;
    private final NoiseGeneratorOctaves q;

    public FLAChunkProviderHell(World world, WorldChunkManager worldchunkmanager, GeneratorSettingsNether generatorsettingsnether, ConfigValues configValues) {
        super(world, worldchunkmanager, generatorsettingsnether);
        this.configValues = configValues;
        this.lowX = this.configValues.farLandsLowX / 4;
        this.lowZ = this.configValues.farLandsLowZ / 4;
        this.highX = this.configValues.farLandsHighX / 4;
        this.highZ = this.configValues.farLandsHighZ / 4;
        this.o = new NoiseGeneratorOctaves(this.e, 15, 0);
        this.p = new NoiseGeneratorOctaves(this.e, 15, 0);
        this.q = new NoiseGeneratorOctaves(this.e, 7, 0);
    }

    // Used for Far Lands
    private double a(int i, int j, int k, double d0, double d1, double d2, double d3) {
        double d4 = 0.0D;
        double d5 = 0.0D;
        double d6 = 0.0D;
        double d7 = 1.0D;

        int iMultiX = 1;
        int kMultiZ = 1;

        if (i >= this.highX || i <= this.lowX) {
            iMultiX = 3137706;
        }

        if (k >= this.highZ || k <= this.lowZ) {
            kMultiZ = 3137706;
        }

        for (int l = 0; l < 16; ++l) {
            double d8 = NoiseGeneratorOctaves.a((double) i * d0 * d7); // same as original d4 (x-based)
            double d9 = NoiseGeneratorOctaves.a((double) j * d1 * d7);
            double d10 = NoiseGeneratorOctaves.a((double) k * d0 * d7); // same as original d6 (z-based)
            double d11 = d1 * d7;

            d4 += this.o.a(l).a(d8 * iMultiX, d9, d10 * kMultiZ, d11, (double) j * d11) / d7;
            d5 += this.p.a(l).a(d8 * iMultiX, d9, d10 * kMultiZ, d11, (double) j * d11) / d7;
            if (l < 8) {
                d6 += this.q.a(l).a(NoiseGeneratorOctaves.a((double) i * d2 * d7), NoiseGeneratorOctaves.a((double) j * d3 * d7), NoiseGeneratorOctaves.a((double) k * d2 * d7), d3 * d7, (double) j * d3 * d7) / d7;
            }

            d7 /= 2.0D;
        }

        return MathHelper.b(d4 / 512.0D, d5 / 512.0D, (d6 / 10.0D + 1.0D) / 2.0D);
    }

    // Override for Far Lands
    @Override
    protected void a(double[] adouble, int i, int j, double d0, double d1, double d2, double d3, int k, int l) {
        double[] adouble1 = this.a(i, j);
        double d4 = adouble1[0];
        double d5 = adouble1[1];
        double d6 = this.g();
        double d7 = this.h();

        for (int i1 = 0; i1 < this.i(); ++i1) {
            double d8 = this.a(i, i1, j, d0, d1, d2, d3);

            d8 -= this.a(d4, d5, i1);
            if ((double) i1 > d6) {
                d8 = MathHelper.b(d8, (double) l, ((double) i1 - d6) / (double) k);
            } else if ((double) i1 < d7) {
                d8 = MathHelper.b(d8, -30.0D, (d7 - (double) i1) / (d7 - 1.0D));
            }

            adouble[i1] = d8;
        }

    }
}
