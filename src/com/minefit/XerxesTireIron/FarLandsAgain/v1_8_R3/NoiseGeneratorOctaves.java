package com.minefit.XerxesTireIron.FarLandsAgain.v1_8_R3;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NoiseGenerator;
import net.minecraft.server.v1_8_R3.NoiseGeneratorPerlin;

public class NoiseGeneratorOctaves extends NoiseGenerator {
    private NoiseGeneratorPerlin[] a;
    private int b;
    private final int lowX;
    private final int lowZ;
    private final int highX;
    private final int highZ;

    public NoiseGeneratorOctaves(ConfigurationSection config, Random random, int i) {
        this.b = i;
        this.a = new NoiseGeneratorPerlin[i];
        this.lowX = config.getInt("lowX", -12550824) / 4;
        this.lowZ = config.getInt("lowZ", -12550824) / 4;
        this.highX = config.getInt("highX", 12550824) / 4;
        this.highZ = config.getInt("highZ", 12550824) / 4;

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
