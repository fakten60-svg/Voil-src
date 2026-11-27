package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.util.PlayerTargetUtil;

public class TextureCache {
    private static final DynamicTextureCache textures = new DynamicTextureCache(ApiClient.INSTANCE::requestSharedThemeData);

    private TextureCache() {
    }

    public static void registerTexture(String s, byte[] abyte) {
        textures.putData(s, abyte);
    }

    public static void removeTexture(String s) {
        textures.remove(s);
    }

    private static GlTexture getDefaultTexture() {
        return (GlTexture)PlayerTargetUtil.defaultSkinTextures.key();
    }

    public static GlTexture getTexture(String s) {
        return s.isBlank() ? getDefaultTexture() : textures.getTexture(s);
    }
}
