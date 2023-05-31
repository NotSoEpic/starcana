package dindcrzy.starcana.recipes;

import dindcrzy.starcana.blocks.ImplementedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public abstract class BaseTableRecipe implements Recipe<ImplementedInventory> {
    private final DefaultedList<Ingredient> inputs;
    private final ItemStack output;
    private final Identifier id;
    private final int size;
    private final int starlight;
    public BaseTableRecipe(DefaultedList<Ingredient> inputs, ItemStack output, Identifier id, int size, int starlight) {
        this.inputs = inputs;
        this.output = output;
        this.id = id;
        this.size = size;
        this.starlight = starlight;
    }

    public DefaultedList<Ingredient> getInputs() {
        return inputs;
    }
    public int getStarlight() { return starlight; }

    public boolean canCraft(int starlight) {
        return starlight >= this.starlight;
    }

    @Override
    public boolean matches(ImplementedInventory inventory, World world) {
        if (inventory.size() < size) return false;
        for (int i = 0; i < size; i++) {
            if (!getInputs().get(i).test(inventory.getStack(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(ImplementedInventory inventory, DynamicRegistryManager registryManager) {
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
