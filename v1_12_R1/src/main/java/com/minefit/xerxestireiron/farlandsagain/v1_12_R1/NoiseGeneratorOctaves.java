package com.minefit.xerxestireiron.farlandsagain.v1_12_R1;

import java.util.Random;

import net.minecraft.server.v1_12_R1.MathHelper;
import net.minecraft.server.v1_12_R1.NoiseGenerator;
import net.minecraft.server.v1_12_R1.NoiseGeneratorPerlin;

public class NoiseGeneratorOctaves extends NoiseGenerator {
    private NoiseGeneratorPerlin[] a;
    private int b;
    private final int lowX;
    private final int lowZ;
    private final int highX;
    private final int highZ;

    public NoiseGeneratorOctaves(ConfigValues configValues, Random random, int i) {
        this.b = i;
        this.a = new NoiseGeneratorPerlin[i];
        this.lowX = configValues.farLandsLowX / 4;
        this.lowZ = configValues.farLandsLowZ / 4;
        this.highX = configValues.farLandsHighX / 4;
        this.highZ = configValues.farLandsHighZ / 4;

        for (int j = 0; j < i; ++j) {
            this.a[j] = new NoiseGeneratorPerlin(random);
        }
    }

    public double[] a(double[] adouble, int i, int j, int k, int l, int i1, int j1, double d0, double d1, double d2) {
        if (adouble == null) {
            adouble = new double[l * i1 * j1];
        } else {
            for (int k1 = 0; k1 < adouble.length; ++k1) {
                adouble[k1] = 0.0D;
            }
        }

        if (i >= this.highX) {
            i += 3137706;
        } else if (i <= this.lowX) {
            i -= 3137706;
        }

        if (k >= this.highZ) {
            k += 3137706;
        } else if (k <= this.lowZ) {
            k -= 3137706;
        }

        double d3 = 1.0D;

        for (int l1 = 0; l1 < this.b; ++l1) {
            double d4 = i * d3 * d0;
            double d5 = j * d3 * d1;
            double d6 = k * d3 * d2;
            long i2 = MathHelper.d(d4);
            long j2 = MathHelper.d(d6);

            d4 -= i2;
            d6 -= j2;

            // i2 %= 16777216L;
            // j2 %= 16777216L;

            d4 += i2;
            d6 += j2;
            this.a[l1].a(adouble, d4, d5, d6, l, i1, j1, d0 * d3, d1 * d3, d2 * d3, d3);
            d3 /= 2.0D;
        }

        return adouble;
    }

    public double[] a(double[] adouble, int i, int j, int k, int l, double d0, double d1, double d2) {
        return this.a(adouble, i, 10, j, k, 1, l, d0, 1.0D, d1);
    }
}
