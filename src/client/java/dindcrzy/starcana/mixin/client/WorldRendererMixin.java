package dindcrzy.starcana.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dindcrzy.starcana.CHelper;
import dindcrzy.starcana.ConstellationVisuals;
import dindcrzy.starcana.Helper;
import dindcrzy.starcana.IndexedVertexBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin {
	private final int[] indices = new int[13];

	@Shadow private @Nullable ClientWorld world;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void thirdTimesTheCharm(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
		ConstellationVisuals.init();
	}

	// https://github.com/SpongePowered/Mixin/issues/137
	// weird syntax but it works
	@Redirect(method = "renderStars()V", at = @At(value = "NEW", args="class=net/minecraft/client/gl/VertexBuffer"))
	private static VertexBuffer specialBuffer() {
		return new IndexedVertexBuffer();
	}

	@Redirect(method = "renderStars()V", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/render/WorldRenderer;renderStars(Lnet/minecraft/client/render/BufferBuilder;)Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;"))
	private BufferBuilder.BuiltBuffer coolerStars(WorldRenderer instance, BufferBuilder buffer) {
		Random random = Random.create(238523L);
		buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
		int vertices = 0;
		indices[0] = 0;
		for (int i = 0; i < 1200; i++) {
			int gonSides = random.nextBetween(3, 5);
			if (random.nextDouble() < 0.3) {
				gonSides = random.nextBetween(6, 10);
			}
			Vec3d pos = Helper.randomUnitVector(random);
			double n = pos.lengthSquared();
			if (n > 0.01) { // no div by 0
				n = 1.0 / Math.sqrt(n);
				pos = pos.multiply(n);
				double scale = 0.25 + 0.15 * random.nextDouble();
				Vec3d x = Helper.randomOrthogonalVector(pos, random).multiply(scale);
				Vec3d y = pos.crossProduct(x);
				pos = pos.multiply(100.0);
				boolean spiky = gonSides > 4 && ((gonSides & 1) == 0) && random.nextFloat() < 0.4;
				Vec3d[] points = Helper.genStarVectors(x, y, pos, gonSides, spiky);
				for (int p = 1; p < gonSides - 1; p++) {
					buffer.vertex(points[0].x, points[0].y, points[0].z).next();
					buffer.vertex(points[p].x, points[p].y, points[p].z).next();
					buffer.vertex(points[p+1].x, points[p+1].y, points[p+1].z).next();
				}
				vertices += 3 * (gonSides - 2);
			}
			if (i % 100 == 0 && i > 0) {
				indices[i / 100] = vertices;
			}
		}
		indices[12] = vertices;
		return buffer.end();
	}

	@Redirect(at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V"),
			method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V")
	private void colouredStars(VertexBuffer instance, Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram program) {
		assert world != null;
		float tick = (MinecraftClient.getInstance().getTickDelta() + world.getTime()) * 0.001f;
		float[] rgba = Arrays.copyOf(RenderSystem.getShaderColor(), 4);
		if (instance instanceof IndexedVertexBuffer buffer) {
			float[] rgbA = Arrays.copyOf(rgba, 4);
			for (int i = 0; i < 12; i++) {
				float visMul = (float)(1 + 0.4 * Math.sin(tick * (40 + i * 2) + i * 123)); // twinkle :3
				rgbA[3] = rgba[3] * visMul;
				buffer.setIndexOffset(indices[i]);
				buffer.setElementCount(indices[i + 1] - indices[i]);
				CHelper.chromaticAberration(0.3f, buffer, viewMatrix, projectionMatrix,
						program, tick, rgbA);
			}
		} else {
			// should never reach here but /shrug
			CHelper.chromaticAberration(0.3f, instance, viewMatrix, projectionMatrix,
					program, tick, rgba);
		}
		ConstellationVisuals.render(viewMatrix, projectionMatrix, this.world, rgba);
	}

	@ModifyVariable(name = "matrix4f2", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 2, target = "Lnet/minecraft/client/render/BufferBuilder;begin(Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;)V"),
	method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V")
	private Matrix4f changeMoonPos(Matrix4f value, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable) {
		assert world != null;
		matrices.push();
		// undoing the sun transformation
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.world.getSkyAngle(tickDelta) * -360.0f));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
		// doing the moon transformation
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Helper.getLinearMoonAngle(world.getLunarTime()) * 360.0f));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(Helper.getMoonTilt(world.getLunarTime()) * 360.0f));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
		Matrix4f newValue = matrices.peek().getPositionMatrix();
		matrices.pop();
		return newValue;
	}
}