package dindcrzy.starcana;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.HashMap;

public class ConstellationVisuals {
    private record IndexData(int starSize, long starIndex, int lineSize, long lineIndex) {}
    public static final IndexedVertexBuffer starBuffer = new IndexedVertexBuffer();
    public static final HashMap<Constellation, IndexData> indexData = new HashMap<>();
    /* public static final HashMap<Constellation, Pair<VertexBuffer, VertexBuffer>>
            constellations = new HashMap<>(); */

    public static long buildConstellation(Constellation constellation, BufferBuilder buffer, long offset) {
        long starIndex = offset;
        int starSize = 0;

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
            starSize += 3 * (points.length - 2);
        }

        offset += starSize;
        long lineIndex = offset;
        int lineSize = 0;

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

                buffer.vertex(m2.x, m2.y, m2.z).next();
                buffer.vertex(p2.x, p2.y, p2.z).next();
                buffer.vertex(m1.x, m1.y, m1.z).next();

                lineSize += 6;
            } else {
                Starcana.LOGGER.warn("Error creating mesh for " + constellation.getId() +
                        ":\nStar indices " + pair.getA() + ", " + pair.getB() +
                        " are too close (" + rel.length() + ")");
            }
        }

        offset += lineSize;
        indexData.put(constellation, new IndexData(starSize, starIndex, lineSize, lineIndex));
        return offset;
    }

    public static void init() {
        long offset = 0;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
        for (Constellation constellation : Constellations.CONSTELLATION_REGISTRY) {
            offset = buildConstellation(constellation, bufferBuilder, offset);
        }
        starBuffer.bind();
        starBuffer.upload(bufferBuilder.end());
        VertexBuffer.unbind();
    }

    public static void render(Matrix4f viewMatrix, Matrix4f projectionMatrix, ClientWorld world, float[] rgba) {
        float tick = (MinecraftClient.getInstance().getTickDelta() + world.getTime()) * 0.001f;
        starBuffer.bind();
        ShaderProgram program = GameRenderer.getPositionProgram();
        HashMap<Identifier, Float> vis = ((IClientData)MinecraftClient.getInstance()).getConstellationVisibility();

        float[] rgbA = Arrays.copyOf(rgba, 4);
        for (Constellation key : indexData.keySet()) {
            float visibility = key.getAlphaFactor(world.getLunarTime());
            if (visibility > 0) {
                float visMul = (float)(1 + 0.3 * Math.sin(tick * 40 + (key.hashCode() % 123))); // more twinkle :o
                visibility *= visMul;
                IndexData data = indexData.get(key);

                starBuffer.setElementCount(data.starSize);
                starBuffer.setIndexOffset(data.starIndex);

                rgbA[3] = rgba[3] * visibility;
                // rendering stars
                CHelper.chromaticAberration(0.3f,
                        starBuffer, viewMatrix, projectionMatrix, program, tick, rgbA);

                // rendering connection points
                float visC = vis.getOrDefault(key.getId(), 0f);
                if (visC > 0) {
                    starBuffer.setElementCount(data.lineSize);
                    starBuffer.setIndexOffset(data.lineIndex);

                    rgbA[3] = rgba[3] * 0.25f * visC * visibility;
                    CHelper.chromaticAberration(0.2f,
                            starBuffer, viewMatrix, projectionMatrix, program, tick, rgbA);
                }
            }
        }
        // probably not necessary but just to be safe
        RenderSystem.setShaderColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        VertexBuffer.unbind();
    }
}
