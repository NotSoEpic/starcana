package dindcrzy.starcana.items;

import dindcrzy.starcana.Starcana;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static Item LUX_WAND = Registry.register(Registries.ITEM, Starcana.id("lux_wand"),
            new Item(new FabricItemSettings().maxCount(1)));
    public static ConstellationNotes CONSTELLATION_NOTES = Registry.register(Registries.ITEM, Starcana.id("constellation_notes"),
            new ConstellationNotes(new FabricItemSettings().maxCount(1)));

    public static void register() {

    }
}
