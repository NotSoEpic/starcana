package dindcrzy.starcana;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static void register() {
        Registry.register(CONSTELLATION_REGISTRY, Starcana.id("plunger"), PLUNGER);
        for (long i = 0; i < 30; i++) {
            Registry.register(CONSTELLATION_REGISTRY, Starcana.id(String.valueOf(i)), random(i));
        }
    }

    private static int closestPosExclude(Vec2f pos, ArrayList<Vec2f> poss, int[] exclude) {
        float dist = Float.MAX_VALUE;
        int best = 0;
        for (int i = 0; i < poss.size(); i++) {
            int finalI = i;
            float cur_dist = poss.get(i).distanceSquared(pos);
            if (Arrays.stream(exclude).noneMatch(v -> v == finalI) && cur_dist < dist) {
                best = i;
                dist = cur_dist;
            }
        }
        return best;
    }

    public static boolean tooClose(Vec3d posInSky) {
        return Constellations.CONSTELLATION_REGISTRY.stream().anyMatch(constellation -> constellation.posInSky.dotProduct(posInSky) > 95);
    }

    public static Constellation random(Long seed) {
        Constellation con = new Constellation(seed);
        Random random = Random.create(seed);

        // star positions + connections
        ArrayList<Vec2f> stars = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Vec2f pos = new Vec2f(random.nextFloat() * 2f - 1f, random.nextFloat() * 2f - 1f);
            if (stars.stream().noneMatch(star -> star.distanceSquared(pos) < 0.2 * 0.2)) {
                stars.add(pos);
                con.addStar(pos.x, pos.y);
            }
        }
        for (int i = 0; i < stars.size(); i++) {
            int j = closestPosExclude(stars.get(i), stars, new int[]{i});
            if (con.connectionExists(i, j)) {
                int k = closestPosExclude(stars.get(i), stars, new int[]{i, j});
                if (con.connectionExists(i, k)) {
                    int l = closestPosExclude(stars.get(i), stars, new int[]{i, j, k});
                    con.addConnection(i, l);
                } else {
                    con.addConnection(i, k);
                }
            } else {
                con.addConnection(i, j);
            }
        }

        List<StarEnergy> energies = new ArrayList<>(StarEnergies.ENERGY_REGISTRY.stream().toList());
        int energyCount = 3 - (int) Math.sqrt(random.nextBetween(0, 3*3));
        for (int i = 0; i < energyCount && energies.size() > 0; i++) {
            StarEnergy option = energies.remove(random.nextInt(energies.size()));
            con.addEnergy(option, 1 - (float)i / 3);
        }
        if (random.nextFloat() < 0.5) {
            Constellation.MOON_POSITION[] moon_positions = Constellation.MOON_POSITION.values();
            Constellation.MOON_POSITION position = moon_positions[random.nextInt(moon_positions.length)];
            con.setPosVisibility(position);
        }
        if (random.nextFloat() < 0.5) {
            boolean[] phases = new boolean[]{true, true, true, true, true, true, true, true};
            for (int i = random.nextBetween(3, 6); i > 0; i--) {
                phases[random.nextInt(8)] = false;
            }
            con.setPhaseVisibility(phases);
        }
        return con;
    }
}
