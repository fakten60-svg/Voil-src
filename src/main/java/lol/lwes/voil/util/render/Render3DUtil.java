package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.awt.Color;
import lol.lwes.voil.setting.GradientSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Render3DUtil {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[][] FACE_EDGE_DIRECTIONS = new Direction[][]{
        {Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.NORTH},
        {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST},
        {Direction.WEST, Direction.UP, Direction.EAST, Direction.DOWN},
        {Direction.DOWN, Direction.EAST, Direction.UP, Direction.WEST},
        {Direction.DOWN, Direction.SOUTH, Direction.UP, Direction.NORTH},
        {Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.DOWN}
    };

    public static void drawBoxOutline(Matrix4f matrix4f, Box box, Color color) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, (float)box.minX, (float)box.minY, (float)box.minZ);
        Vector3d vector3d1 = RenderAPI.getRelativePos(RenderManager.renderContext, (float)box.maxX, (float)box.maxY, (float)box.maxZ);
        float f1 = (float)vector3d.x;
        float f2 = (float)vector3d.y;
        float f3 = (float)vector3d.z;
        float f = (float)vector3d1.x;
        drawBoxLines(f1, f2, f3, f, (float)vector3d1.y, (float)vector3d1.z, ColorUtil.toArgb(color), matrix4f);
    }

    public static void drawGradientLine(Vec3d vec3d, Vec3d vec3d1, GradientSetting gradientsetting) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, (float)vec3d.x, (float)vec3d.y, (float)vec3d.z);
        Vector3d vector3d1 = RenderAPI.getRelativePos(RenderManager.renderContext, (float)vec3d1.x, (float)vec3d1.y, (float)vec3d1.z);
        float[] afloat = gradientsetting.getPositions();
        int j = afloat.length;
        float[] afloat1 = new float[j + 2];
        int i = 0;
        i++;
        afloat1[0] = 0.0F;

        for (float f : afloat) {
            if (f > afloat1[i - 1] && f < 1.0F) {
                afloat1[i++] = f;
            }
        }

        afloat1[i++] = 1.0F;
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer());
        glbuffermeshbuilder.start();

        for (int k = 0; k < i; k++) {
            float f1 = afloat1[k];
            GlBufferMeshBuilder glbuffermeshbuilder1 = glbuffermeshbuilder.pos(
                (float)(vector3d.x + (vector3d1.x - vector3d.x) * f1),
                (float)(vector3d.y + (vector3d1.y - vector3d.y) * f1),
                (float)(vector3d.z + (vector3d1.z - vector3d.z) * f1)
            );
            Color color = gradientsetting.getColorAt(f1);
            glbuffermeshbuilder1.color(ColorUtil.toArgb(color));
        }

        for (int l = 0; l < i - 1; l++) {
            glbuffermeshbuilder.putIndices(new int[]{l, l + 1});
        }

        glbuffermeshbuilder.end();
    }

    public static void drawPillar(Matrix4f matrix4f, Vec3d vec3d, Color color, float f, float f1) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, (float)vec3d.x, (float)vec3d.y, (float)vec3d.z);
        float f2 = (float)vector3d.x;
        float f3 = (float)vector3d.y;
        float f4 = (float)vector3d.z;
        int i = ColorUtil.toArgb(color);
        ((GlBufferMeshBuilder)RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer()).start())
            .pos(matrix4f, f2 - f, f3, f4 - f)
            .color(i)
            .pos(matrix4f, f2 + f, f3, f4 - f)
            .color(i)
            .pos(matrix4f, f2 + f, f3, f4 + f)
            .color(i)
            .pos(matrix4f, f2 - f, f3, f4 + f)
            .color(i)
            .pos(matrix4f, f2 - f, f3 + f1, f4 - f)
            .color(i)
            .pos(matrix4f, f2 - f, f3 + f1, f4 + f)
            .color(i)
            .pos(matrix4f, f2 + f, f3 + f1, f4 + f)
            .color(i)
            .pos(matrix4f, f2 + f, f3 + f1, f4 - f)
            .color(i)
            .pos(matrix4f, f2 - f, f3, f4 - f)
            .color(i)
            .pos(matrix4f, f2 - f, f3 + f1, f4 - f)
            .color(i)
            .pos(matrix4f, f2 + f, f3 + f1, f4 - f)
            .color(i)
            .pos(matrix4f, f2 + f, f3, f4 - f)
            .color(i)
            .pos(matrix4f, f2 - f, f3, f4 + f)
            .color(i)
            .pos(matrix4f, f2 + f, f3, f4 + f)
            .color(i)
            .pos(matrix4f, f2 + f, f3 + f1, f4 + f)
            .color(i)
            .pos(matrix4f, f2 - f, f3 + f1, f4 + f)
            .color(i)
            .pos(matrix4f, f2 + f, f3, f4 - f)
            .color(i)
            .pos(matrix4f, f2 + f, f3 + f1, f4 - f)
            .color(i)
            .pos(matrix4f, f2 + f, f3 + f1, f4 + f)
            .color(i)
            .pos(matrix4f, f2 + f, f3, f4 + f)
            .color(i)
            .pos(matrix4f, f2 - f, f3, f4 - f)
            .color(i)
            .pos(matrix4f, f2 - f, f3, f4 + f)
            .color(i)
            .pos(matrix4f, f2 - f, f3 + f1, f4 + f)
            .color(i)
            .pos(matrix4f, f2 - f, f3 + f1, f4 - f)
            .color(i)
            .putIndices(
                new int[]{0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 13, 14, 12, 14, 15, 16, 17, 18, 16, 18, 19, 20, 21, 22, 20, 22, 23}
            )
            .end();
        drawFlatOutline(f2, f3, f4, f, f1, i, matrix4f);
    }

    public static void drawBlockSet(Matrix4f matrix4f, LongOpenHashSet longopenhashset, Color color, Color color1) {
        if (!longopenhashset.isEmpty()) {
            boolean flag = color != null;
            int i = flag ? ColorUtil.toArgb(color) : 0;
            int j = ColorUtil.toArgb(color1);
            GlBufferMeshBuilder glbuffermeshbuilder = flag ? RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer()) : null;
            GlBufferMeshBuilder glbuffermeshbuilder1 = RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer());
            if (flag) {
                glbuffermeshbuilder.start();
            }

            glbuffermeshbuilder1.start();
            byte b0 = 0;
            byte b1 = 0;
            float[][] afloat = new float[4][];
            LongIterator longiterator = longopenhashset.iterator();

            while (longiterator.hasNext()) {
                long k = longiterator.nextLong();
                int l = BlockPos.unpackLongX(k);
                int i1 = BlockPos.unpackLongY(k);
                int j1 = BlockPos.unpackLongZ(k);
                Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, l, i1, j1);
                float f = (float)vector3d.x;
                float f1 = (float)vector3d.y;
                float f2 = (float)vector3d.z;
                float f3 = f + 1.0F;
                float f4 = f1 + 1.0F;
                float f5 = f2 + 1.0F;

                for (Direction direction : DIRECTIONS) {
                    int l1 = direction.getOffsetX();
                    int k1 = l;
                    int j2 = k1 + l1;
                    l1 = direction.getOffsetY();
                    k1 = i1;
                    if (!longopenhashset.contains(BlockPos.asLong(j2, k1 + l1, j1 + direction.getOffsetZ()))) {
                        switch (direction) {
                            case DOWN:
                                afloat[0] = new float[]{f, f1, f2};
                                afloat[1] = new float[]{f, f1, f5};
                                afloat[2] = new float[]{f3, f1, f5};
                                afloat[3] = new float[]{f3, f1, f2};
                                break;
                            case UP:
                                afloat[0] = new float[]{f, f4, f2};
                                afloat[1] = new float[]{f3, f4, f2};
                                afloat[2] = new float[]{f3, f4, f5};
                                afloat[3] = new float[]{f, f4, f5};
                                break;
                            case NORTH:
                                afloat[0] = new float[]{f, f1, f2};
                                afloat[1] = new float[]{f, f4, f2};
                                afloat[2] = new float[]{f3, f4, f2};
                                afloat[3] = new float[]{f3, f1, f2};
                                break;
                            case SOUTH:
                                afloat[0] = new float[]{f, f1, f5};
                                afloat[1] = new float[]{f3, f1, f5};
                                afloat[2] = new float[]{f3, f4, f5};
                                afloat[3] = new float[]{f, f4, f5};
                                break;
                            case WEST:
                                afloat[0] = new float[]{f, f1, f2};
                                afloat[1] = new float[]{f, f1, f5};
                                afloat[2] = new float[]{f, f4, f5};
                                afloat[3] = new float[]{f, f4, f2};
                                break;
                            case EAST:
                                afloat[0] = new float[]{f3, f1, f2};
                                afloat[1] = new float[]{f3, f4, f2};
                                afloat[2] = new float[]{f3, f4, f5};
                                afloat[3] = new float[]{f3, f1, f5};
                        }

                        if (flag) {
                            for (float[] afloat1 : afloat) {
                                glbuffermeshbuilder.pos(matrix4f, afloat1[0], afloat1[1], afloat1[2]).color(i);
                            }

                            glbuffermeshbuilder.putIndices(new int[]{b0, b0 + 1, b0 + 2, b0, b0 + 2, b0 + 3});
                            b0 += 4;
                        }

                        Direction[] adirection = FACE_EDGE_DIRECTIONS[direction.ordinal()];

                        for (int i2 = 0; i2 < 4; i2++) {
                            boolean flag2;
                            label79: {
                                Direction direction1 = adirection[i2];
                                l1 = direction1.getOffsetX();
                                k1 = l;
                                j2 = k1 + l1;
                                l1 = direction1.getOffsetY();
                                k1 = i1;
                                int k2 = k1 + l1;
                                l1 = direction1.getOffsetZ();
                                k1 = j1;
                                if (longopenhashset.contains(BlockPos.asLong(j2, k2, k1 + l1))) {
                                    l1 = direction1.getOffsetX();
                                    k1 = l;
                                    j2 = k1 + l1;
                                    l1 = direction.getOffsetX();
                                    k1 = j2;
                                    j2 = k1 + l1;
                                    l1 = direction1.getOffsetY();
                                    k1 = i1;
                                    k2 = k1 + l1;
                                    l1 = direction.getOffsetY();
                                    k1 = k2;
                                    k2 = k1 + l1;
                                    l1 = direction1.getOffsetZ();
                                    k1 = j1;
                                    int l2 = k1 + l1;
                                    l1 = direction.getOffsetZ();
                                    k1 = l2;
                                    if (!longopenhashset.contains(BlockPos.asLong(j2, k2, k1 + l1))) {
                                        flag2 = true;
                                        break label79;
                                    }
                                }

                                flag2 = false;
                            }

                            boolean flag1 = flag2;
                            if (!flag1) {
                                float[] afloat2 = afloat[i2];
                                float[] afloat3 = afloat[(i2 + 1) % 4];
                                glbuffermeshbuilder1.pos(matrix4f, afloat2[0], afloat2[1], afloat2[2]).color(j);
                                glbuffermeshbuilder1.pos(matrix4f, afloat3[0], afloat3[1], afloat3[2]).color(j);
                                glbuffermeshbuilder1.putIndices(new int[]{b1, b1 + 1});
                                b1 += 2;
                            }
                        }
                    }
                }
            }

            if (flag) {
                glbuffermeshbuilder.end();
            }

            glbuffermeshbuilder1.end();
        }
    }

    public static void drawBlockFace(Matrix4f matrix4f, BlockPos blockpos, Direction direction, Color color, Color color1) {
        float f = blockpos.getX();
        float f1 = blockpos.getY();
        float f2 = blockpos.getZ();
        float f3 = f + 1.0F;
        float f4 = f1 + 1.0F;
        float f5 = f2 + 1.0F;
        float[][] afloat;
        switch (direction) {
            case DOWN:
                afloat = new float[][]{{f, f1, f2}, {f, f1, f5}, {f3, f1, f5}, {f3, f1, f2}};
                break;
            case UP:
                afloat = new float[][]{{f, f4, f2}, {f3, f4, f2}, {f3, f4, f5}, {f, f4, f5}};
                break;
            case NORTH:
                afloat = new float[][]{{f, f1, f2}, {f, f4, f2}, {f3, f4, f2}, {f3, f1, f2}};
                break;
            case SOUTH:
                afloat = new float[][]{{f, f1, f5}, {f3, f1, f5}, {f3, f4, f5}, {f, f4, f5}};
                break;
            case WEST:
                afloat = new float[][]{{f, f1, f2}, {f, f1, f5}, {f, f4, f5}, {f, f4, f2}};
                break;
            case EAST:
                afloat = new float[][]{{f3, f1, f2}, {f3, f4, f2}, {f3, f4, f5}, {f3, f1, f5}};
                break;
            default:
                return;
        }

        Vector3d[] avector3d = new Vector3d[4];

        for (int i = 0; i < 4; i++) {
            avector3d[i] = RenderAPI.getRelativePos(RenderManager.renderContext, afloat[i][0], afloat[i][1], afloat[i][2]);
        }

        int i1 = ColorUtil.toArgb(color);
        int j = ColorUtil.toArgb(color1);
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer());
        glbuffermeshbuilder.start();

        for (int k = 0; k < 4; k++) {
            glbuffermeshbuilder.pos(matrix4f, (float)avector3d[k].x, (float)avector3d[k].y, (float)avector3d[k].z).color(i1);
        }

        glbuffermeshbuilder.putIndices(new int[]{0, 1, 2, 0, 2, 3}).end();
        GlBufferMeshBuilder glbuffermeshbuilder1 = RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer());
        glbuffermeshbuilder1.start();

        for (int l = 0; l < 4; l++) {
            Vector3d vector3d = avector3d[l];
            Vector3d vector3d1 = avector3d[(l + 1) % 4];
            glbuffermeshbuilder1.pos(matrix4f, (float)vector3d.x, (float)vector3d.y, (float)vector3d.z).color(j);
            glbuffermeshbuilder1.pos(matrix4f, (float)vector3d1.x, (float)vector3d1.y, (float)vector3d1.z).color(j);
        }

        glbuffermeshbuilder1.putIndices(new int[]{0, 1, 2, 3, 4, 5, 6, 7}).end();
    }

    public static void drawGradientBox(
        Matrix4f matrix4f, Vec3d vec3d, GradientSetting gradientsetting, GradientSetting gradientsetting1, float f, float f1, float f2
    ) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, (float)vec3d.x, (float)vec3d.y, (float)vec3d.z);
        float f3 = (float)vector3d.x - f1;
        float f4 = (float)vector3d.x + f1;
        float f5 = (float)vector3d.y;
        float f6 = (float)vector3d.y + f2;
        float f7 = (float)vector3d.z - f1;
        float f8 = (float)vector3d.z + f1;
        float f9 = f1 * 2.0F;
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer());
        GlBufferMeshBuilder glbuffermeshbuilder1 = RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer());
        glbuffermeshbuilder.start();
        glbuffermeshbuilder1.start();
        GradientBoxEmitter gradientboxemitter = new GradientBoxEmitter(
            matrix4f, glbuffermeshbuilder, glbuffermeshbuilder1, gradientsetting, gradientsetting1, f
        );
        Vector3f vector3f = new Vector3f(0.0F, -f2, 0.0F);
        Vector3f vector3f19 = new Vector3f(f4, f6, f7);
        Vector3f vector3f20 = new Vector3f(-f9, 0.0F, 0.0F);
        gradientboxemitter.emitFace(vector3f19, vector3f20, vector3f, 6, 16, true);
        vector3f19 = new Vector3f(f3, f6, f8);
        vector3f20 = new Vector3f(f9, 0.0F, 0.0F);
        gradientboxemitter.emitFace(vector3f19, vector3f20, vector3f, 6, 16, true);
        vector3f19 = new Vector3f(f3, f6, f7);
        vector3f20 = new Vector3f(0.0F, 0.0F, f9);
        gradientboxemitter.emitFace(vector3f19, vector3f20, vector3f, 6, 16, true);
        vector3f19 = new Vector3f(f4, f6, f8);
        vector3f20 = new Vector3f(0.0F, 0.0F, -f9);
        gradientboxemitter.emitFace(vector3f19, vector3f20, vector3f, 6, 16, true);
        vector3f19 = new Vector3f(f3, f6, f7);
        vector3f20 = new Vector3f(f9, 0.0F, 0.0F);
        Vector3f vector3f21 = new Vector3f(0.0F, 0.0F, f9);
        gradientboxemitter.emitFace(vector3f19, vector3f20, vector3f21, 6, 6, false);
        vector3f19 = new Vector3f(f3, f5, f7);
        vector3f20 = new Vector3f(f9, 0.0F, 0.0F);
        vector3f21 = new Vector3f(0.0F, 0.0F, f9);
        gradientboxemitter.emitFace(vector3f19, vector3f20, vector3f21, 6, 6, false);
        glbuffermeshbuilder.end();
        glbuffermeshbuilder1.end();
    }

    public static void drawBoxFilled(Matrix4f matrix4f, Box box, Color color) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, (float)box.minX, (float)box.minY, (float)box.minZ);
        Vector3d vector3d1 = RenderAPI.getRelativePos(RenderManager.renderContext, (float)box.maxX, (float)box.maxY, (float)box.maxZ);
        float f = (float)vector3d.x;
        float f1 = (float)vector3d.y;
        float f2 = (float)vector3d.z;
        float f3 = (float)vector3d1.x;
        float f4 = (float)vector3d1.y;
        float f5 = (float)vector3d1.z;
        int i = ColorUtil.toArgb(color);
        ((GlBufferMeshBuilder)RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer()).start())
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f5)
            .color(i)
            .pos(matrix4f, f, f1, f5)
            .color(i)
            .pos(matrix4f, f, f4, f2)
            .color(i)
            .pos(matrix4f, f, f4, f5)
            .color(i)
            .pos(matrix4f, f3, f4, f5)
            .color(i)
            .pos(matrix4f, f3, f4, f2)
            .color(i)
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f2)
            .color(i)
            .pos(matrix4f, f, f1, f5)
            .color(i)
            .pos(matrix4f, f3, f1, f5)
            .color(i)
            .pos(matrix4f, f3, f4, f5)
            .color(i)
            .pos(matrix4f, f, f4, f5)
            .color(i)
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f, f1, f5)
            .color(i)
            .pos(matrix4f, f, f4, f5)
            .color(i)
            .pos(matrix4f, f, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f2)
            .color(i)
            .pos(matrix4f, f3, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f4, f5)
            .color(i)
            .pos(matrix4f, f3, f1, f5)
            .color(i)
            .putIndices(
                new int[]{0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 13, 14, 12, 14, 15, 16, 17, 18, 16, 18, 19, 20, 21, 22, 20, 22, 23}
            )
            .end();
        drawBoxLines(f, f1, f2, f3, f4, f5, i, matrix4f);
    }

    public static void drawFilledCube(Matrix4f matrix4f, double d0, double d1, double d2, float f, Color color) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, (float)d0, (float)d1, (float)d2);
        float f1 = (float)vector3d.x;
        float f2 = (float)vector3d.y;
        float f3 = (float)vector3d.z;
        int i = color.getRGB();
        GlBufferMeshBuilder glbuffermeshbuilder = RenderManager.bufferCollector.getBuffer(RenderManager.getTriangleNoCullLayer());
        glbuffermeshbuilder.start();
        byte b0 = 0;

        for (int j = 0; j < 30; j++) {
            double d3 = Math.PI * j / 30;
            double d4 = Math.PI * (j + 1) / 30;

            for (int k = 0; k < 30; k++) {
                double d5 = (Math.PI * 2) * k / 30;
                double d6 = (Math.PI * 2) * (k + 1) / 30;
                float f4 = f1 + (float)(Math.cos(d5) * Math.sin(d3)) * f;
                float f5 = f2 + (float)Math.cos(d3) * f;
                float f6 = f3 + (float)(Math.sin(d5) * Math.sin(d3)) * f;
                float f7 = f1 + (float)(Math.cos(d6) * Math.sin(d3)) * f;
                float f8 = f2 + (float)Math.cos(d3) * f;
                float f9 = f3 + (float)(Math.sin(d6) * Math.sin(d3)) * f;
                float f10 = f1 + (float)(Math.cos(d6) * Math.sin(d4)) * f;
                float f11 = f2 + (float)Math.cos(d4) * f;
                float f12 = f3 + (float)(Math.sin(d6) * Math.sin(d4)) * f;
                float f13 = f1 + (float)(Math.cos(d5) * Math.sin(d4)) * f;
                float f14 = f2 + (float)Math.cos(d4) * f;
                float f15 = f3 + (float)(Math.sin(d5) * Math.sin(d4)) * f;
                glbuffermeshbuilder.pos(matrix4f, f4, f5, f6).color(i);
                glbuffermeshbuilder.pos(matrix4f, f13, f14, f15).color(i);
                glbuffermeshbuilder.pos(matrix4f, f10, f11, f12).color(i);
                glbuffermeshbuilder.pos(matrix4f, f4, f5, f6).color(i);
                glbuffermeshbuilder.pos(matrix4f, f10, f11, f12).color(i);
                glbuffermeshbuilder.pos(matrix4f, f7, f8, f9).color(i);
                glbuffermeshbuilder.putIndices(new int[]{b0, b0 + 1, b0 + 2, b0 + 3, b0 + 4, b0 + 5});
                b0 += 6;
            }
        }

        glbuffermeshbuilder.end();
    }

    private static void drawFlatOutline(float f, float f1, float f2, float f3, float f4, int i, Matrix4f matrix4f) {
        ((GlBufferMeshBuilder)RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer()).start())
            .pos(matrix4f, f - f3, f1, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1, f2 + f3)
            .color(i)
            .pos(matrix4f, f + f3, f1, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1, f2 - f3)
            .color(i)
            .pos(matrix4f, f - f3, f1 + f4, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1 + f4, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1 + f4, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1 + f4, f2 + f3)
            .color(i)
            .pos(matrix4f, f + f3, f1 + f4, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1 + f4, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1 + f4, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1 + f4, f2 - f3)
            .color(i)
            .pos(matrix4f, f - f3, f1, f2 - f3)
            .color(i)
            .pos(matrix4f, f - f3, f1 + f4, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1 + f4, f2 - f3)
            .color(i)
            .pos(matrix4f, f + f3, f1, f2 + f3)
            .color(i)
            .pos(matrix4f, f + f3, f1 + f4, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1, f2 + f3)
            .color(i)
            .pos(matrix4f, f - f3, f1 + f4, f2 + f3)
            .color(i)
            .putIndices(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23})
            .end();
    }

    public static void drawMarker(Matrix4f matrix4f, Vec3d vec3d, Color color) {
        drawPillar(matrix4f, vec3d, color, 0.3F, 1.8F);
    }

    public static void drawBlockOutline(Matrix4f matrix4f, double d0, double d1, double d2, Color color) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, (float)d0, (float)d1, (float)d2);
        float f = (float)vector3d.x;
        float f1 = (float)vector3d.y;
        float f2 = (float)vector3d.z;
        int i = color.getRGB();
        ((GlBufferMeshBuilder)RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer()).start())
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f, f1 + 1.0F, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1 + 1.0F, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1 + 1.0F, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1 + 1.0F, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1 + 1.0F, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1 + 1.0F, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1 + 1.0F, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1 + 1.0F, f2)
            .color(i)
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f, f1 + 1.0F, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1 + 1.0F, f2)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f + 1.0F, f1 + 1.0F, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1, f2 + 1.0F)
            .color(i)
            .pos(matrix4f, f, f1 + 1.0F, f2 + 1.0F)
            .color(i)
            .putIndices(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23})
            .end();
    }

    public static void drawBlockTop(Matrix4f matrix4f, BlockPos blockpos, Color color, Color color1) {
        drawBlockFace(matrix4f, blockpos, Direction.UP, color, color1);
    }

    public static void drawWireBox(float f, float f1, float f2, float f3, float f4, float f5, int i) {
        Vector3d vector3d = RenderAPI.getRelativePos(RenderManager.renderContext, f, f1, f2);
        Vector3d vector3d1 = RenderAPI.getRelativePos(RenderManager.renderContext, f3, f4, f5);
        ((GlBufferMeshBuilder)RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer()).start())
            .pos((float)vector3d.x, (float)vector3d.y, (float)vector3d.z)
            .color(i)
            .pos((float)vector3d1.x, (float)vector3d1.y, (float)vector3d1.z)
            .color(i)
            .putIndices(new int[]{0, 1})
            .end();
    }

    private static void drawBoxLines(float f, float f1, float f2, float f3, float f4, float f5, int i, Matrix4f matrix4f) {
        ((GlBufferMeshBuilder)RenderManager.bufferCollector.getBuffer(RenderManager.getLineLayer()).start())
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f5)
            .color(i)
            .pos(matrix4f, f3, f1, f5)
            .color(i)
            .pos(matrix4f, f, f1, f5)
            .color(i)
            .pos(matrix4f, f, f1, f5)
            .color(i)
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f4, f5)
            .color(i)
            .pos(matrix4f, f3, f4, f5)
            .color(i)
            .pos(matrix4f, f, f4, f5)
            .color(i)
            .pos(matrix4f, f, f4, f5)
            .color(i)
            .pos(matrix4f, f, f4, f2)
            .color(i)
            .pos(matrix4f, f, f1, f2)
            .color(i)
            .pos(matrix4f, f, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f2)
            .color(i)
            .pos(matrix4f, f3, f4, f2)
            .color(i)
            .pos(matrix4f, f3, f1, f5)
            .color(i)
            .pos(matrix4f, f3, f4, f5)
            .color(i)
            .pos(matrix4f, f, f1, f5)
            .color(i)
            .pos(matrix4f, f, f4, f5)
            .color(i)
            .putIndices(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23})
            .end();
    }

    public static void drawLine(Vec3d vec3d, Vec3d vec3d1, Color color) {
        float f1 = (float)vec3d.x;
        float f2 = (float)vec3d.y;
        float f = (float)vec3d.z;
        drawWireBox(f1, f2, f, (float)vec3d1.x, (float)vec3d1.y, (float)vec3d1.z, ColorUtil.toArgb(color));
    }
}
