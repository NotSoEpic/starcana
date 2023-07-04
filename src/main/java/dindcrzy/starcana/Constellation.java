package dindcrzy.starcana;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.*;

public class Constellation {
    public final Vec3d posInSky;
    public final Vec3d x;
    public final Vec3d y;
    public final ArrayList<Vec2f> stars = new ArrayList<>();
    public final ArrayList<Pair<Integer, Integer>> connections = new ArrayList<>();

    private final int phaseGracePeriod; // +- visibility period when moon phase changes
    private final int phaseSwitchShift; // +- time when moon phase changes
    private MOON_POSITION moonVisibility;

    // 0 full, 1 - 3 waning, 4 new, 5 - 7 waxing
    private final boolean[] phaseVisibility = {true, true, true, true, true, true, true, true};
    public final HashMap<StarEnergy, Float> energies = new HashMap<>();
    Constellation(long seed) {
        Random random = Random.create(seed);
        Vec3d posInSky = Helper.randomUnitVector(random);
        int tries = 10;
        while (--tries > 0 && Constellations.tooClose(posInSky)) {
            posInSky = Helper.randomUnitVector(random);
        }
        this.x = Helper.randomOrthogonalVector(posInSky, random);
        this.y = posInSky.crossProduct(x);
        this.posInSky = posInSky.multiply(100);
        this.phaseGracePeriod = random.nextBetween(100, 300);
        this.phaseSwitchShift = random.nextBetween(-200, 200);
        this.moonVisibility = MOON_POSITION.BOTH;
    }

    // should range from (-1,-1) to (1,1) ideally
    public Constellation addStar(float x, float y) {
        stars.add(new Vec2f(x, y));
        return this;
    }

    public boolean connectionExists(int i1, int i2) {
        return connections.stream().anyMatch(
                pair -> (pair.getA() == i1 && pair.getB() == i2) ||
                        (pair.getA() == i2 && pair.getB() == i1));
    }

    public Constellation addConnection(int i1, int i2) {
        if (!connectionExists(i1, i2))
            connections.add(new Pair<>(i1, i2));
        return this;
    }

    public Constellation addConnections(int[] is) {
        for (int i = 0; i < is.length - 1; i++) {
            addConnection(is[i], is[i + 1]);
        }
        return this;
    }

    // 0 full, 1 - 3 waning, 4 new, 5 - 7 waxing
    public Constellation setPhaseVisibility(boolean[] values) {
        System.arraycopy(values, 0, phaseVisibility, 0, 8);
        return this;
    }
    public Constellation setPosVisibility(MOON_POSITION vis) {
        this.moonVisibility = vis;
        return this;
    }

    public Constellation addEnergy(StarEnergy energy, float strength) {
        energies.put(energy, strength);
        return this;
    }

    public Vec3d starPos(int i, float scale) {
        Vec2f rel = stars.get(i).multiply(scale);
        return posInSky.add(x.multiply(rel.x)).add(y.multiply(rel.y));
    }

    public boolean isVisiblePhase(int phase) {
        return phaseVisibility[phase];
    }

    public float alphaFactorMoonPos(long lunarTime) {
        return switch (moonVisibility) {
            case VISIBLE -> Helper.getMoonVisibility(lunarTime);
            case NOT_VISIBLE -> 1f - Helper.getMoonVisibility(lunarTime);
            case TRANSITION -> {
                float v = Helper.getMoonVisibility(lunarTime);
                yield (float)MathHelper.clamp(5 * v * (1 - v) - 0.25, 0, 1);
            }
            default -> 1f;
        };
    }
    // the moon phase can change in the middle of the night thanks to 8/9ths speed
    // interpolates visibility so that it isn't a jarring jump
    public float alphaFactorMoonPhase(long lunarTime) {
        int prePhase = Helper.getMoonPhase(lunarTime - phaseGracePeriod + phaseSwitchShift);
        int postPhase = Helper.getMoonPhase(lunarTime + phaseGracePeriod + phaseSwitchShift);
        boolean inPrePhase = isVisiblePhase(prePhase);
        boolean inPostPhase = isVisiblePhase(postPhase);
        if (inPrePhase && inPostPhase) {
            return 1f;
        }
        if (!inPrePhase && !inPostPhase) {
            return 0f;
        }
        // switches over at 19500
        float lerp = (Math.floorMod(lunarTime + phaseSwitchShift, 27000) - 19500) / (phaseGracePeriod * 2f) + 0.5f;
        if (!inPostPhase) { // disappearing
            return 1f - lerp;
        } else { // appearing
            return lerp;
        }
    }
    public float getAlphaFactor(long lunarTime) {
        return alphaFactorMoonPhase(lunarTime) *
                alphaFactorMoonPos(lunarTime);
    }
    public boolean isVisible(long lunarTime) {
        return getAlphaFactor(lunarTime) * Helper.starAlpha(lunarTime) > 0.1;
    }

    public Vector3f getSkyVector(long lunarTime) {
        Matrix4f transform = new Matrix4f();
        transform.rotate((float)-Math.PI / 2f, 0, 1, 0);
        transform.rotate((float) (Helper.getLinearSunAngle(lunarTime) * Math.PI * 2f), 1, 0, 0);
        return transform.transformPosition(posInSky.normalize().toVector3f());
    }

    public MOON_POSITION getMoonVisibility() {
        return moonVisibility;
    }

    public boolean[] getPhaseVisibility() {
        return Arrays.copyOf(phaseVisibility, phaseVisibility.length);
    }

    private Vector3f color;
    public Vector3f getColor() {
        if (color == null) {
            color = new Vector3f(0, 0, 0);
            float weights = 0;
            for (StarEnergy energy : energies.keySet()) {
                float weight = energies.get(energy);
                color.add(energy.color.toVector3f().mul(weight));
                weights += weight;
            }
            if (weights > 0) {
                color.div(weights);
            } else {
                color = new Vector3f(1, 1, 1);
            }
        }
        return color;
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
            translationKey = Util.createTranslationKey("constellation", getId());
        }
        return translationKey;
    }

    public Text getFullInfoText(long lunarTime) {
        MutableText[] phasesText = new MutableText[8];
        for (int i = 0; i < 8; i++) {
            if (i == Helper.getMoonPhase(lunarTime)) {
                phasesText[i] = phaseVisibility[i] ?
                        Text.literal("✔").formatted(Formatting.GREEN) :
                        Text.literal("✘").formatted(Formatting.RED);
            } else {
                phasesText[i] = phaseVisibility[i] ? Text.literal("✔") : Text.literal("✘");
            }
        }

        MutableText energyText = Text.empty().formatted(Formatting.RESET);
        if (energies.size() > 0) {
            ArrayList<Map.Entry<StarEnergy, Float>> energiesSorted = new ArrayList<>(energies.entrySet().stream().toList());
            energiesSorted.sort(
                    (e1, e2) -> (int) ((e1.getValue() - e2.getValue()) * 100) // ascending
            );
            for (int i = energiesSorted.size() - 1; i >= 0; i--) {
                energyText.append(
                        Text.translatable(energiesSorted.get(i).getKey().getTranslationKey())
                                .append(String.format(" (%.2f)", energiesSorted.get(i).getValue()))
                                .formatted(energiesSorted.get(i).getKey().getTextColor()));
                if (i != 0) {
                    energyText.append(Text.literal(", ").formatted(Formatting.RESET));
                }
            }
        } else {
            energyText.append(Text.literal("None"));
        }

        return Text.translatable(
                "starcana.debug.full_constellation",
                Text.translatable(getTranslationKey()),
                Text.literal(moonVisibility.toString()).formatted(alphaFactorMoonPos(lunarTime) > 0.1 ?
                        Formatting.GREEN : Formatting.RED),
                phasesText[0], phasesText[1], phasesText[2], phasesText[3], phasesText[4], phasesText[5], phasesText[6], phasesText[7],
                getSkyVector(lunarTime).y > 0 ?
                        Text.literal("Upper").formatted(Formatting.GREEN) :
                        Text.literal("Lower").formatted(Formatting.RED),
                getSkyVector(lunarTime).z > 0 ? "South" : "North",
                getSkyVector(lunarTime).x > 0 ? "East" : "West",
                getSkyVector(lunarTime).y > 0 && isVisible(lunarTime) ?
                        Text.literal("Yes").formatted(Formatting.GREEN) :
                        Text.literal("No").formatted(Formatting.RED),
                energyText
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constellation that = (Constellation) o;
        return phaseGracePeriod == that.phaseGracePeriod && phaseSwitchShift == that.phaseSwitchShift && posInSky.equals(that.posInSky) && x.equals(that.x) && y.equals(that.y) && stars.equals(that.stars) && connections.equals(that.connections) && moonVisibility == that.moonVisibility && Arrays.equals(phaseVisibility, that.phaseVisibility);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(posInSky, x, y, stars, connections, phaseGracePeriod, phaseSwitchShift, moonVisibility);
        result = 31 * result + Arrays.hashCode(phaseVisibility);
        return result;
    }

    public enum MOON_POSITION {
        BOTH,
        VISIBLE,
        NOT_VISIBLE,
        TRANSITION;

        @Override
        public String toString() {
            return switch (this) {
                case BOTH -> "Always";
                case VISIBLE -> "Visible";
                case NOT_VISIBLE -> "Not Visible";
                case TRANSITION -> "On Horizon";
            };
        }
    }
}
