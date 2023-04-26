package dindcrzy.starcana;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CHelper {
    public static void chromaticAberration(float intensity, VertexBuffer buffer, Matrix4f viewMatrix,
                                           Matrix4f projectionMatrix, ShaderProgram program, float tick, float[] rgba) {

        buffer.bind();
        RenderSystem.setShaderColor(0, rgba[1], 0, rgba[3]);
        buffer.draw(viewMatrix, projectionMatrix, program);

        // red / blue (shifted, opposite directions)
        Vector3f aberration = Helper.starShift(tick, 0f).mul(intensity);
        RenderSystem.setShaderColor(rgba[0], 0, 0, rgba[3]);
        buffer.draw(new Matrix4f(viewMatrix).translate(aberration), projectionMatrix, program);

        RenderSystem.setShaderColor(0, 0, rgba[2], rgba[3]);
        buffer.draw(new Matrix4f(viewMatrix).translate(aberration.negate()), projectionMatrix, program);
        VertexBuffer.unbind();
    }
}
