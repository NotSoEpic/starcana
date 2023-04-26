package dindcrzy.starcana;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Constellations {
    public static final RegistryKey<Registry<Constellation>> CONSTELLATION =
            RegistryKey.ofRegistry(Starcana.id("constellation"));
    public static final SimpleRegistry<Constellation> CONSTELLATION_REGISTRY =
            FabricRegistryBuilder.createSimple(CONSTELLATION)
                    .attribute(RegistryAttribute.SYNCED).buildAndRegister();

    public static Constellation PLUNGER = new Constellation(524789L)
            .addStar(0f, 1f)
            .addStar(0f, -0.2f)
            .addStar(-0.6f, -0.4f)
            .addStar(-0.8f, -1f)
            .addStar(0.8f, -1f)
            .addStar(0.6f, -0.4f)
            .addConnectionLine(new int[]{0, 1, 2, 3, 4, 5, 1});
    public static Constellation BOX = new Constellation(2377L)
            .addStar(-1f, -1f)
            .addStar(1f, -1f)
            .addStar(1f, 1f)
            .addStar(-1f, 1f)
            .addConnectionLine(new int[]{0, 1, 2, 3, 0});
    public static Constellation RNG = random(348998L);

    public static void register() {
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("plunger"), PLUNGER);
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("box"), BOX);
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("rng"), RNG);
    }

    public static Constellation random(Long seed) {
        Random random = Random.create(seed);
        Constellation con = new Constellation(seed);
        int innerStars = 0;
        for (int i = 0; i < 4; i++) {
            Vec2f candidate = new Vec2f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f);
            if (con.stars.stream().noneMatch(
                    vec -> vec.distanceSquared(candidate) < 0.1)) {
                con.addStar(candidate.x, candidate.y);
                innerStars++;
            }
        }
        for (int i = 0; i < 6; i++) {
            Vec2f candidate = new Vec2f(random.nextFloat() * 2f - 1f, random.nextFloat() * 2f - 1f);
            if (con.stars.stream().noneMatch(
                    vec -> vec.distanceSquared(candidate) < 0.1)) {
                con.addStar(candidate.x, candidate.y);
            }
        }
        for (int i = 0; i < innerStars - 1; i++) {
            con.addConnection(i, i+1);
        }

        return con;
    }
}
