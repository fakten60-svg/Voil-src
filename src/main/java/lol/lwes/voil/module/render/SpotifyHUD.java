package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import lol.lwes.media.LyricLine;
import lol.lwes.media.LyricsController;
import lol.lwes.media.MediaManager;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.gui.NotificationManager;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.WipeFontEffect;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.media.MediaController;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.TextureRectangleRenderer;
import lol.lwes.voil.util.render.UiAnimation;

public class SpotifyHUD extends Module {
    public NumberSetting imageRoundness;
    public BooleanSetting showLyrics;
    public ColorSetting lyricsColor;
    public ColorSetting lyricsHighlightColor;
    public ModeSetting soundWaveMode;
    public ColorSetting soundWaveColor;
    public BooleanSetting lyricsPop;
    public NumberSetting popHeight;
    private final LyricsController mediaClient;
    private final WipeFontEffect waveEffect;
    private final UiAnimation springAnim;
    private final UiAnimation smoothAnimA;
    private final UiAnimation smoothAnimB;
    private final UiAnimation smoothAnimC;
    private final UiAnimation springAnimB;
    private String currentTrackId;
    private boolean trackChanged;
    private String currentLyricLine;
    private Color coverColor1;
    private Color coverColor2;
    private Color coverColor3;
    private Color coverColor4;
    private String lastLyric;
    private final Map<Integer, Long> wordCache;

    public SpotifyHUD() {
        super("SpotifyHUD", "Displays the current Spotify track on your screen", Category.Render);
        this.imageRoundness = new NumberSetting("Image Roundness", 0.0, 16.0, 15.0, 1.0, "Sets the roundness of the album art image");
        this.showLyrics = new BooleanSetting("Show Lyrics", false, "Shows the current track's lyrics");
        Color color3 = new Color(180, 180, 180);
        String s2 = "Sets the color of the next lyrics line";
        Color color = color3;
        this.lyricsColor = new ColorSetting("Lyrics Color", color, s2);
        String s3 = "Sets the color of the highlighted portion of the lyrics";
        Color color1 = Color.white;
        this.lyricsHighlightColor = new ColorSetting("Lyrics Highlight Color", color1, s3);
        this.soundWaveMode = new ModeSetting(
            "Sound Wave Mode", "Custom", "Dynamic mode animates the sound wave bars based on the music", new String[]{"Sync with cover", "Custom"}
        );
        color3 = new Color(30, 185, 84);
        String s4 = "Sets the color of the sound wave bars";
        Color color2 = color3;
        this.soundWaveColor = new ColorSetting("Sound Wave Color", color2, s4);
        this.lyricsPop = new BooleanSetting("Lyrics Pop", true, "Makes each word jump up as it's sung");
        this.popHeight = new NumberSetting("Pop Height", 0.0, 12.0, 4.5, 0.5, "How far the word lifts");
        this.mediaClient = new LyricsController();
        this.waveEffect = new WipeFontEffect(Color.WHITE, new Color(180, 180, 180));
        this.springAnim = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.65, 0.18);
        this.smoothAnimA = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.75, 0.15);
        this.smoothAnimB = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.8, 0.12);
        this.smoothAnimC = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.75, 0.15);
        this.springAnimB = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.45, 0.25);
        this.currentTrackId = "";
        this.trackChanged = false;
        this.currentLyricLine = "";
        this.coverColor1 = new Color(30, 185, 84);
        this.coverColor2 = new Color(30, 185, 84);
        this.coverColor3 = new Color(30, 185, 84);
        this.coverColor4 = new Color(30, 185, 84);
        this.lastLyric = "";
        this.wordCache = new HashMap<>();
        Setting[] asetting = new Setting[]{
            this.imageRoundness,
            this.showLyrics,
            this.lyricsColor,
            this.lyricsHighlightColor,
            this.lyricsPop,
            this.popHeight,
            this.soundWaveMode,
            this.soundWaveColor
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.lyricsColor, this.showLyrics);
        Setting.dependSetting(this.lyricsHighlightColor, this.showLyrics);
        Setting.dependSetting(this.lyricsPop, this.showLyrics);
        Setting.dependSetting(this.popHeight, () -> this.showLyrics.isEnabled() && this.lyricsPop.isEnabled());
    }

    private static String truncate(String s, float f) {
        MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
        return truncate(msdffontatlas, 0.0F, s, f);
    }

    private void drawLyricText(String s, float f, float f1, int i, int j, float f2) {
        if (!this.lyricsPop.isEnabled()) {
            this.waveEffect.setOffset(i);
            VoilClient.fonts.getSelectedFont().renderWithEffect(s, f, f1, this.waveEffect);
        } else {
            float f3 = f2 * j;
            float f4 = VoilClient.fonts.getSelectedFont().getWidth(" ");
            int k = 0;
            long l = System.currentTimeMillis();
            float f5 = this.popHeight.getFloat();

            while (k <= s.length()) {
                int i1 = s.indexOf(32, k);
                if (i1 < 0) {
                    i1 = s.length();
                }

                String s1 = s.substring(k, i1);
                float f6 = VoilClient.fonts.getSelectedFont().getWidth(s1);
                int j1 = i + k;
                float f7 = 0.0F;
                if (!s1.isEmpty() && f3 >= j1) {
                    Long olong = this.wordCache.computeIfAbsent(j1, integer -> f3 > integer.intValue() + 2.0F ? 0L : l);
                    if (olong > 0L) {
                        float f8 = (float)(l - olong);
                        float f9;
                        if (f8 < 350.0F) {
                            float f10 = f8 / 350.0F;
                            float f11 = 1.0F + 2.7F * (float)Math.pow(f10 - 1.0F, 3.0) + 1.7F * (float)Math.pow(f10 - 1.0F, 2.0);
                            f9 = Math.max(0.0F, f11);
                        } else if (f8 < 350.0F + 150.0F) {
                            f9 = 1.0F;
                        } else {
                            float f12 = (f8 - 350.0F - 150.0F) / 360.0F;
                            f9 = f12 >= 1.0F ? 0.0F : (float)Math.pow(1.0F - f12, 3.0);
                        }

                        f7 = f9 * f5;
                    }
                }

                if (!s1.isEmpty()) {
                    this.waveEffect.setOffset(j1);
                    VoilClient.fonts.getSelectedFont().renderWithEffect(s1, f, f1 - f7, this.waveEffect);
                }

                f += f6 + f4;
                k = i1 + 1;
            }
        }
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame()) {
            MediaController mediacontroller = VoilClient.getMediaController();
            if (!mediacontroller.update()) {
                this.setEnabled(false);
            }
        }
    }

    @Override
    public void onRender2D() {
        if (!Module.isNotInGame() && !VoilClient.getMediaController().isIdle()) {
            MediaController mediacontroller = VoilClient.getMediaController();
            if (!mediacontroller.isIdle()) {
                int i = VoilClient.getHudLayout().getX(this);
                int j = VoilClient.getHudLayout().getY(this);
                if (this.showLyrics.isEnabled()) {
                    LyricsController lyricscontroller = this.mediaClient;
                    String s11 = mediacontroller.getArtist();
                    String s9 = mediacontroller.getTitle();
                    String s10 = s11;
                    lyricscontroller.update(s10, s9);
                    LyricLine lyricline = this.mediaClient.getCurrentLine();
                    String s = lyricline != null ? lyricline.text() : "";
                    if (!s.equals(this.currentLyricLine)) {
                        this.currentLyricLine = s;
                        this.wordCache.clear();
                        this.springAnim.snapTo(4.0).setTarget(0.0);
                        this.smoothAnimA.snapTo(0.0).setTarget(1.0);
                        this.smoothAnimB.snapTo(0.0).setTarget(0.6);
                    }

                    this.springAnim.update();
                    this.smoothAnimA.update();
                    this.smoothAnimB.update();
                }

                float f48 = 75.0F;
                float f49 = 0.0F;
                List<String> list = List.of();
                List<String> list1 = List.of();
                if (this.showLyrics.isEnabled() && this.mediaClient.hasLyrics()) {
                    LyricLine lyricline1 = this.mediaClient.getCurrentLine();
                    LyricLine lyricline2 = this.mediaClient.getNextLine();
                    if (lyricline1 != null) {
                        list = wrapLines(lyricline1.text(), 187.0F);
                    }

                    if (lyricline2 != null) {
                        list1 = wrapLines(lyricline2.text(), 187.0F);
                    }

                    int l3 = list.size();
                    int i2 = list1.size();
                    int l1 = l3;
                    int k = l1 + i2;
                    l1 = k * 14;
                    f49 = l1 + 6;
                }

                float f50 = f48 + (mediacontroller.isPlaying() ? f49 : 0.0F);
                double d1 = f50;
                this.smoothAnimC.setTarget(d1);
                this.smoothAnimC.update();
                float f51 = this.smoothAnimC.getFloat();
                float f42 = i;
                float f43 = j;
                float f44 = f42;
                Render2DUtil.drawBlurRectEx(f44, f43, 195.0F, f51, 14.0F, 24.0F, 1.5F);
                int k2 = i + 195;
                int k3 = (int)f51;
                int j3 = j;
                RenderManager.setScissor(i, j, k2, j3 + k3);
                String s13 = MediaManager.getProvider().getSourceAppId();
                String s1;
                if (s13 != null && !s13.isEmpty()) {
                    String s2 = s13.toLowerCase();
                    if (s2.contains("youtube_music") || s2.contains("youtubemusic")) {
                        s1 = IconGlyphs.iconLetterD();
                    } else if (s2.contains("chrome")) {
                        s1 = IconGlyphs.iconLetterB();
                    } else if (s2.contains("spotify")) {
                        s1 = IconGlyphs.iconLetterC();
                    } else if (s2.contains("soundcloud")) {
                        s1 = IconGlyphs.iconLetterA();
                    } else {
                        s1 = "A";
                    }
                } else {
                    s1 = "A";
                }

                Color color16;
                if (s1.equals(IconGlyphs.iconLetterB()) || s1.equals(IconGlyphs.iconLetterD())) {
                    color16 = new Color(255, 0, 0, 200);
                } else if (s1.equals(IconGlyphs.iconLetterC())) {
                    color16 = new Color(30, 185, 84, 200);
                } else if (s1.equals(IconGlyphs.iconLetterA())) {
                    color16 = new Color(255, 120, 0, 200);
                } else {
                    color16 = new Color(255, 255, 255, 180);
                }

                VoilClient.fonts.getMedia().render(s1, i + 168, j + 2, 22.0F, color16.getRGB());
                String s18 = mediacontroller.getTitle();
                String s7 = mediacontroller.getArtist();
                String s6 = s18;
                String s3 = s6 + "|" + s7;
                if (!s3.equals(this.currentTrackId)) {
                    if (!this.currentTrackId.isEmpty()) {
                        this.trackChanged = true;
                        this.springAnimB.snapTo(0.0).setTarget(1.0);
                    }

                    this.currentTrackId = s3;
                }

                this.springAnimB.update();
                float f = i + 8;
                float f1 = j + 8;
                float f2 = this.springAnimB.getFloat();
                if (this.trackChanged && mediacontroller.getPreviousCover() != null && f2 < 0.99F) {
                    float f3 = f - f2 * (55.0F + 8.0F);
                    float f4 = 1.0F - f2;
                    TextureRectangleRenderer texturerectanglerenderer1 = Render2DUtil.textureRenderer;
                    GlTexture gltexture3 = mediacontroller.getPreviousCover();
                    float f64 = this.imageRoundness.getFloat();
                    Color color3 = new Color(1.0F, 1.0F, 1.0F, f4);
                    float f24 = 0.7F;
                    float f25 = f64;
                    float f26 = 55.0F;
                    float f27 = 55.0F;
                    float f28 = f1;
                    float f29 = f3;
                    GlTexture gltexture = gltexture3;
                    texturerectanglerenderer1.render(gltexture, f29, f28, f27, f26, f25, f24, color3);
                    float f5 = f + (1.0F - f2) * (55.0F + 8.0F);
                    TextureRectangleRenderer texturerectanglerenderer2 = Render2DUtil.textureRenderer;
                    gltexture3 = mediacontroller.getCover();
                    f64 = this.imageRoundness.getFloat();
                    Color color4 = new Color(1.0F, 1.0F, 1.0F, f2);
                    float f30 = 0.7F;
                    float f31 = f64;
                    float f32 = 55.0F;
                    float f33 = 55.0F;
                    float f34 = f1;
                    float f35 = f5;
                    GlTexture gltexture1 = gltexture3;
                    texturerectanglerenderer2.render(gltexture1, f35, f34, f33, f32, f31, f30, color4);
                } else {
                    this.trackChanged = false;
                    TextureRectangleRenderer texturerectanglerenderer = Render2DUtil.textureRenderer;
                    GlTexture gltexture4 = mediacontroller.getCover();
                    float f66 = this.imageRoundness.getFloat();
                    Color color5 = Color.WHITE;
                    float f36 = 0.7F;
                    float f37 = f66;
                    float f38 = 55.0F;
                    float f39 = 55.0F;
                    float f40 = f1;
                    float f41 = f;
                    GlTexture gltexture2 = gltexture4;
                    texturerectanglerenderer.render(gltexture2, f41, f40, f39, f38, f37, f36, color5);
                }

                String s14 = mediacontroller.getTitle() == null ? "" : mediacontroller.getTitle();
                String s15 = mediacontroller.getArtist() == null ? "" : mediacontroller.getArtist();
                float f52 = i + 68;
                float f6 = i + 168 - f52 - 4.0F;
                MSDFFontAtlas msdffontatlas1 = VoilClient.fonts.getSelectedFont();
                float f65 = j + 6;
                Color color8 = Color.gray;
                msdffontatlas1.render("now playing", f52, f65, 10.0F, ColorUtil.withAlpha(color8, 200).getRGB());
                MSDFFontAtlas msdffontatlas;
                (msdffontatlas = VoilClient.fonts.getGreyscale()).render(truncate(msdffontatlas, 13.0F, s14, f6), f52, j + 16, 13.0F, Color.white.getRGB());
                VoilClient.fonts.getSelectedFont().render(truncate(s15, 118.0F), f52, j + 29, Color.white);
                long l = mediacontroller.getDuration();
                long i1 = mediacontroller.getPosition();
                float f7 = 0.0F;
                if (l > 0L && i1 >= 0L) {
                    f7 = Math.max(0.0F, Math.min(1.0F, (float)i1 / (float)l));
                }

                float f8 = i + 68;
                float f9 = j + 45;
                Render2DUtil.drawRoundedRect(f8, f9, 118.0F, 4.0F, 4.0F / 2.0F, 1.0F, new Color(255, 255, 255, 60));
                Render2DUtil.drawRoundedRect(f8, f9, 118.0F * f7, 4.0F, 4.0F / 2.0F, 1.0F, Color.WHITE);
                String s4 = String.format("%d:%02d", i1 / 60L, i1 % 60L);
                VoilClient.fonts.getSelectedFont().render(s4, f8, f9 + 7.0F, Color.white);
                long j1 = l - i1;
                String s5 = String.format("-%d:%02d", j1 / 60L, j1 % 60L);
                VoilClient.fonts.getSelectedFont().render(s5, f8 + 118.0F - 25.0F, f9 + 7.0F, Color.white);
                double d0 = System.currentTimeMillis() / 1000.0;
                float f10 = f8 + VoilClient.fonts.getSelectedFont().getWidth(s4) + 4.0F;
                float f11 = f8 + 118.0F - 25.0F - 4.0F;
                float f12 = 25 * 1.5F + 24 * 1.0F;
                float f13 = f10 + (f11 - f10 - f12) / 2.0F;
                float f14 = f9 + 13.0F;
                Color color;
                if (this.soundWaveMode.is("Sync with cover")) {
                    this.renderLyricLine(mediacontroller);
                    color = null;
                } else {
                    color = this.soundWaveColor.getColor();
                }

                if (mediacontroller.isPlaying()) {
                    float[] afloat = MediaManager.getProvider().getSpectrum(25);

                    for (int k1 = 0; k1 < 25; k1++) {
                        float f15 = (float)k1 / 24;
                        float f16 = 1.0F - 2.0F * Math.abs(f15 - 0.5F);
                        f16 = (float)Math.pow(f16, 0.6);
                        float f17 = (float)Math.sin(d0 * 4.0 + k1 * 0.4) * 0.5F + 0.5F;
                        float f18 = (float)Math.sin(d0 * 5.7 + k1 * 0.7) * 0.3F + 0.5F;
                        float f19 = (float)Math.sin(d0 * 7.3 + k1 * 1.1) * 0.2F + 0.5F;
                        float f20 = (f17 + f18 + f19) / 3.0F;
                        float f21 = (float)Math.pow(afloat[k1], 0.6);
                        float f22 = f21 * 0.75F + f20 * 0.25F * (0.3F + f21 * 0.7F);
                        float f23 = (1.0F + f22 * 15.0F) * f16;
                        f23 = Math.max(1.0F, f23);
                        Color color18;
                        if (color != null) {
                            color18 = color;
                        } else {
                            Color color9 = this.coverColor2;
                            Color color10 = this.coverColor1;
                            color18 = ColorUtil.lerpColor(color10, color9, f15);
                        }

                        Color color1 = color18;
                        Color color19;
                        if (color != null) {
                            color19 = color;
                        } else {
                            Color color11 = this.coverColor4;
                            Color color12 = this.coverColor3;
                            color19 = ColorUtil.lerpColor(color12, color11, f15);
                        }

                        Color color2 = color19;
                        Render2DUtil.drawRectGradient(f13 + k1 * (1.5F + 1.0F), f14 - f23 / 2.0F, 1.5F, f23, 1.5F / 2.0F, 1.0F, color1, color1, color2, color2);
                    }
                } else {
                    float f53 = 12.0F;
                    float f55 = 3.0F;
                    float f57 = 3.0F;
                    float f59 = f55 * 2.0F + f57;
                    float f61 = f10 + (f11 - f10 - f59) / 2.0F;
                    float f62 = f14 - f53 / 2.0F + 2.0F;
                    Render2DUtil.drawRoundedRect(f61, f62, f55, f53, 0.5F, 1.0F, Color.WHITE);
                    Render2DUtil.drawRoundedRect(f61 + f55 + f57, f62, f55, f53, 0.5F, 1.0F, Color.WHITE);
                }

                float f54 = (f51 - f48) / Math.max(1.0F, f49);
                if (this.showLyrics.isEnabled() && this.mediaClient.hasLyrics() && !list.isEmpty() && f54 > 0.05F) {
                    float f56 = this.mediaClient.getLineProgress();
                    float f58 = j + f48 - 2.0F + this.springAnim.getFloat();
                    float f60 = this.smoothAnimA.getFloat() * f54;
                    LyricLine lyricline3 = this.mediaClient.getCurrentLine();
                    if (lyricline3 != null) {
                        int l2 = lyricline3.text().length();
                        WipeFontEffect wipefonteffect = this.waveEffect;
                        Color color13 = this.lyricsHighlightColor.getColor();
                        Color color21 = ColorUtil.withAlpha(color13, f60);
                        Color color22 = this.lyricsColor.getColor();
                        float f45 = f60 * 0.6F;
                        Color color14 = color22;
                        Color color6 = ColorUtil.withAlpha(color14, f45);
                        Color color7 = color21;
                        wipefonteffect.setColors(color7, color6).setLength(l2).setProgress(f56);
                        int i3 = 0;

                        for (String s16 : list) {
                            float f46 = i + 4;
                            String s12 = s16;
                            this.drawLyricText(s12, f46, f58, i3, l2, f56);
                            f58 += 14;
                            j3 = s16.length();
                            k3 = j3 + 1;
                            j3 = i3;
                            i3 = j3 + k3;
                        }
                    }

                    if (!list1.isEmpty()) {
                        float f63 = this.smoothAnimB.getFloat() * f54;
                        Color color20 = this.lyricsColor.getColor();
                        float f47 = f63 * 0.4F;
                        Color color15 = color20;
                        Color color17 = ColorUtil.withAlpha(color15, f47);

                        for (String s17 : list1) {
                            VoilClient.fonts.getSelectedFont().render(s17, i + 4, f58, color17);
                            f58 += 14;
                        }
                    }
                }

                RenderManager.disableScissor();
                HudLayout hudlayout = VoilClient.getHudLayout();
                int j2 = (int)f51;
                SpotifyHUD spotifyhud1 = this;
                hudlayout.setSize(spotifyhud1, 195, j2);
            }
        }
    }

    @Override
    public void onEnable() {
        if (SystemUtil.isLinux()) {
            NotificationManager notificationmanager = VoilClient.getNotifications();
            String s = "Spotify HUD is not supported on Linux systems.";
            String s1 = "Spotify HUD Disabled";
            notificationmanager.addNotification(s1, s);
            this.setEnabled(false);
        }
    }

    private static List<String> wrapLines(String s, float f) {
        ArrayList<String> arraylist = new ArrayList<>();
        if (s != null && !s.isEmpty()) {
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            if (msdffontatlas.getWidth(s) <= f) {
                arraylist.add(s);
                return arraylist;
            }

            String[] astring = s.split(" ");
            StringBuilder stringbuilder = new StringBuilder();

            for (String s1 : astring) {
                String s5;
                if (stringbuilder.isEmpty()) {
                    s5 = s1;
                } else {
                    String s3 = String.valueOf(stringbuilder);
                    s5 = s3 + " " + s1;
                }

                String s2 = s5;
                if (msdffontatlas.getWidth(s2) > f && !stringbuilder.isEmpty()) {
                    arraylist.add(stringbuilder.toString());
                    stringbuilder = new StringBuilder(s1);
                } else {
                    stringbuilder = new StringBuilder(s2);
                }
            }

            if (!stringbuilder.isEmpty()) {
                arraylist.add(stringbuilder.toString());
            }

            return arraylist;
        } else {
            return arraylist;
        }
    }

    private void renderLyricLine(MediaController mediacontroller) {
        String s2 = mediacontroller.getArtist();
        String s1 = mediacontroller.getTitle();
        String s = s1 + "|" + s2;
        if (!s.equals(this.lastLyric)) {
            this.lastLyric = s;
            BufferedImage bufferedimage = MediaManager.getProvider().getCoverImage();
            if (bufferedimage != null) {
                long i = 0L;
                long j = 0L;
                long k = 0L;
                int i2 = bufferedimage.getHeight();
                int l1 = bufferedimage.getWidth();
                int l = Math.max(1, l1 * i2 / 2000);
                int i1 = 0;
                int j1 = 0;

                while (true) {
                    i2 = bufferedimage.getHeight();
                    l1 = bufferedimage.getWidth();
                    if (j1 >= l1 * i2) {
                        float f5 = (float)i / i1 / 255.0F;
                        float f6 = (float)j / i1 / 255.0F;
                        float f = (float)k / i1 / 255.0F;
                        float f1 = Math.max(f5, Math.max(f6, f));
                        float f2 = Math.min(f5, Math.min(f6, f));
                        float f3 = f1 > 0.0F ? (f1 - f2) / f1 : 0.0F;
                        if (f3 < 0.3F) {
                            float f4 = (f5 + f6 + f) / 3.0F;
                            f5 = ColorUtil.clamp01(f4 + (f5 - f4) * 1.5F);
                            f6 = ColorUtil.clamp01(f4 + (f6 - f4) * 1.5F);
                            f = ColorUtil.clamp01(f4 + (f - f4) * 1.5F);
                        }

                        this.coverColor1 = new Color(ColorUtil.clamp01(f5 * 1.3F), ColorUtil.clamp01(f6 * 1.3F), ColorUtil.clamp01(f * 1.3F));
                        this.coverColor2 = new Color(ColorUtil.clamp01(f5 * 0.7F), ColorUtil.clamp01(f6 * 0.7F), ColorUtil.clamp01(f * 0.7F));
                        this.coverColor3 = new Color(ColorUtil.clamp01(f5 * 1.1F), ColorUtil.clamp01(f6 * 1.1F), ColorUtil.clamp01(f * 1.1F));
                        this.coverColor4 = new Color(ColorUtil.clamp01(f5 * 0.9F), ColorUtil.clamp01(f6 * 0.9F), ColorUtil.clamp01(f * 0.9F));
                        return;
                    }

                    int k1 = bufferedimage.getRGB(j1 % bufferedimage.getWidth(), j1 / bufferedimage.getWidth());
                    l1 = k1 >> 16;
                    i += l1 & 0xFF;
                    l1 = k1 >> 8;
                    j += l1 & 0xFF;
                    k += k1 & 0xFF;
                    i1++;
                    j1 += l;
                }
            }
        }
    }

    private static String truncate(MSDFFontAtlas msdffontatlas, float f, String s, float f1) {
        ToDoubleFunction<String> todoublefunction = s3 -> f > 0.0F ? msdffontatlas.getWidth(s3, f) : msdffontatlas.getWidth(s3);
        if (todoublefunction.applyAsDouble(s) <= f1) {
            return s;
        }

        float f2 = (float)todoublefunction.applyAsDouble("...");

        for (int i = s.length() - 1; i > 0; i--) {
            if (todoublefunction.applyAsDouble(s.substring(0, i)) + f2 <= f1) {
                String s1 = s.substring(0, i).stripTrailing();
                return s1 + "...";
            }
        }

        return "...";
    }
}
