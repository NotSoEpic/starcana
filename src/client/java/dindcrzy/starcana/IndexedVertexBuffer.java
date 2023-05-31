package dindcrzy.starcana;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;

public class IndexedVertexBuffer extends VertexBuffer {
    private long indexOffset = 0L;
    private int elementCount = 0;

    @Override
    public void upload(BufferBuilder.BuiltBuffer buffer) {
        super.upload(buffer);
        this.indexOffset = 0L;
        this.elementCount = this.indexCount;
    }

    @Override
    public void draw() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._drawElements(
                this.drawMode.glMode,
                this.elementCount,
                this.getIndexType().glType,
                this.indexOffset * 2 // WHY TWO???
        );
    }

    public void setIndexOffset(long indexOffset) {
        this.indexOffset = Math.min(Math.max(indexOffset, 0), this.indexCount);
    }

    public void setElementCount(int elementCount) {
        this.elementCount = Math.min(Math.max(elementCount, 0), this.indexCount);
    }
}
