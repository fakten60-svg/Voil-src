package dev.prestigeclient.renderlib.api.texture;

import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RLTextureLoader {
    private static final Logger log = LoggerFactory.getLogger(RLTextureLoader.class);
    private static final boolean ANISOTROPIC_FILTERING_SUPPORTED = GL.getCapabilities().GL_EXT_texture_filter_anisotropic;
    private static final float MAX_ANISOTROPY;

    private static ByteBuffer loadImageData(String s) throws IOException {
        String s1 = s.startsWith("/") ? s.substring(1) : s;

        ByteBuffer bytebuffer;
        try (InputStream inputstream = RLTextureLoader.class.getClassLoader().getResourceAsStream(s1)) {
            if (inputstream == null) {
                throw new IOException("Resource not found: " + s1);
            }

            byte[] abyte = inputstream.readAllBytes();
            bytebuffer = ByteBuffer.allocateDirect(abyte.length);
            bytebuffer.put(abyte);
            bytebuffer.flip();
        }

        return bytebuffer;
    }

    static {
        if (ANISOTROPIC_FILTERING_SUPPORTED) {
            MAX_ANISOTROPY = GL11.glGetFloat(34047);
            log.info("Anisotropic filtering supported: Max {}x", MAX_ANISOTROPY);
        } else {
            MAX_ANISOTROPY = 1.0F;
            log.info("Anisotropic filtering not supported");
        }
    }

    public static GlTexture loadTexture(String s, int i, int j) throws IOException {
        return loadTexture(s, i, j, true);
    }

    public static GlTexture loadTexture(String s) throws IOException {
        return loadTexture(s, 9729, 33071, true);
    }

    public static GlTexture loadTexture(String s, int i, int j, boolean flag) throws IOException {
        ByteBuffer bytebuffer = loadImageData(s);

        GlTexture gltexture;
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            IntBuffer intbuffer = memorystack.mallocInt(1);
            IntBuffer intbuffer1 = memorystack.mallocInt(1);
            IntBuffer intbuffer2 = memorystack.mallocInt(1);
            ByteBuffer bytebuffer1 = STBImage.stbi_load_from_memory(bytebuffer, intbuffer, intbuffer1, intbuffer2, 4);
            if (bytebuffer1 == null) {
                throw new IOException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            try {
                int k = intbuffer.get(0);
                int l = intbuffer1.get(0);
                int j1 = k * l;
                int i1 = j1 * 4;
                if (bytebuffer1.remaining() < i1) {
                    throw new IOException("Image buffer too small: " + bytebuffer1.remaining() + " bytes, expected " + i1);
                }

                gltexture = GlTexture.create(3553);
                gltexture.init(32856, k, l);
                GL15.glBindBuffer(35052, 0);
                GL11.glPixelStorei(3314, 0);
                GL11.glPixelStorei(3316, 0);
                GL11.glPixelStorei(3315, 0);
                GL11.glPixelStorei(3317, 4);
                GL11.glBindTexture(3553, gltexture.tex);
                gltexture.setData(0, 0, k, l, 6408, 5121, bytebuffer1);
                if (flag) {
                    GL30.glGenerateMipmap(3553);
                    if (i == 9729) {
                        GL11.glTexParameteri(3553, 10241, 9987);
                        GL11.glTexParameteri(3553, 10240, 9729);
                    } else {
                        gltexture.setFilter(i);
                    }
                } else {
                    gltexture.setFilter(i);
                }

                gltexture.setEdgeMode(j);
                if (ANISOTROPIC_FILTERING_SUPPORTED && flag) {
                    float f = Math.min(64.0F, MAX_ANISOTROPY);
                    GL11.glTexParameterf(3553, 34046, f);
                }

                GL11.glBindTexture(3553, 0);
            } finally {
                STBImage.stbi_image_free(bytebuffer1);
            }
        }

        return gltexture;
    }
}
