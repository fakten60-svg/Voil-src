package dev.prestigeclient.renderlib.impl.render.resource;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.ARBTextureStorage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class GlTexture implements AutoCloseable {
    public final int tex;
    private final Cleanable cleanable;
    private final int target;
    private volatile boolean inited = false;
    private int format;
    private int width;
    private int height;
    private int levels = 1;
    private boolean autoMipmap = true;

    public GlTexture(int i, int j) {
        this.target = i;
        this.tex = j;
        this.cleanable = RenderAPI.GL_CLEANER.register(this, () -> RenderAPI.runNowOrPostpone(() -> GL11.glDeleteTextures(j)));
    }

    public void clear(int i, float[] afloat) {
        int j = componentsForFormat(i);
        if (afloat.length != j) {
            throw new IllegalArgumentException(
                "clear value length " + afloat.length + " does not match component count " + j + " for format 0x" + Integer.toHexString(i)
            );
        }

        long k = (long)this.width * this.height * j * 4L;
        ByteBuffer bytebuffer = MemoryUtil.memAlloc((int)k);

        try {
            for (int l = 0; l < this.width * this.height; l++) {
                for (int i1 = 0; i1 < j; i1++) {
                    bytebuffer.putFloat(afloat[i1]);
                }
            }

            bytebuffer.flip();
            GL11.glBindTexture(this.target, this.tex);
            GL11.glTexSubImage2D(this.target, 0, 0, 0, this.width, this.height, i, 5126, bytebuffer);
            if (this.autoMipmap && this.levels > 1) {
                GL30.glGenerateMipmap(this.target);
            }

            GL11.glBindTexture(this.target, 0);
        } finally {
            MemoryUtil.memFree(bytebuffer);
        }
    }

    public void init(int i, int j, int k) {
        this.init(i, j, k, this.autoMipmap ? maxMipLevels(j, k) : 1);
    }

    public void init(int i, int j, int k, int l) {
        if (this.inited) {
            throw new IllegalStateException("already inited - this is an immutable texture!");
        }

        if (l < 1) {
            throw new IllegalArgumentException("levels must be >= 1");
        }

        this.inited = true;
        this.format = i;
        this.width = j;
        this.height = k;
        this.levels = l;
        GL11.glBindTexture(this.target, this.tex);
        ARBTextureStorage.glTexStorage2D(this.target, l, i, j, k);
        if (l > 1) {
            GL11.glTexParameteri(this.target, 10241, 9987);
            GL11.glTexParameteri(this.target, 10240, 9729);
        }

        GL11.glBindTexture(this.target, 0);
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }

    public static GlTexture create(int i) {
        return new GlTexture(i, GL11.glGenTextures());
    }

    public void setFilter(int i) {
        this.setFilter(i, i);
    }

    public void setFilter(int i, int j) {
        GL11.glBindTexture(this.target, this.tex);
        GL11.glTexParameteri(this.target, 10241, i);
        GL11.glTexParameteri(this.target, 10240, j);
        GL11.glBindTexture(this.target, 0);
    }

    public void setData(int i, int j, int k, int l, int i1, int j1, ByteBuffer bytebuffer, boolean flag) {
        GL11.glBindTexture(this.target, this.tex);
        GL11.glTexSubImage2D(this.target, 0, i, j, k, l, i1, j1, bytebuffer);
        if (flag && this.levels > 1) {
            GL30.glGenerateMipmap(this.target);
        }

        GL11.glBindTexture(this.target, 0);
    }

    public void setData(int i, int j, int k, int l, int i1, int j1, ByteBuffer bytebuffer) {
        this.setData(i, j, k, l, i1, j1, bytebuffer, this.autoMipmap);
    }

    private static int componentsForFormat(int i) {
        return switch (i) {
            case 6401, 6402, 6403, 36244 -> 1;
            case 6407, 32992, 36248, 36250 -> 3;
            case 6408, 32993, 36249, 36251 -> 4;
            case 33319, 33320 -> 2;
            default -> throw new IllegalArgumentException("Unsupported format for clear emulation: 0x" + Integer.toHexString(i));
        };
    }

    public GlTexture withoutMipmaps() {
        if (this.inited) {
            throw new IllegalStateException("call withoutMipmaps() before init()");
        }

        this.autoMipmap = false;
        return this;
    }

    public void setEdgeMode(int i) {
        GL11.glBindTexture(this.target, this.tex);
        GL11.glTexParameteri(this.target, 10242, i);
        GL11.glTexParameteri(this.target, 10243, i);
        GL11.glBindTexture(this.target, 0);
    }

    public GlTexture closeAndReinit(int i, int j) {
        if (this.inited) {
            this.close();
        }

        GlTexture gltexture1 = create(this.target);
        if (!this.autoMipmap) {
            gltexture1.withoutMipmaps();
        }

        gltexture1.init(this.format, i, j, this.levels);
        return gltexture1;
    }

    public void generateMipmaps() {
        if (this.levels > 1) {
            GL11.glBindTexture(this.target, this.tex);
            GL30.glGenerateMipmap(this.target);
            GL11.glBindTexture(this.target, 0);
        }
    }

    public static int maxMipLevels(int i, int j) {
        int k = Integer.numberOfLeadingZeros(Math.max(i, j));
        return 32 - k;
    }
}
