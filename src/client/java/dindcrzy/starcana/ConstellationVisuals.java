package dindcrzy.starcana;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ConstellationVisuals {
    public static final HashMap<Constellation, Pair<VertexBuffer, VertexBuffer>>
            constellations = new HashMap<>();
    public static final HashMap<Constellation, Float>
            visibilityInterpolate = new HashMap<>();

    public static Pair<BufferBuilder.BuiltBuffer, BufferBuilder.BuiltBuffer> buildConstellation(Constellation constellation, BufferBuilder buffer) {
        buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
        Vec3d[] starPos = new Vec3d[constellation.stars.size()];
        Random random = Random.create(473478L);
        for (int i = 0; i < constellation.stars.size(); i++) {
            Vec3d pos = constellation.starPos(i, 12f); // separation
            Vec3d pn = pos.normalize();
            double sMul = 0.9 + random.nextFloat() * 0.2;
            Vec3d x = Helper.randomOrthogonalVector(pn, random).multiply(0.75 * sMul); // star size
            Vec3d y = pn.crossProduct(x);
            starPos[i] = pos;
            int gonSides = random.nextBetween(3, 10);
            boolean spiky = ((gonSides & 1) == 0 && gonSides > 4 && random.nextFloat() < 0.5);
            Vec3d[] points = Helper.genStarVectors(
                    x, y, pos,
                    gonSides, spiky);
            for (int p = 1; p < points.length - 1; p++) {
                buffer.vertex(points[0].x, points[0].y, points[0].z).next();
                buffer.vertex(points[p].x, points[p].y, points[p].z).next();
                buffer.vertex(points[p+1].x, points[p+1].y, points[p+1].z).next();
            }
        }
        BufferBuilder.BuiltBuffer builtBuffer = buffer.end();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        for (Pair<Integer, Integer> pair : constellation.connections) {
            Vec3d pos1 = starPos[pair.getA()];
            Vec3d pos2 = starPos[pair.getB()];
            Vec3d rel = pos2.subtract(pos1); // pos1 -> pos2
            Vec3d x = rel.normalize();
            Vec3d y = rel.crossProduct(constellation.posInSky).normalize();

            double w = Math.min(rel.length() - 2, 0.2);

            if (w > 0) {
                Vec3d p1 = pos1.add(x.multiply(1));
                Vec3d p2 = pos2.subtract(x.multiply(1));
                Vec3d m1 = pos1.add(rel.multiply(0.5).add(y.multiply(w)));
                Vec3d m2 = pos1.add(rel.multiply(0.5).subtract(y.multiply(w)));
                buffer.vertex(m1.x, m1.y, m1.z).next();
                buffer.vertex(p1.x, p1.y, p1.z).next();
                buffer.vertex(m2.x, m2.y, m2.z).next();
                buffer.vertex(p2.x, p2.y, p2.z).next();
            } else {
                Starcana.LOGGER.warn("Error creating mesh for " + constellation.getTranslationKey() +
                        ":\nStar indices " + pair.getA() + ", " + pair.getB() +
                        " are too close (" + rel.length() + ")");
            }
        }
        return new Pair<>(builtBuffer, buffer.end());
    }
    public static Pair<VertexBuffer, VertexBuffer>
            genConBuffers(Constellation constellation, BufferBuilder bufferBuilder) {
        Pair<BufferBuilder.BuiltBuffer, BufferBuilder.BuiltBuffer> buffers =
                buildConstellation(constellation, bufferBuilder);

        VertexBuffer buffer1 = new VertexBuffer();
        buffer1.bind();
        buffer1.upload(buffers.getA());
        VertexBuffer.unbind();

        VertexBuffer buffer2 = new VertexBuffer();
        buffer2.bind();
        buffer2.upload(buffers.getB());
        VertexBuffer.unbind();
        return new Pair<>(buffer1, buffer2);
    }

    public static void init() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        Constellations.CONSTELLATION_REGISTRY.forEach(constellation -> {
            if (constellations.containsKey(constellation)) {
                constellations.get(constellation).getA().close();
                constellations.get(constellation).getB().close();
            }
            bufferBuilder.clear();
            constellations.put(constellation, genConBuffers(constellation, bufferBuilder));
        });
    }

    public static void render(Matrix4f viewMatrix, Matrix4f projectionMatrix, ClientWorld world) {
        float tick = (MinecraftClient.getInstance().getTickDelta() + world.getTime()) * 0.001f;
        ShaderProgram program = GameRenderer.getPositionProgram();
        HashMap<Identifier, Float> vis = ((IClientData)MinecraftClient.getInstance()).getConstellationVisibility();

        int delta = 200;

        for (Constellation key : constellations.keySet()) {
            float visibility = Helper.lerpVisibility(key, world, delta);

            // rendering stars
            CHelper.chromaticAberration(0.3f,
                    constellations.get(key).getA(), viewMatrix, projectionMatrix, program, tick);
            float[] rgba = Arrays.copyOf(RenderSystem.getShaderColor(), 4);

            // rendering connection points
            float visC = vis.getOrDefault(key.getId(), 0f);
            if (visC > 0) {
                RenderSystem.setShaderColor(rgba[0], rgba[1], rgba[2], rgba[3] * 0.25f * visC);
                CHelper.chromaticAberration(0.2f,
                        constellations.get(key).getB(), viewMatrix, projectionMatrix, program, tick);
                RenderSystem.setShaderColor(rgba[0], rgba[1], rgba[2], rgba[3]);
            }
        }
    }
}
