package dindcrzy.starcana.items;

import dindcrzy.starcana.Constellation;
import dindcrzy.starcana.Constellations;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConstellationNotes extends Item {
    public static final String CONSTELLATION_KEY = "Constellation";
    public static final Identifier MOON_ID = new Identifier("moon");
    public static final String MOON_KEY = "minecraft.moon";
    public ConstellationNotes(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Identifier id = getConstellationId(stack);
        if (MOON_ID.equals(id)) {
            // its called "Lunar Notes"... don't need to say it twice lol
            // tooltip.add(Text.translatable(MOON_KEY).formatted(Formatting.AQUA));
            if (context.isAdvanced()) {
                tooltip.add(Text.literal(MOON_ID.toString()).formatted(Formatting.GRAY));
            }
        } else if (id != null) {
            Constellation constellation = Constellations.CONSTELLATION_REGISTRY.get(id);
            if (constellation != null) {
                tooltip.add(Text.translatable(constellation.getTranslationKey()).formatted(Formatting.AQUA));
                if (context.isAdvanced()) {
                    tooltip.add(Text.literal(id.toString()).formatted(Formatting.GRAY));
                }
            }
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        Identifier id = getConstellationId(user.getStackInHand(hand));
        if (MOON_ID.equals(id)) {
            if (world.isClient()) {
                user.sendMessage(Text.of("THE MOON"));
            }
            return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
        } else if (id != null) {
            // doesnt work, its clientside only stuff :(
            // user.client.setScreen(new NotesScreen());
            Constellation constellation = Constellations.CONSTELLATION_REGISTRY.get(id);
            if (constellation != null) {
                if (world.isClient()) {
                    user.sendMessage(constellation.getFullInfoText(world.getLunarTime()));
                }
                return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
            }
        }
        return super.use(world, user, hand);
    }

    public static void setConstellation(ItemStack notes, @Nullable Identifier id) {
        if (notes.isOf(ModItems.CONSTELLATION_NOTES)) {
            NbtCompound nbt = notes.getOrCreateNbt();
            if (id == null) {
                nbt.remove(CONSTELLATION_KEY);
            } else {
                nbt.putString(CONSTELLATION_KEY, id.toString());
            }
        }
    }

    @Nullable
    public static Identifier getConstellationId(ItemStack notes) {
        if (notes.isOf(ModItems.CONSTELLATION_NOTES)) {
            NbtCompound nbt = notes.getOrCreateNbt();
            if (nbt.contains(CONSTELLATION_KEY, NbtElement.STRING_TYPE)) {
                return new Identifier(nbt.getString(CONSTELLATION_KEY));
            }
        }
        return null;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        Identifier id = getConstellationId(stack);
        if (MOON_ID.equals(id)) {
            return super.getTranslationKey(stack) + ".moon";
        }
        if (id != null) {
            return super.getTranslationKey(stack) + ".stars";
        }
        return super.getTranslationKey(stack);
    }
}
