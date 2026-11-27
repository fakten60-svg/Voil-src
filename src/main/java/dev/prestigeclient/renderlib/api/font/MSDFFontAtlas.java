package dev.prestigeclient.renderlib.api.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.example.UIExamples;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.InternalShaders;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.ui.RectInstancer;
import dev.prestigeclient.renderlib.impl.render.ui.UiRenderer;
import dev.prestigeclient.renderlib.impl.render.ui.built.DefaultPreBuiltVertices;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20;

public class MSDFFontAtlas {
    private static final String FORMATTING_PALETTE = "0123456789abcdefklmnor";
    private static final int[][] FORMATTING_COLOR_PALETTE = new int[32][3];
    private final int[] textColor = new int[3];
    private float textX;
    private float distanceRange;
    private int atlasWidth;
    private int atlasHeight;
    private float size = 11.0F;
    private final MSDFGlyph[] glyphs = new MSDFGlyph[4194304];
    private MSDFFontMetrics fontMetrics;
    private GlTexture texture;
    public Function<MSDFFontAtlas, Vector4f> scissorProvider;
    public BiFunction<MSDFFontAtlas, StateApplier[], StateApplier[]> fontRenderLayerModifier;
    public Function<MSDFFontAtlas, UiRenderer> fontToUIRenderer = msdffontatlas1 -> UIExamples.getRenderer();
    private final MSDFFontAtlas.SimpleUvRectExtension uvScratch = new MSDFFontAtlas.SimpleUvRectExtension(new Vector2f(), new Vector2f());
    private RectInstancer runPage;
    private UiRenderer runRenderer;
    private Vector4f runScissor;
    private final RectInstancer dummyFontRI = new RectInstancer(
        null, DefaultPreBuiltVertices.SQUARE_TEX.getVAOIndexMesh(), (object, list) -> {
            MSDFFontAtlas.SimpleUvRectExtension msdffontatlas$simpleuvrectextension = (MSDFFontAtlas.SimpleUvRectExtension)object;
            list.get(0).pad(msdffontatlas$simpleuvrectextension.uvOffset);
            list.get(1).pad(msdffontatlas$simpleuvrectextension.uvScale);
        }, new String[]{"UvOffset", "UvScale"}
    );
    private final Map<MSDFFontAtlas.StatesKey, List<RectInstancer>> fontLayerCache = new HashMap<>();

    public MSDFFontAtlas(String s, String s1) throws IOException {
        RLPath rlpath = RLPath.of(s, "fonts/" + s1 + ".json");
        RLPath rlpath1 = RLPath.of(s, "fonts/" + s1 + ".png");

        try (
            InputStream inputstream = rlpath.openStream();
            InputStream inputstream1 = rlpath1.openStream();
        ) {
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            this.init(inputstreamreader, inputstream1);
        }
    }

    public MSDFFontAtlas(Reader reader, InputStream inputstream) throws IOException {
        this.init(reader, inputstream);
    }

    static {
        for (int i = 0; i < 32; i++) {
            int j1 = i >> 3;
            j1 &= 1;
            int j = j1 * 85;
            j1 = i >> 2;
            j1 &= 1;
            j1 *= 170;
            int k = j1 + j;
            j1 = i >> 1;
            j1 &= 1;
            j1 *= 170;
            int l = j1 + j;
            j1 = i & 1;
            j1 *= 170;
            int i1 = j1 + j;
            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            FORMATTING_COLOR_PALETTE[i][0] = k;
            FORMATTING_COLOR_PALETTE[i][1] = l;
            FORMATTING_COLOR_PALETTE[i][2] = i1;
        }
    }

    private void init(Reader reader, InputStream inputstream) throws IOException {
        JsonObject jsonobject = JsonParser.parseReader(reader).getAsJsonObject();
        if (!"msdf".equals(jsonobject.getAsJsonObject("atlas").get("type").getAsString())) {
            throw new RuntimeException("not msdf atlas");
        }

        this.atlasWidth = jsonobject.getAsJsonObject("atlas").get("width").getAsInt();
        this.atlasHeight = jsonobject.getAsJsonObject("atlas").get("height").getAsInt();
        this.distanceRange = jsonobject.getAsJsonObject("atlas").get("distanceRange").getAsFloat();
        this.fontMetrics = MSDFFontMetrics.parse(jsonobject.getAsJsonObject("metrics"));
        NativeImage nativeimage = NativeImage.read(inputstream);
        this.texture = GlTexture.create(3553);
        this.texture.init(32856, nativeimage.getWidth(), nativeimage.getHeight());
        int i = nativeimage.getWidth();
        int j = nativeimage.getHeight();
        ByteBuffer bytebuffer = BufferUtils.createByteBuffer(i * j * 4);

        for (int k = 0; k < j; k++) {
            for (int l = 0; l < i; l++) {
                int i1 = nativeimage.getColorArgb(l, k);
                bytebuffer.put((byte)(i1 & 0xFF));
                int j1 = i1 >> 8;
                bytebuffer.put((byte)(j1 & 0xFF));
                j1 = i1 >> 16;
                bytebuffer.put((byte)(j1 & 0xFF));
                j1 = i1 >> 24;
                bytebuffer.put((byte)(j1 & 0xFF));
            }
        }

        bytebuffer.flip();
        GL20.glPixelStorei(3317, 1);
        GL20.glPixelStorei(3314, 0);
        GL20.glPixelStorei(3316, 0);
        GL20.glPixelStorei(3315, 0);
        this.texture.setData(0, 0, i, j, 6408, 5121, bytebuffer);
        GL20.glPixelStorei(3317, 4);
        this.texture.setFilter(9729);
        this.texture.setEdgeMode(33071);
        nativeimage.close();

        for (JsonElement jsonelement : jsonobject.getAsJsonArray("glyphs")) {
            JsonObject jsonobject1 = jsonelement.getAsJsonObject();
            MSDFGlyph msdfglyph = MSDFGlyph.parse(jsonobject1);
            this.glyphs[msdfglyph.getUnicode()] = msdfglyph;
        }
    }

    public float getSize() {
        return this.size;
    }

    public void cleanup() {
        if (this.texture != null) {
            this.texture.close();
        }
    }

    public void setSize(float f) {
        this.size = f;
    }

    public String truncate(String s, float f, float f1) {
        if (this.getWidth(s, f1) <= f) {
            return s;
        }

        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            if (!(this.getWidth(stringbuilder.toString(), f1) < f)) {
                stringbuilder.append("...");
                break;
            }

            stringbuilder.append(s.charAt(i));
        }

        return stringbuilder.toString();
    }

    public void render(String s, float f, float f1, int i) {
        this.render(s, f, f1, this.size, i);
    }

    public void render(String s, float f, float f1, Color color) {
        this.render(s, f, f1, this.size, color.getRGB());
    }

    public void render(String s, float f, float f1, float f2, int i) {
        RenderSystem.getModelViewStack();
        this.beginRun();
        int l1 = i >>> 24;
        int j = l1 & 0xFF;
        l1 = i >>> 16;
        int k = l1 & 0xFF;
        l1 = i >>> 8;
        int l = l1 & 0xFF;
        int i1 = i & 0xFF;
        this.textColor[0] = k;
        this.textColor[1] = l;
        this.textColor[2] = i1;

        for (int j1 = 0; j1 < s.length(); j1++) {
            int k1 = s.codePointAt(j1);
            if (k1 == 167 && j1 + 1 < s.length()) {
                int i2 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(s.charAt(j1 + 1)));
                if (i2 >= 0 && i2 < 16) {
                    System.arraycopy(FORMATTING_COLOR_PALETTE[i2], 0, this.textColor, 0, 3);
                } else if (i2 == 21) {
                    this.textColor[0] = k;
                    this.textColor[1] = l;
                    this.textColor[2] = i1;
                }

                j1++;
            } else {
                MSDFGlyph msdfglyph = this.glyphs[k1];
                if (msdfglyph != null) {
                    f += this.renderGlyph(msdfglyph, f, f1, f2, j);
                }
            }
        }
    }

    public void render(OrderedText orderedtext, float f, float f1, float f2, int i) {
        RenderSystem.getModelViewStack();
        this.beginRun();
        int j1 = i >>> 24;
        int j = j1 & 0xFF;
        int k = i >>> 16 & 0xFF;
        int l = i >>> 8 & 0xFF;
        int i1 = i & 0xFF;
        this.textColor[0] = k;
        this.textColor[1] = l;
        this.textColor[2] = i1;
        this.textX = f;
        orderedtext.accept((j3, style, k2) -> {
            MSDFGlyph msdfglyph = this.glyphs[k2];
            if (msdfglyph == null) {
                return true;
            }

            if (style.getColor() != null) {
                int l2 = style.getColor().getRgb();
                int i3 = l2 >>> 16;
                this.textColor[0] = i3 & 0xFF;
                i3 = l2 >>> 8;
                this.textColor[1] = i3 & 0xFF;
                this.textColor[2] = l2 & 0xFF;
            } else {
                this.textColor[0] = k;
                this.textColor[1] = l;
                this.textColor[2] = i1;
            }

            this.textX = this.textX + this.renderGlyph(msdfglyph, this.textX, f1, f2, j);
            return true;
        });
    }

    private float smoothstep(float f) {
        f = Math.max(0.0F, Math.min(1.0F, f));
        return f * f * (3.0F - 2.0F * f);
    }

    private RectInstancer runLayer() {
        RectInstancer rectinstancer = this.runPage;
        if (rectinstancer == null || rectinstancer.rectCount.get() >= 1024) {
            RectInstancer rectinstancer1 = this.getMSDFRenderLayer();
            MSDFFontAtlas msdffontatlas1 = this;
            msdffontatlas1.runPage = rectinstancer1;
            rectinstancer = rectinstancer1;
        }

        return rectinstancer;
    }

    private void beginRun() {
        this.runRenderer = this.fontToUIRenderer.apply(this);
        this.runScissor = this.scissorProvider == null ? null : this.scissorProvider.apply(this);
        this.runPage = this.getMSDFRenderLayer();
    }

    private int interpolateColorRGB(Color color, Color color1, float f) {
        int i = color.getRed();
        int j = color.getGreen();
        int k = color.getBlue();
        int l = color.getAlpha();
        int i1 = color1.getRed();
        int j1 = color1.getGreen();
        int k1 = color1.getBlue();
        int l1 = (int)(i + (i1 - i) * f);
        int i2 = (int)(j + (j1 - j) * f);
        int j2 = (int)(k + (k1 - k) * f);
        int i3 = l << 24;
        int l2 = l1 << 16;
        int k2 = i3;
        i3 = k2 | l2;
        l2 = i2 << 8;
        k2 = i3;
        k2 |= l2;
        return k2 | j2;
    }

    public void renderVerticalGradient(String s, float f, float f1, Color color, Color color1, int i, float f2) {
        this.renderVerticalGradient(s, f, f1, this.size, color, color1, i, f2);
    }

    public void renderVerticalGradient(String s, float f, float f1, float f2, Color color, Color color1, int i, float f3) {
        RenderSystem.getModelViewStack();
        this.beginRun();
        float f4 = 0.0F;
        long j = System.currentTimeMillis();

        for (int k = 0; k < s.length(); k++) {
            int l = s.codePointAt(k);
            if (l == 167 && k + 1 < s.length()) {
                k++;
            } else {
                MSDFGlyph msdfglyph = this.glyphs[l];
                if (msdfglyph != null) {
                    float f5 = ((float)(j % i) / i + f3 * (f1 / 100.0F)) % 1.0F;
                    int i1 = this.interpolateColor(color, color1, f5);
                    int j1 = i1 >>> 24 & 0xFF;
                    this.textColor[0] = i1 >>> 16 & 0xFF;
                    this.textColor[1] = i1 >>> 8 & 0xFF;
                    this.textColor[2] = i1 & 0xFF;
                    f4 += this.renderGlyph(msdfglyph, f + f4, f1, f2, j1);
                }
            }
        }
    }

    public void renderHorizontalGradient(String s, float f, float f1, Color color, Color color1, int i) {
        this.renderHorizontalGradient(s, f, f1, this.size, color, color1, i);
    }

    public void renderHorizontalGradient(String s, float f, float f1, float f2, Color color, Color color1, int i) {
        RenderSystem.getModelViewStack();
        this.beginRun();
        float f3 = this.getWidth(s, f2);
        float f4 = 0.0F;
        long j = System.currentTimeMillis();

        for (int k = 0; k < s.length(); k++) {
            int l = s.codePointAt(k);
            if (l == 167 && k + 1 < s.length()) {
                k++;
            } else {
                MSDFGlyph msdfglyph = this.glyphs[l];
                if (msdfglyph != null) {
                    float f5 = (f4 / f3 + (float)(j % i) / i) % 1.0F;
                    int i1 = this.interpolateColor(color, color1, f5);
                    int k1 = i1 >>> 24;
                    int j1 = k1 & 0xFF;
                    k1 = i1 >>> 16;
                    this.textColor[0] = k1 & 0xFF;
                    this.textColor[1] = i1 >>> 8 & 0xFF;
                    this.textColor[2] = i1 & 0xFF;
                    f4 += this.renderGlyph(msdfglyph, f + f4, f1, f2, j1);
                }
            }
        }
    }

    public void renderCenteredString(String s, float f, float f1, float f2, int i) {
        this.render(s, f - this.getWidth(s, f2) / 2.0F, f1, f2, i);
    }

    public void renderCenteredString(String s, float f, float f1, int i) {
        this.render(s, f - this.getWidth(s) / 2.0F, f1, this.size, i);
    }

    public float getWidth(String s, float f) {
        float f1 = 0.0F;

        for (int i = 0; i < s.length(); i++) {
            int j = s.codePointAt(i);
            if (j == 167 && i + 1 < s.length()) {
                i++;
            } else {
                MSDFGlyph msdfglyph = this.glyphs[j];
                if (msdfglyph != null) {
                    f1 += f * msdfglyph.getAdvance();
                }
            }
        }

        return f1;
    }

    public float getWidth(String s) {
        return this.getWidth(s, this.size);
    }

    public float getWidth(Text text) {
        return this.getWidth(text, this.size);
    }

    public float getWidth(Text text, float f) {
        return this.getWidth(text.asOrderedText(), f);
    }

    public float getWidth(OrderedText orderedtext) {
        return this.getWidth(orderedtext, this.size);
    }

    public float getWidth(OrderedText orderedtext, float f) {
        float[] afloat = new float[1];
        orderedtext.accept((j, style, i) -> {
            MSDFGlyph msdfglyph = this.glyphs[i];
            if (msdfglyph == null) {
                return true;
            }

            if (msdfglyph.getPlaneRight() - msdfglyph.getPlaneLeft() != 0.0F) {
                afloat[0] += f * msdfglyph.getAdvance();
            }

            return true;
        });
        return afloat[0];
    }

    public void renderWithShadow(String s, float f, float f1, float f2, int i) {
        int k = i & 0xFF000000;
        int j = k | 4144959;
        this.render(s, f + 0.75F, f1 + 0.75F, f2, j);
        this.render(s, f, f1, f2, i);
    }

    public void renderWithShadow(String s, float f, float f1, int i) {
        this.renderWithShadow(s, f, f1, this.size, i);
    }

    private float renderGlyph(MSDFGlyph msdfglyph, float f, float f1, float f2, int i) {
        if (msdfglyph.getPlaneRight() - msdfglyph.getPlaneLeft() != 0.0F) {
            UiRenderer uirenderer = this.runRenderer != null ? this.runRenderer : this.fontToUIRenderer.apply(this);
            float f3 = f + msdfglyph.getPlaneLeft() * f2;
            float f4 = f + msdfglyph.getPlaneRight() * f2;
            float f5 = f1 + this.fontMetrics.getAscender() * f2 - msdfglyph.getPlaneTop() * f2;
            float f6 = f1 + this.fontMetrics.getAscender() * f2 - msdfglyph.getPlaneBottom() * f2;
            float f7 = msdfglyph.getAtlasLeft() / this.atlasWidth;
            float f8 = msdfglyph.getAtlasRight() / this.atlasWidth;
            float f9 = 1.0F - msdfglyph.getAtlasTop() / this.atlasHeight;
            float f10 = 1.0F - msdfglyph.getAtlasBottom() / this.atlasHeight;
            int i1 = i << 24;
            int l = this.textColor[0] << 16;
            int k = i1;
            i1 = k | l;
            l = this.textColor[1] << 8;
            k = i1;
            i1 = k | l;
            l = this.textColor[2];
            k = i1;
            int j = k | l;
            this.uvScratch.uvOffset().set(f7, f9);
            this.uvScratch.uvScale().set(f8 - f7, f10 - f9);
            uirenderer.addRectangle(
                this.runLayer(),
                f3,
                f5,
                f4 - f3,
                f6 - f5,
                j,
                this.runRenderer != null ? this.runScissor : (this.scissorProvider == null ? null : this.scissorProvider.apply(this)),
                this.uvScratch
            );
        }

        return f2 * msdfglyph.getAdvance();
    }

    private int interpolateColor(Color color, Color color1, float f) {
        float f1 = color.getRed();
        int i2 = color1.getRed();
        int j1 = color.getRed();
        int i1 = i2;
        int i = (int)(f1 + (i1 - j1) * f);
        f1 = color.getGreen();
        i2 = color1.getGreen();
        j1 = color.getGreen();
        i1 = i2;
        int j = (int)(f1 + (i1 - j1) * f);
        f1 = color.getBlue();
        i2 = color1.getBlue();
        j1 = color.getBlue();
        i1 = i2;
        int k = (int)(f1 + (i1 - j1) * f);
        f1 = color.getAlpha();
        i2 = color1.getAlpha();
        j1 = color.getAlpha();
        i1 = i2;
        int l = (int)(f1 + (i1 - j1) * f);
        int k1 = l << 24;
        j1 = i << 16;
        i1 = k1;
        int l1 = i1 | j1;
        j1 = j << 8;
        i1 = l1;
        i1 |= j1;
        return i1 | k;
    }

    public void renderRightString(String s, float f, float f1, float f2, int i) {
        this.render(s, f - this.getWidth(s, f2), f1, f2, i);
    }

    public void renderRightString(String s, float f, float f1, int i) {
        this.render(s, f - this.getWidth(s), f1, this.size, i);
    }

    public RectInstancer getMSDFRenderLayer() {
        StateApplier[] astateapplier = new StateApplier[]{
            StateApplier.framebufferState(RenderAPI.getMinecraftFramebuffer()),
            StateApplier.shaderState(InternalShaders.MSDF),
            StateApplier.flagState(2884, false, true),
            StateApplier.flagState(2929, true, false),
            StateApplier.scissorState(false, false, 0, 0, 0, 0),
            StateApplier.depthFuncState(515, 515),
            StateApplier.blendState(true, true),
            new MSDFFontAtlas.TextureAndUniformSetter(this.texture, this.distanceRange)
        };
        if (this.fontRenderLayerModifier != null) {
            astateapplier = this.fontRenderLayerModifier.apply(this, astateapplier);
        }

        List<RectInstancer> list = this.fontLayerCache.computeIfAbsent(new MSDFFontAtlas.StatesKey(astateapplier), stateskey -> new ArrayList<>());

        for (RectInstancer rectinstancer : list) {
            if (rectinstancer.rectCount.get() < 1024) {
                return rectinstancer;
            }
        }

        RectInstancer rectinstancer1 = this.dummyFontRI.copyWithLayer(new RenderLayer(VertexFormats.POSITION_UV, 4, false, astateapplier));
        list.add(rectinstancer1);
        return rectinstancer1;
    }

    public float getLineHeight(float f) {
        return this.fontMetrics.getLineHeight() * f;
    }

    public float getLineHeight() {
        return this.getLineHeight(this.size);
    }

    public void renderDiagonalGradient(String s, float f, float f1, float f2, Color color, Color color1, int i, float f3) {
        RenderSystem.getModelViewStack();
        this.beginRun();
        float f4 = this.getWidth(s, f2);
        float f5 = 0.0F;
        long j = System.currentTimeMillis();

        for (int k = 0; k < s.length(); k++) {
            int l = s.codePointAt(k);
            if (l == 167 && k + 1 < s.length()) {
                k++;
            } else {
                MSDFGlyph msdfglyph = this.glyphs[l];
                if (msdfglyph != null) {
                    float f6 = f4 > 0.0F ? f5 / f4 : 0.0F;
                    float f7 = f1 / 1000.0F * f3;
                    float f8 = (float)(j % i) / i;
                    float f9 = (f6 + f7 + f8) % 1.0F;
                    f9 = this.smoothstep(f9);
                    int i1 = this.interpolateColorRGB(color, color1, f9);
                    int k1 = i1 >>> 24;
                    int j1 = k1 & 0xFF;
                    k1 = i1 >>> 16;
                    this.textColor[0] = k1 & 0xFF;
                    this.textColor[1] = i1 >>> 8 & 0xFF;
                    this.textColor[2] = i1 & 0xFF;
                    f5 += this.renderGlyph(msdfglyph, f + f5, f1, f2, j1);
                }
            }
        }
    }

    public void renderDiagonalGradient(String s, float f, float f1, Color color, Color color1, int i, float f2) {
        this.renderDiagonalGradient(s, f, f1, this.size, color, color1, i, f2);
    }

    public void renderDiagonalGradientWithShadow(String s, float f, float f1, float f2, Color color, Color color1, int i, float f3) {
        Color color2 = new Color(color.getRed() / 4, color.getGreen() / 4, color.getBlue() / 4, color.getAlpha());
        Color color3 = new Color(color1.getRed() / 4, color1.getGreen() / 4, color1.getBlue() / 4, color1.getAlpha());
        this.renderDiagonalGradient(s, f + 0.75F, f1 + 0.75F, f2, color2, color3, i, f3);
        this.renderDiagonalGradient(s, f, f1, f2, color, color1, i, f3);
    }

    public void renderHorizontalGradientWithShadow(String s, float f, float f1, float f2, Color color, Color color1, int i) {
        Color color2 = new Color(color.getRed() / 4, color.getGreen() / 4, color.getBlue() / 4, color.getAlpha());
        Color color3 = new Color(color1.getRed() / 4, color1.getGreen() / 4, color1.getBlue() / 4, color1.getAlpha());
        this.renderHorizontalGradient(s, f + 0.75F, f1 + 0.75F, f2, color2, color3, i);
        this.renderHorizontalGradient(s, f, f1, f2, color, color1, i);
    }

    public void renderVerticalGradientWithShadow(String s, float f, float f1, float f2, Color color, Color color1, int i, float f3) {
        Color color2 = new Color(color.getRed() / 4, color.getGreen() / 4, color.getBlue() / 4, color.getAlpha());
        Color color3 = new Color(color1.getRed() / 4, color1.getGreen() / 4, color1.getBlue() / 4, color1.getAlpha());
        this.renderVerticalGradient(s, f + 0.75F, f1 + 0.75F, f2, color2, color3, i, f3);
        this.renderVerticalGradient(s, f, f1, f2, color, color1, i, f3);
    }

    public void renderWithEffect(String s, float f, float f1, RLFontEffect rlfonteffect) {
        this.renderWithEffect(s, f, f1, this.size, rlfonteffect);
    }

    public void renderWithEffect(String s, float f, float f1, float f2, RLFontEffect rlfonteffect) {
        RenderSystem.getModelViewStack();
        this.beginRun();
        float f3 = 0.0F;
        long i = System.currentTimeMillis();
        double d0 = i / 1000.0;

        for (int j = 0; j < s.length(); j++) {
            int k = s.codePointAt(j);
            if (k == 167 && j + 1 < s.length()) {
                j++;
            } else {
                MSDFGlyph msdfglyph = this.glyphs[k];
                if (msdfglyph != null) {
                    int l = rlfonteffect.getColorFor(j, d0);
                    int j1 = l >>> 24;
                    int i1 = j1 & 0xFF;
                    j1 = l >>> 16;
                    this.textColor[0] = j1 & 0xFF;
                    this.textColor[1] = l >>> 8 & 0xFF;
                    this.textColor[2] = l & 0xFF;
                    f3 += this.renderGlyph(msdfglyph, f + f3, f1, f2, i1);
                }
            }
        }
    }

    public record SimpleUvRectExtension(Vector2f uvOffset, Vector2f uvScale) {
    }

    public record StatesKey(StateApplier[] states) {
        @Override
        public boolean equals(Object object) {
            return object instanceof MSDFFontAtlas.StatesKey msdffontatlas$stateskey1 && Arrays.equals(this.states, msdffontatlas$stateskey1.states);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.states);
        }
    }

    public record TextureAndUniformSetter(GlTexture texture, float distanceRange) implements StateApplier {
        @Override
        public void apply(RenderPass renderpass) {
            GL13C.glActiveTexture(33984);
            GL20.glBindTexture(3553, this.texture.tex);
            RLUniforms rluniforms = RLUniforms.get(renderpass.shaderProgram.glId());
            rluniforms.set1i("msdfTexture", 0);
            rluniforms.set1f("pxRange", this.distanceRange);
        }
    }
}
