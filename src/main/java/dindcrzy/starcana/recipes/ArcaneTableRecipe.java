package dindcrzy.starcana.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ArcaneTableRecipe extends BaseTableRecipe {
    public static final int size = 9;
    public ArcaneTableRecipe(DefaultedList<Ingredient> inputs, ItemStack output, int starlight, Identifier id) {
        super(inputs, output, id, size, starlight);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ArcaneTableSerializer.INSTANCE;
    }


    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<ArcaneTableRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "arcane_table_recipe";
    }
}
