package lol.lwes.voil.util.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.texture.RLTextureLoader;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mgr.ShaderManager;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import dev.prestigeclient.renderlib.impl.render.resource.ShaderProgram;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RectInstancer;
import dev.prestigeclient.renderlib.impl.render.ui.UiRenderer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import lol.lwes.voil.util.render.EmojiGlyph;
import lol.lwes.voil.util.render.TextureStateApplier;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class EmojiAtlas {
    public static final ShaderProgram EMOJI_SHADER = ShaderManager.loadShader(
        RLPath.of("shaders/instance_built_pos_tex.vert"), RLPath.of("assets/tulip", "shaders/core/emoji_sprite.fsh"), false
    );
    private final Map glyphsByText = new HashMap();
    private final Map glyphsByAlias;
    private final GlTexture atlasTexture;
    private final float lineHeight;
    private int maxGlyphLength;
    private float fontSize;
    public Function<EmojiAtlas, Vector4f> tintProvider;
    public BiFunction<EmojiAtlas, StateApplier[], StateApplier[]> offsetProvider;
    public Function<EmojiAtlas, UiRenderer> rendererProvider;
    private final Map<String, EmojiGlyph> glyphsByShortcode;
    private final RectInstancer rectInstancer;

    public EmojiAtlas(String s, String s1) throws IOException {
        this.glyphsByAlias = new HashMap();
        this.maxGlyphLength = 1;
        this.fontSize = 16.0F;
        this.glyphsByShortcode = new LinkedHashMap<>();
        this.rectInstancer = new RectInstancer(null, DefaultPreBuiltVertices.SQUARE_TEX.getVAOIndexMesh(), (object, list) -> {
            MSDFFontAtlas.SimpleUvRectExtension msdffontatlas$simpleuvrectextension = (MSDFFontAtlas.SimpleUvRectExtension)object;
            list.get(0).pad(msdffontatlas$simpleuvrectextension.uvOffset());
            list.get(1).pad(msdffontatlas$simpleuvrectextension.uvScale());
        }, new String[]{"UvOffset", "UvScale"});
        String s2 = s + "/fonts/" + s1;
        ClassLoader classloader = EmojiAtlas.class.getClassLoader();
        String s6 = s2 + ".json";

        JsonObject jsonobject;
        try (InputStream inputstream = classloader.getResourceAsStream(s6)) {
            if (inputstream == null) {
                throw new IOException("missing emoji metadata: " + s2 + ".json");
            }

            jsonobject = JsonParser.parseReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8)).getAsJsonObject();
        }

        JsonObject jsonobject4 = jsonobject.getAsJsonObject("atlas");
        float f = jsonobject4.get("width").getAsFloat();
        float f1 = jsonobject4.get("height").getAsFloat();
        this.lineHeight = jsonobject.getAsJsonObject("metrics").get("lineHeight").getAsFloat();
        JsonArray jsonarray = jsonobject.getAsJsonArray("glyphs");

        for (int i = 0; i < jsonarray.size(); i++) {
            JsonObject jsonobject1 = jsonarray.get(i).getAsJsonObject();
            JsonObject jsonobject2 = jsonobject1.getAsJsonObject("planeBounds");
            JsonObject jsonobject3 = jsonobject1.getAsJsonObject("atlasBounds");
            float f2 = jsonobject3.get("left").getAsFloat();
            float f3 = jsonobject3.get("right").getAsFloat();
            float f4 = jsonobject3.get("top").getAsFloat();
            float f5 = jsonobject3.get("bottom").getAsFloat();
            String s3 = decodeCodepoints(jsonobject1.getAsJsonArray("codepoints"));
            String s4 = jsonobject1.has("name") ? jsonobject1.get("name").getAsString() : s3;
            EmojiGlyph emojiglyph = new EmojiGlyph(
                s3,
                s4,
                jsonobject1.get("advance").getAsFloat(),
                jsonobject2.get("left").getAsFloat(),
                jsonobject2.get("bottom").getAsFloat(),
                jsonobject2.get("right").getAsFloat(),
                jsonobject2.get("top").getAsFloat(),
                f2 / f,
                1.0F - f4 / f1,
                f3 / f,
                1.0F - f5 / f1
            );
            this.glyphsByText.put(s3, emojiglyph);
            this.glyphsByShortcode.put(s4, emojiglyph);
            this.maxGlyphLength = Math.max(this.maxGlyphLength, s3.length());
        }

        this.atlasTexture = RLTextureLoader.loadTexture(s2 + ".png", 9729, 33071, false);
    }

    public void setFontSize(float f) {
        this.fontSize = f;
    }

    public float drawGlyph(EmojiGlyph emojiglyph, float f, float f1, float f2, float f3) {
        float f4 = f1 + this.lineHeight * f2;
        float f5 = f + emojiglyph.getAtlasY() * f2;
        float f6 = f4 - emojiglyph.getUMin() * f2;
        float f7 = (emojiglyph.getHeight() - emojiglyph.getAtlasY()) * f2;
        float f8 = (emojiglyph.getUMin() - emojiglyph.getWidth()) * f2;
        int j = Math.round(Math.max(0.0F, Math.min(1.0F, f3)) * 255.0F) << 24;
        int i = j | 16777215;
        UiRenderer uirenderer = this.rendererProvider == null ? (MSDFFontAtlas.class.cast(null) == null ? null : null) : this.rendererProvider.apply(this);
        if (uirenderer == null) {
            throw new IllegalStateException("toUiRenderer not set on EmojiAtlas");
        }

        uirenderer.addRectangle(
            this.getRectInstancer(),
            f5,
            f6,
            f7,
            f8,
            i,
            this.tintProvider == null ? null : this.tintProvider.apply(this),
            new MSDFFontAtlas.SimpleUvRectExtension(
                new Vector2f(emojiglyph.getVMin(), emojiglyph.getUMax()),
                new Vector2f(emojiglyph.getVMax() - emojiglyph.getVMin(), emojiglyph.getAdvance() - emojiglyph.getUMax())
            )
        );
        return emojiglyph.getAtlasX() * f2;
    }

    public EmojiGlyph findGlyphAt(String s, int i) {
        int l = this.maxGlyphLength;
        int k = s.length();

        for (int j = Math.min(l, k - i); j > 0; j--) {
            EmojiGlyph emojiglyph = (EmojiGlyph)this.glyphsByText.get(s.substring(i, i + j));
            if (emojiglyph != null) {
                return emojiglyph;
            }
        }

        return null;
    }

    public boolean hasGlyph(String s) {
        return this.glyphsByText.containsKey(s);
    }

    public void close() {
        if (this.atlasTexture != null) {
            this.atlasTexture.close();
        }
    }

    public float getScaledLineHeight(float f) {
        return this.lineHeight * f;
    }

    public List<EmojiGlyph> searchGlyphs(String s, int i) {
        ArrayList<EmojiGlyph> arraylist = new ArrayList<>();
        ArrayList<EmojiGlyph> arraylist1 = new ArrayList<>();
        String s1 = s.toLowerCase();

        for (Entry<String, EmojiGlyph> entry : this.glyphsByShortcode.entrySet()) {
            if (entry.getKey().startsWith(s1)) {
                arraylist.add(entry.getValue());
                if (arraylist.size() >= i) {
                    return arraylist;
                }
            } else if (!s1.isEmpty() && entry.getKey().contains(s1)) {
                arraylist1.add(entry.getValue());
            }
        }

        for (EmojiGlyph emojiglyph : arraylist1) {
            if (arraylist.size() >= i) {
                break;
            }

            arraylist.add(emojiglyph);
        }

        return arraylist;
    }

    public float getFontSize() {
        return this.fontSize;
    }

    private static String decodeCodepoints(JsonArray jsonarray) {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < jsonarray.size(); i++) {
            stringbuilder.appendCodePoint(jsonarray.get(i).getAsInt());
        }

        return stringbuilder.toString();
    }

    public String replaceShortcodes(String s) {
        if (s.indexOf(58) < 0) {
            return s;
        }

        StringBuilder stringbuilder = new StringBuilder(s.length());
        int i = 0;

        while (i < s.length()) {
            char c0 = s.charAt(i);
            if (c0 == ':') {
                int j = s.indexOf(58, i + 1);
                if (j > i + 1 && j - i <= 34) {
                    EmojiGlyph emojiglyph = this.glyphsByShortcode.get(s.substring(i + 1, j));
                    if (emojiglyph != null) {
                        stringbuilder.append(emojiglyph.getEmoji());
                        i = j + 1;
                        continue;
                    }
                }
            }

            stringbuilder.append(c0);
            i++;
        }

        return stringbuilder.toString();
    }

    public EmojiGlyph getGlyphByShortcode(String s) {
        return this.glyphsByShortcode.get(s);
    }

    public float getGlyphAdvance(EmojiGlyph emojiglyph, float f) {
        return emojiglyph.getAtlasX() * f;
    }

    public RectInstancer getRectInstancer() {
        StateApplier[] astateapplier = new StateApplier[]{
            StateApplier.framebufferState(RenderAPI.getMinecraftFramebuffer()),
            StateApplier.shaderState(EMOJI_SHADER),
            StateApplier.flagState(2884, false, true),
            StateApplier.flagState(2929, true, false),
            StateApplier.scissorState(false, false, 0, 0, 0, 0),
            StateApplier.depthFuncState(515, 515),
            StateApplier.blendState(true, true),
            new TextureStateApplier(this.atlasTexture)
        };
        if (this.offsetProvider != null) {
            astateapplier = this.offsetProvider.apply(this, astateapplier);
        }

        int i = Arrays.hashCode(astateapplier);
        if (this.glyphsByAlias.get(i) == null) {
            this.glyphsByAlias.put(i, this.rectInstancer.copyWithLayer(new RenderLayer(VertexFormats.POSITION_UV, 4, false, astateapplier)));
        }

        return (RectInstancer)this.glyphsByAlias.get(i);
    }
}
