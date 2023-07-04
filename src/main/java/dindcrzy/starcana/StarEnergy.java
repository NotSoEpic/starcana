package dindcrzy.starcana;

import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

public class StarEnergy {
    public final Vec3d color;
    public final float baseline;
    public final Formatting textColor;
    public StarEnergy(Vec3d color, float baseline, Formatting textColor) {
        this.color = color;
        this.baseline = baseline;
        this.textColor = textColor;
    }

    private Identifier id;
    public Identifier getId() {
        if (id == null) {
            id = StarEnergies.ENERGY_REGISTRY.getId(this);
        }
        return id;
    }

    private String translationKey;
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.createTranslationKey("energy", getId());
        }
        return translationKey;
    }

    public Formatting getTextColor() {
        return textColor;
    }
}
