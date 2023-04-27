package dindcrzy.starcana;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.random.Random;

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
            .addConnections(new int[]{0, 1, 2, 3, 4, 5, 1});
    public static Constellation BOX = new Constellation(2377L)
            .addStar(-1f, -1f)
            .addStar(1f, -1f)
            .addStar(1f, 1f)
            .addStar(-1f, 1f)
            .addConnections(new int[]{0, 1, 2, 3, 0});
    public static Constellation RNG = random(348998L, Constellation.MOON_POSITION.TRANSITION);
    public static Constellation PHASES = new Constellation(9012L)
            .addStar(0, 0)
            .addStar(0.25f, 0.25f)
            .addStar(0.5f, 0)
            .addStar(-0.3f, -0.4f)
            .addStar(-0.8f, -0.5f)
            .addConnections(new int[]{0, 1, 2})
            .addConnections(new int[]{1, 3, 4})
            .setPhaseVisibility(new boolean[]{true, true, false, false, false, false, true, true});

    public static Constellation PHASE_VIS = new Constellation(54390L)
            .addStar(0.25f, 0.4f)
            .addStar(0.4f, -0.25f)
            .addStar(-0.25f, -0.4f)
            .addStar(-0.4f, 0.25f)
            .addConnections(new int[]{0, 1, 2, 3, 1})
            .setPhaseVisibility(new boolean[]{true, false, true, false, true, false, true, false})
            .setPosVisibility(Constellation.MOON_POSITION.TRANSITION);

    public static Constellation TEST = new Constellation(541978L)
            .addStar(-0.74f, -0.56f)
            .addStar(-0.12f, -0.52f)
            .addStar(0.28f, -0.72f)
            .addStar(0.40f, -0.34f)
            .addStar(-0.64f, 0.10f)
            .addStar(-0.12f, 0.43f)
            .addStar(-0.63f, 0.85f)
            .addStar(0.61f, 0.78f)
            .addConnection(0, 1)
            .addConnection(1, 4)
            .addConnection(4, 5)
            .addConnection(5, 6)
            .addConnection(7, 5)
            .addConnection(3, 1)
            .addConnection(1, 2)
            .setPhaseVisibility(new boolean[]{false,false,true,true,true,true,false,false})
            .setPosVisibility(Constellation.MOON_POSITION.VISIBLE);

    public static void register() {
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("plunger"), PLUNGER);
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("box"), BOX);
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("rng"), RNG);
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("phases"), PHASES);
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("phase_vis"), PHASE_VIS);
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("test"), TEST);
    }

    public static Constellation random(Long seed, Constellation.MOON_POSITION vis) {
        Random random = Random.create(seed);
        Constellation con = new Constellation(seed).setPosVisibility(vis);
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
