package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lol.lwes.voil.util.PlayerTargetUtil;

public class DynamicTextureCache {
    private static final byte[] EMPTY_DATA;
    private final Map pendingData = new ConcurrentHashMap();
    private final Map textures = new ConcurrentHashMap();
    private final Map retryTimes = new ConcurrentHashMap();
    private final Consumer uploadExecutor;

    public DynamicTextureCache(Consumer<String> consumer) {
        this.uploadExecutor = consumer;
    }

    static {
        byte[] abyte = new byte[0];
        EMPTY_DATA = abyte;
    }

    private GlTexture createTexture(String s) {
        byte[] abyte = (byte[])this.pendingData.remove(s);
        if (abyte != null && abyte.length != 0) {
            GlTexture gltexture;
            try {
                gltexture = PlayerTargetUtil.createTexture(abyte);
            } catch (RuntimeException runtimeexception) {
                return null;
            }

            if (gltexture == null) {
                return null;
            }

            this.textures.put(s, gltexture);
            this.retryTimes.remove(s);
            return gltexture;
        } else {
            return null;
        }
    }

    public void putData(String s, byte[] abyte) {
        if (s != null && !s.isBlank()) {
            this.pendingData.put(s, abyte == null ? EMPTY_DATA : abyte);
        }
    }

    public void remove(String s) {
        if (s != null) {
            this.textures.remove(s);
            this.pendingData.remove(s);
            this.retryTimes.remove(s);
        }
    }

    private void markRetry(String s) {
        long i = System.currentTimeMillis();
        Long olong = (Long)this.retryTimes.get(s);
        if (olong == null || i >= olong) {
            this.retryTimes.put(s, i + 300000L);
            this.uploadExecutor.accept(s);
        }
    }

    public void clear() {
        this.textures.clear();
        this.pendingData.clear();
        this.retryTimes.clear();
    }

    public GlTexture getTexture(String s) {
        if (s != null && !s.isBlank()) {
            GlTexture gltexture = (GlTexture)this.textures.get(s);
            if (gltexture != null) {
                return gltexture;
            }

            GlTexture gltexture1 = this.createTexture(s);
            if (gltexture1 != null) {
                return gltexture1;
            }

            this.markRetry(s);
            return null;
        } else {
            return null;
        }
    }
}
