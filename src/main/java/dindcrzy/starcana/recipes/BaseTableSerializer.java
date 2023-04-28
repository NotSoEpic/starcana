package dindcrzy.starcana.recipes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;

public abstract class BaseTableSerializer<T extends BaseTableRecipe> implements RecipeSerializer<T> {
    record Results(DefaultedList<Ingredient> inputs, ItemStack output) {}

    public Results read(JsonObject obj, int size) {
        BaseTableFormat recipeJson = new Gson().fromJson(obj, BaseTableFormat.class);
        DefaultedList<Ingredient> inputs = DefaultedList.ofSize(size, Ingredient.EMPTY);
        if (recipeJson.inputs.size() != size) throw(
                new JsonSyntaxException("Array inputs was of size " + recipeJson.inputs.size() + ", expected " + size));
        for (int i = 0; i < size; i++) {
            inputs.add(i, getOrIngredientEmpty(recipeJson.inputs.get(i)));
        }
        ItemStack output = getStack(recipeJson.output);
        return new Results(inputs, output);
    }

    public void write(PacketByteBuf buf, T recipe, int size) {
        for (int i = 0; i < size; i++) {
            recipe.getInputs().get(i).write(buf);
        }
        buf.writeItemStack(recipe.getOutput());
    }

    public Results read(PacketByteBuf buf, int size) {
        DefaultedList<Ingredient> inputs = DefaultedList.ofSize(size);
        for (int i = 0; i < size; i++) {
            inputs.add(i, Ingredient.fromPacket(buf));
        }
        ItemStack output = buf.readItemStack();
        return new Results(inputs, output);
    }

    public static Ingredient getOrIngredientEmpty(JsonElement obj) {
        if (obj.isJsonPrimitive() && Objects.equals(obj.getAsString(), "")) {
            return Ingredient.EMPTY;
        }
        return Ingredient.fromJson(obj);
    }

    public static ItemStack getStack(JsonObject obj) {
        String string = JsonHelper.getString(obj, "item");
        Item item = Registries.ITEM.getOrEmpty(new Identifier(string))
                .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + string + "'"));
        int i = JsonHelper.getInt(obj, "count", 1);
        if (i < 1) {
            throw(new JsonSyntaxException("Invalid item count '" + i + "'"));
        }
        // todo: json to nbt (is it needed?)
        return new ItemStack(item, i);
    }
}
