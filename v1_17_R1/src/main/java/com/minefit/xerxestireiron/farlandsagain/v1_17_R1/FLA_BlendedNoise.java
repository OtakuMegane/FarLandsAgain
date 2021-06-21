package com.minefit.xerxestireiron.farlandsagain.v1_17_R1;

import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class FLA_BlendedNoise extends BlendedNoise {
	private final PerlinNoise minLimitNoise;
	private final PerlinNoise maxLimitNoise;
	private final PerlinNoise mainNoise;
    private final ConfigValues configValues;
    private final int lowX;
    private final int lowZ;
    private final int highX;
    private final int highZ;

	public FLA_BlendedNoise(PerlinNoise var0, PerlinNoise var1, PerlinNoise var2, ConfigValues configValues, int divisor) {
	    super(var0, var1, var2);
		this.minLimitNoise = var0;
		this.maxLimitNoise = var1;
		this.mainNoise = var2;
        this.configValues = configValues;

        if(this.configValues != null) {
            this.highX = configValues.farLandsHighX / divisor;
            this.highZ = configValues.farLandsHighZ / divisor;
            this.lowX = configValues.farLandsLowX / divisor;
            this.lowZ = configValues.farLandsLowZ / divisor;
        } else {
            this.highX = Integer.MAX_VALUE;
            this.highZ = Integer.MAX_VALUE;
            this.lowX = Integer.MIN_VALUE;
            this.lowZ = Integer.MIN_VALUE;
        }
	}

	public FLA_BlendedNoise(RandomSource var0) {
		this(new PerlinNoise(var0, IntStream.rangeClosed(-15, 0)), new PerlinNoise(var0, IntStream.rangeClosed(-15, 0)),
				new PerlinNoise(var0, IntStream.rangeClosed(-7, 0)), null, 4);
	}

	public double sampleAndClampNoise(int var0, int var1, int var2, double var3, double var5, double var7,
			double var9) {
		double var11 = 0.0D;
		double var13 = 0.0D;
		double var15 = 0.0D;
		boolean var17 = true;
		double var18 = 1.0D;

        // FarLandsAgain: We inject a multiplier to force the integer overflow
        int MultiX = 1;
        int MultiZ = 1;

        if (var0 >= this.highX || var0 <= this.lowX) {
            MultiX = 3137706;
        }

        if (var2 >= this.highZ || var2 <= this.lowZ) {
            MultiZ = 3137706;
        }

		for (int var20 = 0; var20 < 8; ++var20) {
			ImprovedNoise var21 = this.mainNoise.getOctaveNoise(var20);
			if (var21 != null) {
				var15 += var21.noise(PerlinNoise.wrap((double) var0 * var7 * var18),
						PerlinNoise.wrap((double) var1 * var9 * var18), PerlinNoise.wrap((double) var2 * var7 * var18),
						var9 * var18, (double) var1 * var9 * var18) / var18;
			}

			var18 /= 2.0D;
		}

		double var20 = (var15 / 10.0D + 1.0D) / 2.0D;
		boolean var22 = var20 >= 1.0D;
		boolean var23 = var20 <= 0.0D;
		var18 = 1.0D;

		for (int var24 = 0; var24 < 16; ++var24) {
			double var25 = PerlinNoise.wrap((double) var0 * var3 * var18);
			double var27 = PerlinNoise.wrap((double) var1 * var5 * var18);
			double var29 = PerlinNoise.wrap((double) var2 * var3 * var18);
			double var31 = var5 * var18;
			ImprovedNoise var33;

            // FarLandsAgain: Inject extra multiplier here. This is so we can control where the Far Lands start.
            // Otherwise we would just block the clamping in PerlinNoise.wrap(double)
			if (!var22) {
				var33 = this.minLimitNoise.getOctaveNoise(var24);
				if (var33 != null) {
					var11 += var33.noise(var25 * MultiX, var27, var29 * MultiZ, var31, (double) var1 * var31) / var18;
				}
			}

			if (!var23) {
				var33 = this.maxLimitNoise.getOctaveNoise(var24);
				if (var33 != null) {
					var13 += var33.noise(var25 * MultiX, var27, var29 * MultiZ, var31, (double) var1 * var31) / var18;
				}
			}

			var18 /= 2.0D;
		}

		return Mth.clampedLerp(var11 / 512.0D, var13 / 512.0D, var20);
	}
}