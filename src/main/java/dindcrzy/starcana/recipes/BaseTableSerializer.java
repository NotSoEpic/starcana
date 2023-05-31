package dindcrzy.starcana.recipes;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;

public abstract class BaseTableSerializer<T extends BaseTableRecipe> implements RecipeSerializer<T> {
    record Results(DefaultedList<Ingredient> inputs, ItemStack output, int starlight) {}

    public Results read(JsonObject obj, int size) {
        DefaultedList<Ingredient> inputs = DefaultedList.ofSize(size, Ingredient.EMPTY);
        JsonArray inArr = obj.getAsJsonArray("inputs");
        if (inArr.size() != size) throw(
                new JsonSyntaxException("Array inputs was of size " + inArr.size() + ", expected " + size));
        for (int i = 0; i < size; i++) {
            Ingredient ingredient = getOrIngredientEmpty(inArr.get(i));
            inputs.set(i, ingredient);
        }
        int starlight = 0;
        if (obj.has("starlight")) {
            if (obj.get("starlight") instanceof JsonPrimitive prim && prim.isNumber()) {
                starlight = prim.getAsInt();
            } else {
                throw(new JsonSyntaxException("Expected number for starlight, got " +
                        obj.get("starlight").toString() + " instead"));
            }
        }
        ItemStack output = getStack(obj.get("output"));
        return new Results(inputs, output, starlight);
    }

    public void write(PacketByteBuf buf, T recipe, int size) {
        for (int i = 0; i < size; i++) {
            recipe.getInputs().get(i).write(buf);
        }
        buf.writeInt(recipe.getStarlight());
        buf.writeItemStack(recipe.getOutput());
    }

    public Results read(PacketByteBuf buf, int size) {
        DefaultedList<Ingredient> inputs = DefaultedList.ofSize(size);
        for (int i = 0; i < size; i++) {
            inputs.set(i, Ingredient.fromPacket(buf));
        }
        int starlight = buf.readInt();
        ItemStack output = buf.readItemStack();
        return new Results(inputs, output, starlight);
    }

    public static Ingredient getOrIngredientEmpty(JsonElement obj) {
        if (obj.isJsonPrimitive()) {
            String v = obj.getAsString();
            if (Objects.equals(v, "")) {
                return Ingredient.EMPTY;
            } else {
                Item item = Registries.ITEM.get(new Identifier(v));
                if (item == Items.AIR) {
                    throw new JsonSyntaxException("Invalid item id '" + v + "'");
                }
                return Ingredient.ofItems(item);
            }
        }
        return Ingredient.fromJson(obj);
    }

    public static ItemStack getStack(JsonElement elem) {
        if (elem.isJsonPrimitive()) {
            String v = elem.getAsString();
            Item item = Registries.ITEM.get(new Identifier(v));
            if (item == Items.AIR) {
                throw new JsonSyntaxException("Invalid item id '" + v + "'");
            }
            return new ItemStack(item);
        } else if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            String string = JsonHelper.getString(obj, "item");
            Item item = Registries.ITEM.getOrEmpty(new Identifier(string))
                    .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + string + "'"));
            int i = JsonHelper.getInt(obj, "count", 1);
            if (i < 1) {
                throw (new JsonSyntaxException("Invalid item count '" + i + "'"));
            }
            // todo: json to nbt (is it needed?)
            return new ItemStack(item, i);
        } else {
            throw new JsonSyntaxException("Itemstack json element is not string or object");
        }
    }
}
