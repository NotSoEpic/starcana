package dindcrzy.starcana.recipes;

import com.google.gson.JsonObject;
import dindcrzy.starcana.Starcana;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ArcaneTableSerializer extends BaseTableSerializer<ArcaneTableRecipe> {
    private ArcaneTableSerializer() {}
    public static final ArcaneTableSerializer INSTANCE = new ArcaneTableSerializer();
    public static final Identifier ID = Starcana.id("arcane_table_recipe");

    @Override
    public ArcaneTableRecipe read(Identifier id, JsonObject obj) {
        BaseTableSerializer.Results results = this.read(obj, ArcaneTableRecipe.size);
        return new ArcaneTableRecipe(results.inputs(), results.output(), id);
    }

    @Override
    public ArcaneTableRecipe read(Identifier id, PacketByteBuf buf) {
        BaseTableSerializer.Results results = this.read(buf, ArcaneTableRecipe.size);
        return new ArcaneTableRecipe(results.inputs(), results.output(), id);
    }

    @Override
    public void write(PacketByteBuf buf, ArcaneTableRecipe recipe) {
        this.write(buf, recipe, ArcaneTableRecipe.size);
    }
}
