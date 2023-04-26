package dindcrzy.starcana;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.*;
import oshi.util.tuples.Pair;

import java.lang.Math;
import java.util.ArrayList;

public class Constellation {
    public final Vec3d posInSky;
    public final Vec3d x;
    public final Vec3d y;
    public final ArrayList<Vec2f> stars = new ArrayList<>();
    public final ArrayList<Pair<Integer, Integer>> connections = new ArrayList<>();
    Constellation(Long seed) {
        Random random = Random.create(seed);
        Vec3d posInSky = Helper.randomUnitVector(random);
        this.x = Helper.randomOrthogonalVector(posInSky, random);
        this.y = this.x.crossProduct(posInSky);
        this.posInSky = posInSky.multiply(100);
    }

    // should range from (-1,-1) to (1,1) ideally
    public Constellation addStar(float x, float y) {
        stars.add(new Vec2f(x, y));
        return this;
    }

    public Constellation addConnection(int i1, int i2) {
        connections.add(new Pair<>(i1, i2));
        return this;
    }

    public Constellation addConnectionLine(int[] is) {
        for (int i = 0; i < is.length - 1; i++) {
            addConnection(is[i], is[i + 1]);
        }
        return this;
    }

    public Vec3d starPos(int i, float scale) {
        Vec2f rel = stars.get(i).multiply(scale);
        return posInSky.add(x.multiply(rel.x)).add(y.multiply(rel.y));
    }

    public boolean isVisible(int phase) {
        return true;
    }

    public Vector3f getSkyVector(World world) {
        Matrix4f transform = new Matrix4f();
        transform.rotate((float)-Math.PI / 2f, 0, 1, 0);
        transform.rotate((float) (world.getSkyAngle(0) * Math.PI * 2f), 1, 0, 0);
        return transform.transformPosition(posInSky.normalize().toVector3f());
    }

    private Identifier id;
    public Identifier getId() {
        if (id == null) {
            id = Constellations.CONSTELLATION_REGISTRY.getId(this);
        }
        return id;
    }

    private String translationKey;
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.createTranslationKey("constellation",
                    Constellations.CONSTELLATION_REGISTRY.getId(this));
        }
        return translationKey;
    }
}
