package dindcrzy.starcana.mixin.client;

import dindcrzy.starcana.IClientData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements IClientData {
    final HashSet<Identifier> foundConstellations = new HashSet<>();
    final HashMap<Identifier, Float> constellationVisibility = new HashMap<>();

    @Override
    public HashSet<Identifier> getFoundConstellations() {
        return foundConstellations;
    }
    @Override
    public HashMap<Identifier, Float> getConstellationVisibility() {
        return constellationVisibility;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateVisibility(CallbackInfo ci) {
        HashSet<Identifier> knownConstellations = new HashSet<>();
        knownConstellations.addAll(constellationVisibility.keySet());
        knownConstellations.addAll(foundConstellations);
        for (Identifier id : knownConstellations) {
            if (foundConstellations.contains(id)) {
                constellationVisibility.put(id,
                         Math.min(1f, constellationVisibility.getOrDefault(id, 0f) + 0.01f)
                );
            } else {
                constellationVisibility.put(id,
                        Math.max(0f, constellationVisibility.getOrDefault(id, 0f) - 0.01f)
                );
            }
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void clearConstellationCache(Screen screen, CallbackInfo ci) {
        foundConstellations.clear();
        constellationVisibility.clear();
    }
}
