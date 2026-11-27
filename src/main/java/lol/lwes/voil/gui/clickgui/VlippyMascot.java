package lol.lwes.voil.gui.clickgui;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Vlippy;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.MovementMode;
import lol.lwes.voil.util.media.TextToSpeech;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.TextureRectangleRenderer;
import lol.lwes.voil.util.render.UiAnimation;
import org.lwjgl.glfw.GLFW;

public class VlippyMascot {
    private static final Color DIM_COLOR = new Color(235, 235, 235);
    private static final String[] MESSAGES = new String[]{
        "It looks like you're trying to crystal pvp. Would you like me to hold your totem?",
        "Tip: pressing W makes you go forward. You're welcome.",
        "I saw you miss that combo. I see everything.",
        "Your config has been reviewed. No further comments.",
        "Have you tried turning your opponent off and on again?",
        "Reminder: shields are for people with something to lose.",
        "I would help, but I'm decorative.",
        "This client was made by lwes_ and sootysplash. Do you know them?",
        "This client uses BlossomEngine for rendering. Thanks! ceejay",
        "Me and melaken editing the next video",
        "Tip: left click to attack right click to use",
        "Are you free after work?",
        "I do hard work behind the scene. I hate it here!",
        "Want coffee?"
    };
    private final ClickGuiScreen screen;
    private final UiAnimation bubbleAnimation = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.7, 0.2);
    private final UiAnimation hoverAnimation = new UiAnimation(EasingType.FLUID_SPRING).setFluidParams(0.65, 0.25);
    private final List<String> lines = new ArrayList<>();
    private boolean bubbleVisible;
    private boolean hovered;
    private long nextMessageTime;
    private long speakStartTime;
    private long lastClickTime;
    private int lastMessageIndex = -1;
    private float renderX;
    private float renderY;
    private MovementMode mood = MovementMode.NONE;
    private long moodEndTime;
    private float posX;
    private float posY;
    private boolean positionInitialized;
    private long lastFrameTime;
    private float angle;
    private final Map<String, Long> cache = new HashMap<>();
    private static TextToSpeech voicePlayer;
    private static boolean voiceFailed;

    public VlippyMascot(ClickGuiScreen clickguiscreen) {
        this.screen = clickguiscreen;
    }

    private void showMessage(long i, String s) {
        this.mood = MovementMode.NONE;
        this.angle = 0.0F;
        this.nextMessageTime = i + 6000L + ThreadLocalRandom.current().nextLong(10000L);
        if (s != null) {
            this.startSpeaking(s, i);
        }
    }

    private String pickRandomMessage() {
        ThreadLocalRandom threadlocalrandom = ThreadLocalRandom.current();
        boolean flag = "Voil+".equalsIgnoreCase(String.valueOf(ClientSettings.userRank.getValue()));
        if (!flag && this.lastMessageIndex != -2 && threadlocalrandom.nextInt(5) == 0) {
            this.lastMessageIndex = -2;
            return "You should look into buying Voil+!";
        }

        if (threadlocalrandom.nextInt(4) == 0) {
            ModuleManager modulemanager1 = ModuleManager.INSTANCE;
            long k1 = modulemanager1.getModules().stream().filter(module -> module.isEnabled()).count();
            this.lastMessageIndex = -1;
            if (k1 == 0L) {
                return "It looks like nothing is enabled. Bold strategy.";
            }

            String s = this.screen.selectedCategory.name;
            return "It looks like you're browsing " + s + " with " + k1 + " modules on.";
        } else {
            int i1 = MESSAGES.length;
            int i = threadlocalrandom.nextInt(i1 + 4);
            if (i == MESSAGES.length) {
                this.lastMessageIndex = -1;
                String s8 = String.valueOf(ClientSettings.username.getValue());
                return "Good job! " + s8;
            }

            if (i == MESSAGES.length + 1) {
                ModuleManager modulemanager = ModuleManager.INSTANCE;
                List list = modulemanager.getModules();
                this.lastMessageIndex = -1;
                String s9 = ((Module)list.get(threadlocalrandom.nextInt(list.size()))).getName();
                return "Have you checked out " + s9 + "?";
            }

            i1 = MESSAGES.length;
            if (i == i1 + 2) {
                this.lastMessageIndex = -1;
                int j1 = threadlocalrandom.nextInt(1, 10000);
                return "Was this your number? " + j1;
            }

            i1 = MESSAGES.length;
            if (i == i1 + 3) {
                this.lastMessageIndex = -1;
                long l1 = (long)GLFW.glfwGetTime();
                long k = l1 / 3600L;
                long l = l1 % 3600L / 60L;
                if (k > 0L) {
                    return "The game has been open for " + k + "h " + l + "m. I'm not judging. (I am.)";
                } else if (l > 0L) {
                    return "The game has been open for " + l + " minutes. Are you warmed up?";
                } else {
                    return "The game has been open for " + l1 + " seconds. Getting warmed up?";
                }
            } else {
                int j;
                do {
                    j = threadlocalrandom.nextInt(MESSAGES.length);
                } while (MESSAGES.length > 1 && j == this.lastMessageIndex);

                this.lastMessageIndex = j;
                return MESSAGES[j];
            }
        }
    }

    public void render(int i, int j, float f) {
        if (!(f < 0.02F)) {
            long k = System.currentTimeMillis();
            if (!this.bubbleVisible && k >= this.nextMessageTime && this.mood == MovementMode.NONE) {
                int l = ThreadLocalRandom.current().nextInt(10);
                if (l == 0) {
                    this.setMood(MovementMode.FOLLOW, k, "Wait up. We're friends now.");
                } else if (l == 1) {
                    this.setMood(MovementMode.FLEE, k, "Personal space. Please.");
                } else if (l == 2) {
                    this.setMood(MovementMode.FALL, k, "Uh oh.");
                } else {
                    this.startSpeaking(this.nextMessage(), k);
                }
            }

            if (this.bubbleVisible && k >= this.speakStartTime) {
                this.bubbleVisible = false;
                if (this.mood == MovementMode.NONE) {
                    this.nextMessageTime = k + 6000L + ThreadLocalRandom.current().nextLong(10000L);
                }
            }

            float f25 = GuiScale.getScaledWidth();
            float f1 = GuiScale.getScaledHeight();
            float f2 = (1.0F - f) * 78.0F;
            float f3 = f25 - 48.0F - 10.0F;
            float f4 = f1 - 48.0F - 10.0F + f2;
            if (!this.positionInitialized) {
                this.posX = f3;
                this.posY = f4;
                this.positionInitialized = true;
                this.lastFrameTime = k;
            }

            float f5 = Math.min(0.1F, (float)(k - this.lastFrameTime) / 1000.0F);
            this.lastFrameTime = k;
            boolean flag = false;
            long i1 = k - this.moodEndTime;
            int k1 = this.mood.ordinal();
            if (k1 != 1) {
                if (k1 != 2) {
                    if (k1 == 3) {
                        flag = true;
                        this.angle += 900.0F * f5;
                        this.posY = this.posY + this.angle * f5;
                        f3 = this.posX;
                        if (this.posY > f1 + 48.0F) {
                            this.posY = f1 + 48.0F;
                            this.angle = 0.0F;
                            if (i1 > 0L) {
                                this.showMessage(k, "I'm fine.");
                            }
                        }
                    }
                } else if (i1 > 0L) {
                    this.showMessage(k, null);
                } else {
                    float f6 = this.posX + 24.0F - i;
                    float f7 = this.posY + 24.0F - j;
                    float f8 = (float)Math.sqrt(f6 * f6 + f7 * f7);
                    if (f8 < 140.0F && f8 > 0.01F) {
                        f3 = this.posX + f6 / f8 * 100.0F;
                        f4 = this.posY + f7 / f8 * 100.0F;
                    } else {
                        f3 = this.posX;
                        f4 = this.posY;
                    }
                }
            } else if (i1 > 0L) {
                this.showMessage(k, "Ok. Going home.");
            } else {
                f3 = i - 24.0F;
                f4 = j + 16.0F;
            }

            if (this.mood == MovementMode.FOLLOW || this.mood == MovementMode.FLEE) {
                f3 = Math.max(10.0F, Math.min(f25 - 48.0F - 10.0F, f3));
                f4 = Math.max(10.0F, Math.min(f1 - 48.0F - 10.0F, f4));
            }

            float f26 = this.mood == MovementMode.FOLLOW ? 6.0F : 4.0F;
            float f27 = 1.0F - (float)Math.exp(-f26 * f5);
            this.posX = this.posX + (f3 - this.posX) * f27;
            if (!flag) {
                this.posY = this.posY + (f4 - this.posY) * f27;
            }

            float f28 = this.posX;
            float f9 = this.posY;
            long j1 = k - this.lastClickTime;
            if (j1 < 0L) {
                float f10 = 1.0F - (float)j1 / 500.0F;
                f28 += (float)(Math.sin(j1 * 0.09) * 4.0 * f10);
                f9 += (float)(Math.cos(j1 * 0.11) * 2.0 * f10);
            } else if (this.mood != MovementMode.FALL) {
                f9 += (float)(Math.sin(k / 500.0) * 3.5);
            }

            f28 = Math.round(f28 * 4.0F) / 4.0F;
            f9 = Math.round(f9 * 4.0F) / 4.0F;
            this.renderX = f28;
            this.renderY = f9;
            boolean flag1 = i >= f28 && i <= f28 + 48.0F && j >= f9 && j <= f9 + 48.0F;
            Color color = flag1 ? Color.WHITE : DIM_COLOR;
            GlTexture gltexture = this.screen.vlippyTexture;
            Render2DUtil.textureRenderer.renderAlt(gltexture, f28, f9, 48.0F, 48.0F, 0.0F, 1.0F, color);
            if (!this.bubbleVisible) {
                this.hovered = false;
            }

            double d0 = this.hovered ? 1.0 : 0.0;
            this.hoverAnimation.setTarget(d0);
            this.hoverAnimation.update();
            float f11 = Math.max(0.0F, Math.min(1.15F, this.hoverAnimation.getFloat()));
            if (f11 > 0.02F && this.screen.coffeeTexture != null) {
                float f12 = 26.0F;
                float f13 = f28 + 12.0F - 34.0F * f11;
                float f14 = f9 + 14.0F - 12.0F * f11 + (float)(Math.sin(k / 300.0) * 1.2F);
                f13 = Math.round(f13 * 4.0F) / 4.0F;
                f14 = Math.round(f14 * 4.0F) / 4.0F;
                TextureRectangleRenderer texturerectanglerenderer = Render2DUtil.textureRenderer;
                GlTexture gltexture2 = this.screen.coffeeTexture;
                Color color6 = Color.WHITE;
                int l1 = (int)(255.0F * Math.min(1.0F, f11) * f);
                Color color2 = color6;
                Color color1 = ColorUtil.withAlpha(color2, l1);
                float f19 = 1.0F;
                float f20 = 0.0F;
                float f21 = f12;
                float f22 = f12;
                float f23 = f14;
                float f24 = f13;
                GlTexture gltexture1 = gltexture2;
                texturerectanglerenderer.renderAlt(gltexture1, f24, f23, f22, f21, f20, f19, color1);
            }

            double d1 = this.bubbleVisible ? 1.0 : 0.0;
            this.bubbleAnimation.setTarget(d1);
            this.bubbleAnimation.update();
            float f29 = Math.max(0.0F, Math.min(1.0F, this.bubbleAnimation.getFloat()));
            if (!(f29 < 0.02F)) {
                int j2 = (int)(255.0F * f29 * f);
                float f30 = VoilClient.fonts.getSelectedFont().getLineHeight(9.0F);
                float f15 = this.lines.size() * (f30 + 1.5F) + 6.0F * 2.0F;
                float f16 = Math.max(10.0F, Math.min(f25 - 150.0F - 10.0F, f28 + 48.0F - 150.0F));
                float f17 = f9 - f15 - 10.0F + (1.0F - f29) * 6.0F;
                if (f17 < 10.0F) {
                    f17 = f9 + 48.0F + 10.0F - (1.0F - f29) * 6.0F;
                }

                int i2 = (int)(245.0F * f29 * f);
                Color color3 = GuiTheme.moduleColor;
                Render2DUtil.drawRoundedRect(f16, f17, 150.0F, f15, 6.0F, 1.0F, ColorUtil.withAlpha(color3, i2));
                Color color4 = GuiTheme.accentColor1;
                Render2DUtil.drawOutline(f16, f17, 150.0F, f15, 6.0F, 0.4F, 0.5F, ColorUtil.withAlpha(color4, j2));
                float f18 = f17 + 6.0F;

                for (String s : this.lines) {
                    MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
                    float f31 = f16 + 6.0F;
                    Color color5 = Color.WHITE;
                    msdffontatlas.render(s, f31, f18, 9.0F, ColorUtil.withAlpha(color5, j2).getRGB());
                    f18 += f30 + 1.5F;
                }
            }
        }
    }

    public boolean mouseClicked(double d0, double d1, int i) {
        if (i != 0) {
            return false;
        }

        if (!(d0 < this.renderX) && !(d0 > this.renderX + 48.0F) && !(d1 < this.renderY) && !(d1 > this.renderY + 48.0F)) {
            long j = System.currentTimeMillis();
            if (this.mood == MovementMode.FLEE) {
                this.showMessage(j, "You caught me. Congratulations.");
                return true;
            }

            if (this.mood != MovementMode.NONE) {
                String s = this.nextMessage();
                this.startSpeaking(s, j);
                return true;
            }

            int k = ThreadLocalRandom.current().nextInt(10);
            if (k == 0) {
                this.setMood(MovementMode.FOLLOW, j, "Wait up. We're friends now.");
            } else if (k == 1) {
                this.setMood(MovementMode.FLEE, j, "Don't touch me.");
            } else if (k == 2) {
                this.setMood(MovementMode.FALL, j, "Uh oh.");
            } else {
                this.startSpeaking(this.nextMessage(), j);
            }

            return true;
        } else {
            return false;
        }
    }

    private String nextMessage() {
        long i = System.currentTimeMillis();

        for (int j = 0; j < 8; j++) {
            String s = this.pickRandomMessage();
            Long olong = this.cache.get(s);
            if (olong == null || i - olong > 30000L) {
                return s;
            }
        }

        int k = ThreadLocalRandom.current().nextInt(1, 10000);
        return "Was this your number? " + k;
    }

    private void startSpeaking(String s, long i) {
        this.lines.clear();
        this.wrapText(s);
        this.bubbleVisible = true;
        this.hovered = false;
        this.lastClickTime = i;
        this.speakStartTime = i;
        this.cache.entrySet().removeIf(entry -> i - entry.getValue() > 30000L);
        this.cache.put(s, i);
        this.speak(s);
    }

    private void setMood(MovementMode movementmode, long i, String s) {
        this.mood = movementmode;
        this.moodEndTime = i;
        if (movementmode == MovementMode.FALL) {
            this.angle = -180.0F;
        }

        if (s != null) {
            this.startSpeaking(s, i);
        }
    }

    public void reset() {
        this.bubbleVisible = false;
        this.bubbleAnimation.reset();
        this.hovered = false;
        this.hoverAnimation.reset();
        this.lines.clear();
        this.lastClickTime = 0L;
        this.lastMessageIndex = -1;
        this.nextMessageTime = System.currentTimeMillis() + 2500L;
        this.speakStartTime = 0L;
        this.mood = MovementMode.NONE;
        this.positionInitialized = false;
        this.angle = 0.0F;
    }

    private void speak(String s) {
        if (Vlippy.showMascot.isEnabled()) {
            if (!voiceFailed) {
                try {
                    if (voicePlayer == null) {
                        voicePlayer = new TextToSpeech();
                    }

                    voicePlayer.speak(s);
                } catch (IllegalStateException illegalstateexception) {
                    voiceFailed = true;
                }
            }
        }
    }

    private void wrapText(String s) {
        StringBuilder stringbuilder = new StringBuilder();

        for (String s1 : s.split(" ")) {
            String s5;
            if (stringbuilder.isEmpty()) {
                s5 = s1;
            } else {
                String s3 = String.valueOf(stringbuilder);
                s5 = s3 + " " + s1;
            }

            String s2 = s5;
            if (VoilClient.fonts.getSelectedFont().getWidth(s2, 9.0F) > 138.0F && !stringbuilder.isEmpty()) {
                this.lines.add(stringbuilder.toString());
                stringbuilder = new StringBuilder(s1);
            } else {
                stringbuilder = new StringBuilder(s2);
            }
        }

        if (!stringbuilder.isEmpty()) {
            this.lines.add(stringbuilder.toString());
        }
    }
}
