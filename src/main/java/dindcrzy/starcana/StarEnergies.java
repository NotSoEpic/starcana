package dindcrzy.starcana;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Iterator;

public class StarEnergies {
    public static final RegistryKey<Registry<StarEnergy>> ENERGY =
            RegistryKey.ofRegistry(Starcana.id("energy"));
    public static final SimpleRegistry<StarEnergy> ENERGY_REGISTRY =
            FabricRegistryBuilder.createSimple(ENERGY)
                    .attribute(RegistryAttribute.SYNCED).buildAndRegister();


    public static StarEnergy DESTRUCTIVE = new StarEnergy(new Vec3d(1, 0.33, 0.33), 0.25f, Formatting.RED);
    public static StarEnergy NATURAL = new StarEnergy(new Vec3d(0.33, 1, 0.33), 0.25f, Formatting.GREEN);
    public static StarEnergy POWERFUL = new StarEnergy(new Vec3d(0.33, 0.33, 1), 0.25f, Formatting.BLUE);

    public static void register() {
        Registry.register(ENERGY_REGISTRY, Starcana.id("destructive"), DESTRUCTIVE);
        Registry.register(ENERGY_REGISTRY, Starcana.id("natural"), NATURAL);
        Registry.register(ENERGY_REGISTRY, Starcana.id("powerful"), POWERFUL);
    }


    public static float getTotalEnergy(StarEnergy energy, World world) {
        float sum = energy.baseline;
        for (Iterator<Constellation> it = Constellations.CONSTELLATION_REGISTRY.stream().iterator(); it.hasNext(); ) {
            Constellation constellation = it.next();
            float value = constellation.energies.getOrDefault(energy, 0f);
            if (value != 0) {
                sum += value * constellation.getAlphaFactor(world.getLunarTime());
            }
        }
        return sum;
    }
}
