package dindcrzy.starcana.recipes;

import dindcrzy.starcana.Starcana;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModRecipes {
    public static void register() {
        Registry.register(Registries.RECIPE_SERIALIZER, ArcaneTableSerializer.ID,
                ArcaneTableSerializer.INSTANCE);
        Registry.register(Registries.RECIPE_TYPE, Starcana.id(ArcaneTableRecipe.Type.ID),
                ArcaneTableRecipe.Type.INSTANCE);
    }
}
