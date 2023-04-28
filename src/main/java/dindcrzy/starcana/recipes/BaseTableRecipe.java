package dindcrzy.starcana.recipes;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public abstract class BaseTableRecipe implements Recipe<Inventory> {
    private final DefaultedList<Ingredient> inputs;
    private final ItemStack output;
    private final Identifier id;
    private final int size;
    public BaseTableRecipe(DefaultedList<Ingredient> inputs, ItemStack output, Identifier id, int size) {
        this.inputs = inputs;
        this.output = output;
        this.id = id;
        this.size = size;
    }

    public DefaultedList<Ingredient> getInputs() {
        return inputs;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (inventory.size() != size) return false;
        for (int i = 0; i < inventory.size(); i++) {
            if (!inputs.get(i).test(inventory.getStack(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return output;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public abstract RecipeSerializer<?> getSerializer();

    @Override
    public abstract RecipeType<?> getType();
}
