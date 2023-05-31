package dindcrzy.starcana;

import com.mojang.blaze3d.systems.RenderSystem;
import dindcrzy.starcana.blocks.Tables.ArcaneTable.ArcaneTableScreenHandler;
import dindcrzy.starcana.recipes.ArcaneTableRecipe;
import dindcrzy.starcana.recipes.BaseTableRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArcaneTableScreen extends HandledScreen<ArcaneTableScreenHandler> {
    private static final Identifier TEXTURE = Starcana.id("textures/gui/container/arcane_table.png");
    public ArcaneTableScreen(ArcaneTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 186;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
        drawStarlightBars(matrices, x, y);
    }



    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    public BaseTableRecipe findRecipe() {
        return MinecraftClient.getInstance().world.getRecipeManager().getFirstMatch(ArcaneTableRecipe.Type.INSTANCE,
                getScreenHandler().getInventory(), MinecraftClient.getInstance().world).orElse(null);
    }

    private final int barWidth = backgroundWidth - 14;
    private void drawStarlightBars(MatrixStack matrices, int x, int y) {
        float s = getScreenHandler().getStarlightFract();
        float r = 0;
        BaseTableRecipe recipe = findRecipe();
        if (recipe != null) {
            r = getScreenHandler().getStarlightFract(recipe.getStarlight());
        }
        if (s >= r) { // more starlight than the recipe calls for
            int split = (int)(barWidth * r);
            int end = (int)(barWidth * s);
            drawStarlightBar(matrices, x, y, 0, split, 2);
            drawStarlightBar(matrices, x, y, split, end, 0);
        } else { // less starlight than the recipe calls for
            int split = (int)(barWidth * s);
            int end = (int)(barWidth * r);
            drawStarlightBar(matrices, x, y, 0, split, 2);
            drawStarlightBar(matrices, x, y, split, end, 1);
        }
    }

    // type: 0 - available + no cost, 1 - no available, cost, 2 - available + cost
    private void drawStarlightBar(MatrixStack matrices, int x, int y, int startX, int endX, int type) {
        drawTexture(matrices, x + 7 + startX, y + 75, 7 + startX, backgroundHeight + 3 + 16 * type, endX - startX, 15);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
