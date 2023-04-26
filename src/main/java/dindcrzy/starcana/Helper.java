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
        // 27000 = 24000 * 9 / 8
        double d = getLinearMoonAngle(world.getLunarTime());
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        float moonAngle = (float)(d * 2.0 + e) / 3.0f;
        return moonAngle * 360.0f;
    }

    public static float getLinearSunAngle(Long lunarTime) {
        return (float)MathHelper.fractionalPart((double)lunarTime / 24000.0 - 0.25);
    }

    public static float getLinearMoonAngle(Long lunarTime) {
        return (float)MathHelper.fractionalPart((double)(lunarTime - 6000) / 27000.0 + 0.5);
    }
    public static float getMoonTilt(Long lunarTime) {
        return (float)Math.cos((double) lunarTime * Math.PI * 2.0 / (24000.0 * 9.0)) * 0.05f;
    }

    // the moon phase can change in the middle of the night thanks to 8/9ths speed
    // interpolates visibility so it isn't a jarring jump
    public static float lerpVisibility(Constellation constellation, World world, int delta) {
        int prePhase = world.getDimension().getMoonPhase(world.getLunarTime() - delta);
        int postPhase = world.getDimension().getMoonPhase(world.getLunarTime() + delta);
        boolean inPrePhase = constellation.isVisible(prePhase);
        boolean inPostPhase = constellation.isVisible(postPhase);
        if (inPrePhase && inPostPhase) {
            return 1f;
        }
        if (!inPrePhase && !inPostPhase) {
            return 0f;
        }
        // 18000
        float lerp = world.getLunarTime() - 6000;
        return 0f;
    }
}
