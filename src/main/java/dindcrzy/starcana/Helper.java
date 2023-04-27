package dindcrzy.starcana;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class Helper {
    // https://math.stackexchange.com/questions/2347215/how-to-find-a-random-unit-vector-orthogonal-to-a-random-unit-vector-in-3d
    public static Vec3d randomUnitVector(Random random) {
        return new Vec3d(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
    }

    public static Vec3d randomOrthogonalVector(Vec3d n, Random random) {
        Vec3d u = randomUnitVector(random);
        while (Math.abs(u.dotProduct(n)) > 0.99) {
            u = randomUnitVector(random);
        }
        return u.crossProduct(n).normalize();
    }

    public static Vec3d[] genStarVectors(Vec3d x, Vec3d y, Vec3d pos, int gonSides, boolean spiky) {
        Vec3d[] points = new Vec3d[gonSides];
        for (int p = 0; p < gonSides; p++) {
            double theta = (double)p / gonSides * Math.PI * 2.0;
            points[p] = pos.add(
                    x.multiply(Math.sin(theta)).add(
                            y.multiply(Math.cos(theta))
                    ).multiply(spiky && (p & 1) == 0 ? gonSides * 0.05 : 1.0)
            );
        }
        return points;
    }

    private static final PerlinNoiseSampler noise = new PerlinNoiseSampler(Random.create(185434L));
    public static Vector3f starShift(float t, float off) {
        return new Vector3f(
                (float)noise.sample(t, off, 0),
                (float)noise.sample(t, off, 1.23),
                (float)noise.sample(t, off, 2.46)
        );
    }

    public static float getMoonAngle(World world) {
        double d = getLinearMoonAngle(world.getLunarTime());
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        float moonAngle = (float)(d * 2.0 + e) / 3.0f;
        return moonAngle * 360.0f;
    }

    public static float getLinearSunAngle(long lunarTime) {
        return (float)MathHelper.fractionalPart((double)lunarTime / 24000.0 - 0.25);
    }

    // -750 -> 0.25 = moonrise
    // 6000 -> 0.5 = up
    // 12750 -> 0.75 = moonset
    // -7500 / 19500 -> 0 = down
    public static float getLinearMoonAngle(long lunarTime) {
        return (float)MathHelper.fractionalPart((double)(lunarTime - 6000) / 27000.0 + 0.5);
    }
    public static float getMoonTilt(long lunarTime) {
        return (float)Math.cos((double) lunarTime * Math.PI * 2.0 / (24000.0 * 9.0)) * 0.05f;
    }

    public static float getMoonVisibility(long lunarTime) {
        return (float)MathHelper.clamp(
                Math.sin((getLinearMoonAngle(lunarTime) - 0.25) * Math.PI * 2f),
                0, 1
        );
    }
    public static int getMoonPhase(long lunarTime) {
        // 27000 = 24000 * 8 / 9
        // return (int)(time / 18000L % 8L + 8L) % 8;
        return (int)(((lunarTime + 7500) / 27000L + 4) % 8L + 8) % 8;
    }

    // copied straight from ClientWorld.method_23787
    public static float starAlpha(long lunarTime) {
        float g = Helper.getLinearSunAngle(lunarTime);
        float h = 1.0f - (MathHelper.cos((g * ((float)Math.PI * 2))) * 2.0f + 0.25f);
        h = MathHelper.clamp(h, 0.0f, 1.0f);
        return h * h * 0.5f;
    }
}
