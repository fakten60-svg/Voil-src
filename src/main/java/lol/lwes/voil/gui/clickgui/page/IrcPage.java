package lol.lwes.voil.gui.clickgui.page;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.community.CommunityPost;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.CloudConfig;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiPanel;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.gui.clickgui.ClickTarget;
import lol.lwes.voil.gui.clickgui.PostCard;
import lol.lwes.voil.irc.IrcState;
import lol.lwes.voil.util.font.EmojiAtlas;
import lol.lwes.voil.util.font.EmojiTextRenderer;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.EmojiGlyph;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.ServerIconCache;
import lol.lwes.voil.util.render.TextureCache;
import lol.lwes.voil.util.render.TextureRectangleRenderer;
import lol.lwes.voil.util.render.UiAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class IrcPage extends ClickGuiPanel {
    private static final String[] STRING_TABLE = new String[]{
        "\ud83d\udd25", "\ud83d\udc80", "\ud83d\ude02", "❤", "\ud83d\udc4d", "\ud83d\udc4e", "\ud83d\ude2d", "\ud83e\udd13"
    };
    private final List<ClickTarget> messages = new ArrayList<>();
    private final List<EmojiGlyph> renderedLines = new ArrayList<>();
    private final UiAnimation scrollAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.78, 0.15);
    private final UiAnimation inputAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private final UiAnimation connectAnimation = new UiAnimation(EasingType.FLUID_RESPONSIVE);
    private final UiAnimation pickerAnimation = new UiAnimation(EasingType.FLUID_SMOOTH).setFluidParams(0.7, 0.15);
    private final Runnable onClose = this::markConnecting;
    private List<PostCard> servers = new ArrayList<>();
    private volatile boolean connecting = true;
    private float scrollOffset = -1.0F;
    private long lastMessageTime = -1L;
    private String inputText = "";
    private boolean inputFocused;
    private double mouseX;
    private double mouseY;
    private long lastClickTime;
    private boolean shiftDown = true;
    private boolean showPicker;
    private double pickerScroll;
    private long noticeStartTime;
    private String noticeText = "";
    private long cursorBlinkTime;
    private long lastTypeTime;
    private int cursorPos;
    private float selectionAnchor;
    private float selectionEnd;
    public static final Color TEXT_PRIMARY = new Color(230, 230, 234);
    public static final Color TEXT_SECONDARY = new Color(154, 154, 165);
    public static final Color TEXT_MUTED = new Color(139, 139, 150);
    public static final Color TEXT_PLACEHOLDER = new Color(110, 110, 120);
    public static final Color TEXT_DISABLED = new Color(74, 74, 85);
    public static final Color COLOR_ERROR = new Color(248, 113, 113);
    public static final Color COLOR_ONLINE = new Color(74, 222, 128);
    public static final Color COLOR_ACCENT = new Color(88, 101, 242);
    public static final Color COLOR_ACCENT_LIGHT = new Color(180, 188, 255);
    public static final Color COLOR_RED = new Color(255, 92, 92);
    public static final Color COLOR_BLUE = new Color(92, 184, 255);
    public static final Color COLOR_PURPLE = new Color(155, 108, 255);
    public static final Color COLOR_GOLD = new Color(255, 194, 75);
    public static final Color COLOR_LIGHT_GRAY = new Color(221, 221, 221);
    public static final int TEXT_PRIMARY_RGB = TEXT_PRIMARY.getRGB();
    public static final int WHITE_RGB = Color.WHITE.getRGB();

    public IrcPage(ClickGuiScreen clickguiscreen) {
        super(clickguiscreen);
    }

    private float getChatX() {
        int j = this.screen.sidebarWidth;
        int i = this.screen.windowX;
        i += j;
        return i + 10;
    }

    private float getChatWidth() {
        int j = this.screen.sidebarWidth;
        int i = this.screen.windowWidth;
        i -= j;
        return i + -21 + 1;
    }

    private float getChatY() {
        int i = this.screen.windowY;
        return i + 28;
    }

    private float getChatHeight() {
        int j = this.screen.windowHeight;
        int i = this.screen.windowY;
        i += j;
        return i + -33 + 1;
    }

    private void drawMessages(List<PostCard> list, float f, float f1, float f2, float f3, double d0, int i, int j) {
        int i1 = (int)f;
        int k1 = i1 + -3 + 1;
        i1 = (int)f2;
        int l1 = i1 + -3 + 1;
        int j1 = (int)(f + f1);
        RenderManager.setScissor(k1, l1, j1, (int)f3);
        float f4 = (float)(f2 - (d0 - this.mouseX));
        String s1 = IrcState.getUsername();
        long k = 0L;

        for (PostCard postcard : list) {
            boolean flag = this.isOwnMessage();
            float f5 = postcard.getHeight(flag);
            if (f4 + f5 >= f2 && f4 <= f3) {
                boolean hovered = i >= f && i <= f + f1 && j >= f4 && j <= f4 + f5;
                if (hovered) {
                    k = postcard.getPost().id();
                }

                this.drawMessage(postcard, f, f4, f1, f5, flag, hovered, i, s1);
            }

            f4 += f5;
        }

        RenderManager.disableScissor();
        this.lastTypeTime = k;
    }

    private void drawEmptyState(float f, float f1, float f2, float f3) {
        String s = IrcState.isConnected() ? "No messages yet say something." : "IRC is currently offline.";
        float f4 = this.getTextWidth(s, 10.0F);
        VoilClient.fonts.getSelectedFont().render(s, f + (f1 - f4) / 2.0F, f2 + f3 / 2.0F - 6.0F, 10.0F, TEXT_PLACEHOLDER.getRGB());
    }

    private boolean isInside(int i, float f) {
        return i >= f - 2.0F && i <= f + 11.0F;
    }

    private void drawScrollbar(float f, float f1, float f2) {
        int i = this.inputText.codePointCount(0, this.inputText.length());
        if (i <= 110) {
            return;
        }

        String s1 = String.valueOf(150 - i);
        float f3 = VoilClient.fonts.getGreyscale().getWidth(s1, 8.0F);
        Color color = i >= 150 ? COLOR_ERROR : TEXT_PLACEHOLDER;
        VoilClient.fonts.getGreyscale().render(s1, f + f1 - f3 - 26.0F, f2 + 1.0F, 8.0F, color.getRGB());
    }

    private void drawButton(String s, float f, float f1, float f2, float f3, int i, Color color, Runnable runnable) {
        boolean hovered = i >= f && i <= f + f2;
        Color color1 = hovered ? ColorUtil.scaleBrightness(color, 1.25F) : color;
        float f4 = 1.0F;
        float f5 = 4.0F;
        float f6 = f3;
        float f7 = f2;
        float f8 = f1;
        Render2DUtil.drawRoundedRect(f, f8, f7, f6, f5, f4, color1);
        float f9 = VoilClient.fonts.getSelectedFont().getWidth(s, 8.5F);
        float f10 = f1 + (f3 - VoilClient.fonts.getSelectedFont().getLineHeight(8.5F)) / 2.0F;
        VoilClient.fonts.getSelectedFont().render(s, f + (f2 - f9) / 2.0F, f10, 8.5F, -1);
        this.drawHoverButton(f, f1, f2, f3, runnable);
    }

    public void mouseClicked(double d0, double d1, int i) {
        if (i == 0) {
            for (ClickTarget clicktarget : this.messages) {
                if (clicktarget.contains(d0, d1)) {
                    clicktarget.activate();
                    return;
                }
            }

            if (this.showPicker) {
                this.shareConfig();
            } else {
                float f = this.getChatX();
                float f1 = this.getChatWidth();
                float f2 = this.getChatHeight();
                this.inputFocused = d0 >= f && d0 <= f + f1 && d1 >= f2 && d1 <= f2 + this.getInputWidth();
            }
        }
    }

    private void sendMessage() {
        if (this.renderedLines.isEmpty()) {
            return;
        }

        int colon = this.inputText.lastIndexOf(':');
        if (colon < 0) {
            return;
        }

        EmojiGlyph glyph = this.renderedLines.get(this.cursorPos);
        this.inputText = this.inputText.substring(0, colon) + glyph.getEmoji() + " ";
        this.renderedLines.clear();
    }

    @Override
    public void onOpen() {
        IrcState.addListener(this.onClose);
        this.markConnecting();
        ApiClient.INSTANCE.loadIrc();
        this.inputFocused = true;
        this.mouseY = 0.0;
        this.mouseX = 0.0;
        this.scrollAnimation.snapTo(0.0);
        this.inputAnimation.reset();
        this.connectAnimation.snapTo(1.0);
        this.lastClickTime = System.currentTimeMillis();
    }

    private void drawInputBox(float f) {
        if (!this.renderedLines.isEmpty()) {
            float f1 = this.renderedLines.size() * 15.0F + 6.0F;
            float f2 = f + 6.0F;
            float f3 = this.getChatHeight() - f1 - 4.0F;
            Color color = GuiTheme.moduleColor;
            Render2DUtil.drawRoundedRect(f2, f3, 150.0F, f1, 6.0F, 1.0F, ColorUtil.withAlpha(color, 246));
            Color color1 = GuiTheme.accentColor1;
            Render2DUtil.drawOutline(f2, f3, 150.0F, f1, 6.0F, 0.3F, 0.5F, ColorUtil.withAlpha(color1, 160));
            MSDFFontAtlas font = VoilClient.fonts.getSelectedFont();

            for (int i = 0; i < this.renderedLines.size(); i++) {
                EmojiGlyph glyph = this.renderedLines.get(i);
                float lineY = f3 + 3.0F + i * 15.0F;
                boolean selected = i == this.cursorPos;
                if (selected) {
                    Render2DUtil.drawRoundedRect(f2 + 3.0F, lineY, 150.0F - 6.0F, 15.0F - 1.0F, 4.0F, 1.0F, ColorUtil.withAlpha(GuiTheme.accentColor1, 80));
                }

                this.drawText(glyph.getEmoji(), f2 + 8.0F, lineY - 3.0F, 12.0F, WHITE_RGB);
                font.render(":" + glyph.getName() + ":", f2 + 26.0F, lineY + 2.5F, 8.5F, (selected ? Color.WHITE : TEXT_SECONDARY).getRGB());
            }
        }
    }

    private void markConnecting() {
        this.connecting = true;
    }

    public void render(int i, int j) {
        this.messages.clear();
        float f = this.getChatX();
        float f1 = this.getChatWidth();
        float f2 = this.getChatY();
        float f3 = this.getInputY();
        float f4 = f3 - f2;
        this.selectionAnchor = f2;
        this.selectionEnd = f3;
        this.drawChatPanel(f, f1);
        float f6 = f1 - 16.0F - 14.0F;
        List<PostCard> list = this.getVisibleMessages(f6);
        float f5 = 0.0F;

        for (PostCard postcard : list) {
            f5 += postcard.getHeight(this.isOwnMessage());
        }

        double d1 = Math.max(0.0, f5 - f4);
        this.mouseY = Math.clamp(this.mouseY, 0.0, d1);
        double d0 = this.mouseY;
        this.scrollAnimation.setTarget(d0);
        this.scrollAnimation.update();
        this.mouseX = this.scrollAnimation.getValue();
        this.drawMessages(list, f, f1, f2, f3, d1, i, j);
        if (list.isEmpty()) {
            this.drawEmptyState(f, f1, f2, f4);
        }

        if (f5 > f4) {
            this.drawRoundedPanel(f + f1 - 2.0F, f2, f4, f5, d1);
        }

        this.drawInputField(f, f1, i, j);
        this.drawInputBox(f);
        if (this.showPicker) {
            this.messages.clear();
            this.drawConfigPicker(i, j);
        }
    }

    @Override
    public void mouseDragged() {
    }

    public void mouseScrolled$1() {
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (!this.inputFocused || this.showPicker || isControlDown(modifiers)) {
            return;
        }

        if (chr >= ' ' && chr != 127) {
            String s = String.valueOf(chr);
            this.insertText(s);
            this.pollServers();
        }
    }

    public void keyPressed(int i, int j) {
        if (!this.inputFocused || this.showPicker) {
            return;
        }

        if (!this.renderedLines.isEmpty() && this.isPrintable(i)) {
            return;
        }

        if (i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) {
            this.shareConfig();
        } else if (i == GLFW.GLFW_KEY_BACKSPACE) {
            this.loadServers();
        } else if (isControlDown(j)) {
            if (i == 67) {
                VoilClient.mc.keyboard.setClipboard(this.inputText);
            } else if (i == 86) {
                String s = VoilClient.mc.keyboard.getClipboard();
                this.insertText(s);
                this.pollServers();
            } else if (i == 88) {
                VoilClient.mc.keyboard.setClipboard(this.inputText);
                this.inputText = "";
                this.renderedLines.clear();
            }
        }
    }

    private void drawServerCard(CommunityPost communitypost, float f, float f1, float f2, int i) {
        Color color = GuiTheme.moduleColor;
        Render2DUtil.drawRoundedRect(f, f1, f2, 50.0F, 6.0F, 1.0F, ColorUtil.withAlpha(color, 210));
        Color color1 = GuiTheme.accentColor1;
        Render2DUtil.drawOutline(f, f1, f2, 50.0F, 6.0F, 0.3F, 0.5F, ColorUtil.withAlpha(color1, 120));
        String s1 = communitypost.getDataString();
        GlTexture gltexture = ServerIconCache.getIcon(s1);
        if (gltexture != null) {
            Render2DUtil.textureRenderer.render(gltexture, f + 7.0F, f1 + 7.0F, 26.0F, 26.0F, 4.0F, 1.0F);
        } else {
            Color color2 = Color.black;
            Render2DUtil.drawRoundedRect(f + 7.0F, f1 + 7.0F, 26.0F, 26.0F, 4.0F, 1.0F, ColorUtil.withAlpha(color2, 90));
        }

        VoilClient.fonts.getInterBold().render(s1, f + 40.0F, f1 + 6.0F, 10.0F, -1);
        String s2 = communitypost.getDataString();
        if (!s2.isEmpty()) {
            VoilClient.fonts.getGreyscale().render(this.truncateToWidth(s2, 8.5F, f2 - 48.0F), f + 40.0F, f1 + 17.0F, 8.5F, TEXT_MUTED.getRGB());
        }

        int l = communitypost.getDataInt();
        String s6 = communitypost.getDataString();
        int k = communitypost.getDataInt();
        String s3 = l + "/" + k + "  " + s6;
        float f3 = VoilClient.fonts.getGreyscale().getWidth(s3, 8.0F);
        VoilClient.fonts.getGreyscale().render(s3, f + f2 - f3 - 8.0F, f1 + 6.0F, 8.0F, TEXT_PLACEHOLDER.getRGB());
        this.drawButton("Join", f + 40.0F, f1 + 32.0F, 40.0F, 12.0F, i, GuiTheme.accentColor1, () -> this.joinServer(s1));
        this.drawButton("Add to list", f + 86.0F, f1 + 32.0F, 62.0F, 12.0F, i, TEXT_DISABLED, () -> this.showNotice(s1));
    }

    private void drawConfigPicker(int i, int j) {
        float f = this.getChatX();
        float f1 = this.getChatWidth();
        float f2 = this.getChatY();
        float f3 = this.getChatHeight() + this.getInputWidth() - f2;
        Color color = Color.black;
        Render2DUtil.drawRoundedRect(f - 4.0F, f2 - 4.0F, f1 + 8.0F, f3 + 8.0F, 8.0F, 1.0F, ColorUtil.withAlpha(color, 175));
        float f4 = Math.min(270.0F, f1 - 16.0F);
        float f5 = Math.min(190.0F, f3 - 16.0F);
        float f6 = f + (f1 - f4) / 2.0F;
        float f7 = f2 + (f3 - f5) / 2.0F;
        Color color1 = GuiTheme.moduleColor;
        Render2DUtil.drawRoundedRect(f6, f7, f4, f5, 8.0F, 1.0F, ColorUtil.withAlpha(color1, 246));
        Render2DUtil.drawOutline(f6, f7, f4, f5, 8.0F, 0.4F, 0.6F, GuiTheme.accentColor1);
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE0D0(), f6 + 11.0F, f7 + 11.0F, 11.0F, GuiTheme.accentColor1.getRGB());
        VoilClient.fonts.getInterBold().render("Share a config", f6 + 28.0F, f7 + 10.0F, 11.0F, -1);
        boolean closeHovered = i >= f6 + f4 - 22.0F && i <= f6 + f4 - 8.0F && j >= f7 + 8.0F && j <= f7 + 22.0F;
        Color color4 = closeHovered ? Color.WHITE : TEXT_PLACEHOLDER;
        double d1 = f6 + f4 - 21.0F;
        Render2DUtil.drawPlus(color4, d1, f7 + 11.0F, 9.0F, 1.4F, 45.0F);
        this.messages.add(new ClickTarget(f6 + f4 - 24.0F, f7 + 6.0F, 20.0F, 20.0F, () -> this.showPicker = false));
        float f8 = f7 + 28.0F;
        float f9 = f5 - 36.0F;
        List<CloudConfig> configs = new ArrayList<>(ClientSettings.cloudConfigs.values());
        if (configs.isEmpty()) {
            String s1 = "No configs found — save one first.";
            float f14 = VoilClient.fonts.getSelectedFont().getWidth(s1, 9.0F);
            VoilClient.fonts.getSelectedFont().render(s1, f6 + (f4 - f14) / 2.0F, f8 + f9 / 2.0F - 5.0F, 9.0F, TEXT_PLACEHOLDER.getRGB());
            return;
        }

        float f10 = configs.size() * 30.0F;
        double d0 = Math.max(0.0, f10 - f9);
        this.pickerScroll = Math.clamp(this.pickerScroll, 0.0, d0);
        this.selectionAnchor = f8;
        this.selectionEnd = f8 + f9;
        int i1 = (int)f6;
        int l = (int)f8;
        int k = (int)(f6 + f4);
        RenderManager.setScissor(i1, l, k, (int)this.selectionEnd);
        float f11 = (float)(f8 - this.pickerScroll);

        for (CloudConfig cloudconfig : configs) {
            if (f11 + 30.0F >= f8 && f11 <= this.selectionEnd) {
                this.drawShareButton(cloudconfig, f6, f11, f4, 30.0F, i);
            }

            f11 += 30.0F;
        }

        RenderManager.disableScissor();
        if (f10 - f9 <= 0.0F) {
            return;
        }

        float f15 = Math.max(16.0F, f9 * (f9 / f10));
        float f16 = d0 <= 0.0 ? 0.0F : (float)(this.pickerScroll / d0);
        float f20 = f6 + f4 - 5.0F;
        Color color2 = Color.black;
        Render2DUtil.drawRoundedRect(f20, f8, 2.0F, f9, 1.0F, 1.0F, ColorUtil.withAlpha(color2, 70));
        float f21 = f6 + f4 - 5.0F;
        float f13 = f8 + (f9 - f15) * f16;
        Color color3 = GuiTheme.accentColor1;
        Render2DUtil.drawRoundedRect(f21, f13, 2.0F, f15, 1.0F, 1.0F, ColorUtil.withAlpha(color3, 175));
    }

    private static String formatTimeAgo(long i) {
        long j = (System.currentTimeMillis() - i) / 60000L;
        if (j < 1L) {
            return "now";
        } else if (j < 60L) {
            return j + "m";
        } else if (j < 1440L) {
            return j / 60L + "h";
        } else {
            return j / 1440L + "d";
        }
    }

    private boolean isOwnMessage() {
        return this.lastTypeTime != 0L;
    }

    private void shareConfig() {
        if (this.inputText.isBlank()) {
            ApiClient apiclient1 = ApiClient.INSTANCE;
            apiclient1.getAllConfigs(true);
            this.showPicker = true;
            this.pickerScroll = 0.0;
            this.inputText = "";
        } else {
            EmojiAtlas emojiatlas = VoilClient.fonts.getEmojiAtlas();
            String s2 = this.inputText;
            String s = emojiatlas.replaceShortcodes(s2);
            long i = this.noticeStartTime;
            String s1 = s;
            ApiClient apiclient = ApiClient.INSTANCE;
            apiclient.sendReply(s1, i);
            this.inputText = "";
            this.refreshServers();
            this.renderedLines.clear();
            this.mouseY = 0.0;
        }
    }

    private String truncateToWidth(String s, float f, float f1) {
        if (this.getTextWidth(s, f) <= f1) {
            return s;
        }

        while (!s.isEmpty() && this.getTextWidth(s + "…", f) > f1) {
            s = s.substring(0, s.length() - 1);
        }

        return s + "…";
    }

    public boolean isConnected() {
        return this.showPicker;
    }

    private void insertText(String s) {
        if (s == null || s.isEmpty()) {
            return;
        }

        String text = s.replace("\n", " ").replace("\r", "");
        StringBuilder builder = new StringBuilder(this.inputText);
        int length = this.inputText.codePointCount(0, this.inputText.length());

        for (int i = 0; i < text.length() && length < 150; length++) {
            int codePoint = text.codePointAt(i);
            builder.appendCodePoint(codePoint);
            i += Character.charCount(codePoint);
        }

        this.inputText = builder.toString();
    }

    @Override
    public void onClose() {
        IrcState.removeListener(this.onClose);
        this.inputFocused = false;
        this.lastTypeTime = 0L;
    }

    private String formatCount(float maxWidth) {
        if (this.inputText.isEmpty() || this.getTextWidth(this.inputText, 9.5F) <= maxWidth) {
            return this.inputText;
        }

        int low = 0;
        int high = this.inputText.length();

        while (low < high) {
            int mid = low + high >>> 1;
            if (this.getTextWidth(this.inputText.substring(this.clampCursor(mid)), 9.5F) <= maxWidth) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return this.inputText.substring(this.clampCursor(low));
    }

    private float getInputY() {
        return this.getChatHeight() - 6.0F;
    }

    private void showNotice(String s) {
        ServerList serverlist = new ServerList(VoilClient.mc);
        serverlist.loadFile();
        serverlist.add(new ServerInfo(s, s, ServerType.OTHER), false);
        serverlist.saveFile();
    }

    private int clampCursor(int i) {
        if (i > 0 && i < this.inputText.length() && Character.isLowSurrogate(this.inputText.charAt(i))) {
            return i + 1;
        }

        return i;
    }

    private boolean isOverInput(float f, float f1) {
        return f + f1 >= this.selectionAnchor && f <= this.selectionEnd;
    }

    private void drawReactionPicker(float f, float f1) {
        long i = System.currentTimeMillis();
        if ((float)(i - this.lastClickTime) >= 550.0F) {
            this.shiftDown = !this.shiftDown;
            this.lastClickTime = i;
        }

        double d0 = this.inputFocused && this.shiftDown ? 1.0 : 0.0;
        this.connectAnimation.setTarget(d0);
        this.connectAnimation.update();
        float f7 = this.connectAnimation.getFloat();
        if (!this.inputFocused || f7 <= 0.02F) {
            return;
        }

        float f8 = f + 1.5F;
        float f9 = f1 + 1.0F;
        int j = (int)(220.0F * f7);
        Color color1 = Color.WHITE;
        Color color = ColorUtil.withAlpha(color1, j);
        Render2DUtil.drawRoundedRect(f8, f9, 0.5F, 10.0F, 0.5F, 1.0F, color);
    }

    private void drawReplyPreview(float f, float f1, float f2, int i, int j) {
        Render2DUtil.drawRoundedRect(f, f2, f1, 13.0F, 4.0F, 1.0F, ColorUtil.withAlpha(GuiTheme.moduleColor, 215));
        Render2DUtil.drawRoundedRect(f + 5.0F, f2 + 3.0F, 1.5F, 7.0F, 0.75F, 1.0F, GuiTheme.accentColor1);
        VoilClient.fonts.getSelectedFont().render("Replying to " + this.noticeText, f + 11.0F, f2 + 2.0F, 8.5F, TEXT_SECONDARY.getRGB());
        boolean closeHovered = i >= f + f1 - 16.0F && i <= f + f1 - 2.0F && j >= f2 && j <= f2 + 13.0F;
        Color color1 = closeHovered ? Color.WHITE : TEXT_MUTED;
        double d0 = f + f1 - 10.0F;
        Render2DUtil.drawPlus(color1, d0, f2 + 3.0F, 7.0F, 1.2F, 45.0F);
        this.messages.add(new ClickTarget(f + f1 - 16.0F, f2, 14.0F, 13.0F, this::refreshServers));
    }

    private void drawMessage(PostCard postcard, float f, float f1, float f2, float f3, boolean b0, boolean b1, int i, String s) {
        CommunityPost communitypost = postcard.getPost();
        if (b1) {
            float f17 = f - 3.0F;
            float f7 = f1 - 2.0F;
            float f19 = f2 + 6.0F;
            float f20 = f3 - 1.0F;
            Color color = Color.white;
            Render2DUtil.drawRoundedRect(f17, f7, f19, f20, 4.0F, 1.0F, ColorUtil.withAlpha(color, 8));
        }

        if (communitypost.isEdited()) {
            this.drawEditorLine(communitypost, f, f1, f2);
            f1 = f1 + 10.0F + 2.0F;
        }

        if (this.isOverInput(f1, 11.0F)) {
            this.drawAvatar(communitypost, f, f1);
            this.drawLinkButton(communitypost, f, f1, s);
        }

        float f4 = f + 16.0F + 7.0F;
        float f5 = f1 + 11.0F;
        List<String> list = postcard.getLines();
        int j = postcard.getVisibleLineCount(b0);

        for (int k = 0; k < j; k++) {
            if (f5 + 11.0F >= this.selectionAnchor && f5 <= this.selectionEnd) {
                this.drawText(list.get(k), f4, f5 - 1.0F, 9.5F, TEXT_PRIMARY_RGB);
            }

            f5 += 11.0F;
        }

        if (!b0 && postcard.isCollapsible()) {
            if (f5 + 10.0F >= this.selectionAnchor && f5 <= this.selectionEnd) {
                int l = postcard.getOverflowLineCount();
                String s4 = "+" + l + " more";
                this.drawText(s4, f4, f5 - 1.0F, 8.0F, TEXT_PLACEHOLDER.getRGB());
            }

            f5 += 10.0F;
        }

        float f11 = f5 + 2.0F;
        if (communitypost.isSharedContent()) {
            float f6 = communitypost.isConfigPost() ? 45.0F : 50.0F;
            if (this.isOverInput(f5 + 2.0F, f6)) {
                this.drawMessageText(f5 + 2.0F);
            }

            f11 = f5 + 2.0F + f6 + 2.0F;
        }

        if (this.isOverInput(f11, 14.0F)) {
            this.drawServerRow(communitypost, f4, f11);
        }

        if (b1 && communitypost.id() > 0L) {
            this.drawCardButton(communitypost, f + f2 - 32.0F, f1, i);
        }
    }

    public void scrollToBottom() {
        this.inputFocused = false;
    }

    private float getTextWidth(String s, float f) {
        EmojiAtlas emojiatlas = VoilClient.fonts.getEmojiAtlas();
        return emojiatlas == null
            ? VoilClient.fonts.getSelectedFont().getWidth(s, f)
            : EmojiTextRenderer.getTextWidth(VoilClient.fonts.getSelectedFont(), emojiatlas, s, f);
    }

    private float drawText(String s, float f, float f1, float f2, int i) {
        EmojiAtlas emojiatlas = VoilClient.fonts.getEmojiAtlas();
        if (emojiatlas != null) {
            return EmojiTextRenderer.renderText(VoilClient.fonts.getSelectedFont(), emojiatlas, s, f, f1, f2, i);
        }

        VoilClient.fonts.getSelectedFont().render(s, f, f1, f2, i);
        return VoilClient.fonts.getSelectedFont().getWidth(s, f2);
    }

    private void drawHoverButton(float f, float f1, float f2, float f3, Runnable runnable) {
        float f4 = Math.max(f1, this.selectionAnchor);
        float f5 = Math.min(f1 + f3, this.selectionEnd);
        if (f5 > f4) {
            this.messages.add(new ClickTarget(f, f4, f2, f5 - f4, runnable));
        }
    }

    private void pollServers() {
        this.renderedLines.clear();
        this.cursorPos = 0;
        EmojiAtlas emojiatlas = VoilClient.fonts.getEmojiAtlas();
        if (emojiatlas == null) {
            return;
        }

        int colon = this.inputText.lastIndexOf(':');
        if (colon < 0 || this.inputText.indexOf(' ', colon) >= 0) {
            return;
        }

        String query = this.inputText.substring(colon + 1);
        if (!query.isEmpty() && !query.contains(":")) {
            this.renderedLines.addAll(emojiatlas.searchGlyphs(query, 6));
        }
    }

    private void drawEditorLine(CommunityPost communitypost, float f, float f1, float f2) {
        float f5 = f + 6.0F;
        float f3 = f1 + 3.0F;
        Color color = GuiTheme.accentColor1;
        Render2DUtil.drawRoundedRect(f5, f3, 1.5F, 6.0F, 0.75F, 1.0F, ColorUtil.withAlpha(color, 150));
        String s = this.truncateToWidth(communitypost.editor() + ": " + communitypost.editorRole(), 7.5F, f2 - 24.0F);
        float f6 = f + 12.0F;
        float f4 = f1 + 1.0F;
        this.drawText(s, f6, f4, 7.5F, TEXT_SECONDARY.getRGB());
    }

    private void drawAvatar(CommunityPost communitypost, float f, float f1) {
        TextureRectangleRenderer texturerectanglerenderer = Render2DUtil.textureRenderer;
        GlTexture gltexture = TextureCache.getTexture(communitypost.getAvatarKey());
        texturerectanglerenderer.render(gltexture, f, f1, 16.0F, 16.0F, 8.0F, 1.0F);
        float f3 = f - 0.4F;
        float f2 = f1 - 0.4F;
        Color color = communitypost.getRoleColor();
        Render2DUtil.drawOutline(f3, f2, 16.8F, 16.8F, 8.4F, 0.3F, 0.4F, ColorUtil.withAlpha(color, 190));
    }

    private float drawMessageText(float f) {
        return f + 50.0F + 2.0F;
    }

    private void drawInputField(float f, float f1, int i, int j) {
        float f2 = this.getChatHeight();
        float f3 = this.getInputWidth();
        boolean muted = IrcState.isMuted();
        if (this.noticeStartTime > 0L) {
            this.drawReplyPreview(f, f1, f2 - 14.0F, i, j);
        }

        this.inputAnimation.setTarget(this.inputFocused && !muted ? 1.0 : 0.0);
        this.inputAnimation.update();
        float focus = this.inputAnimation.getFloat();
        if (GuiTheme.isBlurMode()) {
            Render2DUtil.drawBlurRect(f, f2, f1, f3, 7.0F, 14.0F);
        } else {
            Render2DUtil.drawRoundedRect(f, f2, f1, f3, 7.0F, 1.0F, ColorUtil.withAlpha(GuiTheme.moduleColor, 200));
        }

        Render2DUtil.drawOutline(f, f2, f1, f3, 7.0F, 0.3F, 0.6F, Color.black);
        if (focus > 0.01F) {
            Render2DUtil.drawOutline(f, f2, f1, f3, 7.0F, 0.3F, 0.6F * focus, ColorUtil.withAlpha(GuiTheme.accentColor1, (int)(255.0F * focus)));
        }

        float f5 = f2 + (f3 - VoilClient.fonts.getSelectedFont().getLineHeight(9.5F)) / 2.0F;
        float f6 = f + 10.0F;
        if (muted) {
            VoilClient.fonts.getSelectedFont().render("You are muted", f6, f5, 9.5F, COLOR_ERROR.getRGB());
            return;
        }

        String visibleText = this.formatCount(f1 - 40.0F);
        RenderManager.setScissor((int)(f + 4.0F), (int)f2, (int)(f + f1 - 26.0F), (int)(f2 + f3));
        if (this.inputText.isEmpty()) {
            VoilClient.fonts.getSelectedFont().render("Message everyone...", f6, f5, 9.5F, TEXT_PLACEHOLDER.getRGB());
        } else {
            this.drawText(visibleText, f6, f5, 9.5F, WHITE_RGB);
        }

        this.drawReactionPicker(f6 + this.getTextWidth(visibleText, 9.5F), f5);
        this.drawScrollbar(f, f1, f5);
        this.drawReactionBadge(f, f1, f2, f3, i, j);
        RenderManager.disableScissor();
    }

    private void drawChatPanel(float f, float f1) {
        int k = this.screen.windowY;
        float f2 = k + 9;
        VoilClient.fonts.getInterBold().render("IRC", f, f2, 12.0F, -1);
        if (IrcState.isMuted()) {
            float f4 = this.getTextWidth("You are muted", 9.0F);
            float f18 = f + f1 - f4 - 10.0F;
            float f20 = f4 + 8.0F;
            Color color = COLOR_ERROR;
            Render2DUtil.drawRoundedRect(f18, f2, f20, 13.0F, 3.0F, 1.0F, ColorUtil.withAlpha(color, 45));
            VoilClient.fonts.getSelectedFont().render("You are muted", f + f1 - f4 - 6.0F, f2 + 2.0F, 9.0F, COLOR_ERROR.getRGB());
        }

        float f11 = this.screen.windowY + 25;
        Color color1 = GuiTheme.accentColor1;
        Color color2 = ColorUtil.withAlpha(color1, 60);
        float f12 = 1.0F;
        float f13 = 0.25F;
        float f14 = 0.5F;
        float f15 = f1;
        float f16 = f11;
        float f17 = f;
        Render2DUtil.drawRoundedRect(f17, f16, f15, f14, f13, f12, color2);
    }

    private void drawConfigCard(CommunityPost communitypost, float f, float f1, float f2, int i) {
        Color color = GuiTheme.moduleColor;
        Render2DUtil.drawRoundedRect(f, f1, f2, 45.0F, 6.0F, 1.0F, ColorUtil.withAlpha(color, 210));
        Color color1 = GuiTheme.accentColor1;
        Render2DUtil.drawOutline(f, f1, f2, 45.0F, 6.0F, 0.3F, 0.5F, ColorUtil.withAlpha(color1, 120));
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE0D0(), f + 8.0F, f1 + 8.0F, 12.0F, GuiTheme.accentColor1.getRGB());
        VoilClient.fonts.getInterBold().render(communitypost.getDataString(), f + 26.0F, f1 + 6.0F, 10.0F, -1);
        String details = communitypost.getDataString();
        String s = details + " · " + details + " · " + details;
        VoilClient.fonts.getGreyscale().render(s, f + 26.0F, f1 + 18.0F, 8.5F, TEXT_MUTED.getRGB());
        int votes = communitypost.getDataInt();
        String s1 = "+" + votes + "  -" + votes;
        float f3 = VoilClient.fonts.getGreyscale().getWidth(s1, 8.5F);
        VoilClient.fonts.getGreyscale().render(s1, f + f2 - f3 - 8.0F, f1 + 6.0F, 8.5F, TEXT_MUTED.getRGB());
        String s2 = communitypost.getDataString();
        this.drawButton("Apply Config", f + 26.0F, f1 + 30.0F, 74.0F, 12.0F, i, GuiTheme.accentColor1, () -> ApiClient.INSTANCE.getConfig(s2));
    }

    private void drawCardButton(CommunityPost communitypost, float f, float f1, int i) {
        boolean flag = this.isInside(i, f);
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE1E4(), f, f1, 9.0F, (flag ? Color.WHITE : TEXT_MUTED).getRGB());
        this.drawHoverButton(f - 2.0F, f1 - 2.0F, 13.0F, 13.0F, () -> {
            this.cursorBlinkTime = communitypost.id();
            this.markConnecting();
        });
        boolean flag1 = this.isInside(i, f + 16.0F);
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE04C(), f + 16.0F, f1, 9.0F, (flag1 ? Color.WHITE : TEXT_MUTED).getRGB());
        this.drawHoverButton(f + 14.0F, f1 - 2.0F, 13.0F, 13.0F, () -> {
            this.noticeStartTime = communitypost.id();
            this.noticeText = communitypost.getDisplayAuthorId();
            this.inputFocused = true;
        });
    }

    private List<String> wrapText(String s, float f) {
        List<String> lines = new ArrayList<>();
        StringBuilder stringbuilder = new StringBuilder();

        for (String s2 : s.split(" ")) {
            String s3 = stringbuilder.isEmpty() ? s2 : stringbuilder + " " + s2;
            if (this.getTextWidth(s3, 9.5F) <= f) {
                stringbuilder.setLength(0);
                stringbuilder.append(s3);
                continue;
            }

            if (!stringbuilder.isEmpty()) {
                lines.add(stringbuilder.toString());
            }

            stringbuilder.setLength(0);

            while (this.getTextWidth(s2, 9.5F) > f && s2.length() > 1) {
                int k = this.getCursorIndexAt(s2, f);
                lines.add(s2.substring(0, k));
                s2 = s2.substring(k);
            }

            stringbuilder.append(s2);
        }

        if (!stringbuilder.isEmpty()) {
            lines.add(stringbuilder.toString());
        }

        if (lines.isEmpty()) {
            lines.add("");
        }

        return lines;
    }

    private void drawLinkButton(CommunityPost communitypost, float f, float f1, String s) {
        float f2 = f + 16.0F + 7.0F;
        String s2 = communitypost.getDisplayAuthorId();
        Color color = communitypost.authorId().equals(s) ? GuiTheme.accentColor1 : communitypost.getRoleColor();
        VoilClient.fonts.getInterBold().render(s2, f2, f1, 9.5F, color.getRGB());
        float f3 = f2 + VoilClient.fonts.getInterBold().getWidth(s2, 9.5F) + 5.0F;
        if (communitypost.isDiscordUser()) {
            float f4 = VoilClient.fonts.getSelectedFont().getWidth("discord", 7.5F);
            float f5 = f1 + 1.0F;
            float f6 = f4 + 6.0F;
            Color color1 = COLOR_ACCENT;
            Render2DUtil.drawRoundedRect(f3, f5, f6, 9.0F, 2.5F, 1.0F, ColorUtil.withAlpha(color1, 70));
            VoilClient.fonts.getSelectedFont().render("discord", f3 + 3.0F, f1 + 1.5F, 7.5F, COLOR_ACCENT_LIGHT.getRGB());
            f3 += f4 + 11.0F;
        }

        VoilClient.fonts.getGreyscale().render(formatTimeAgo(communitypost.createdAt()), f3, f1 + 1.5F, 8.0F, TEXT_PLACEHOLDER.getRGB());
    }

    private boolean isPrintable(int i) {
        switch (i) {
            case GLFW.GLFW_KEY_TAB:
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER:
                this.sendMessage();
                return true;
            case GLFW.GLFW_KEY_DOWN:
                this.cursorPos = (this.cursorPos + 1) % this.renderedLines.size();
                return true;
            case GLFW.GLFW_KEY_UP:
                this.cursorPos = (this.cursorPos - 1 + this.renderedLines.size()) % this.renderedLines.size();
                return true;
            default:
                return false;
        }
    }

    private void joinServer(String s) {
        MinecraftClient minecraftclient = MinecraftClient.getInstance();
        minecraftclient.execute(() -> {
            minecraftclient.world.disconnect(Text.of("Connecting to " + s));
            minecraftclient.disconnect(Text.of("Connecting to " + s));
            ServerInfo serverinfo = new ServerInfo(s, s, ServerType.OTHER);
            ConnectScreen.connect(new TitleScreen(), minecraftclient, ServerAddress.parse(s), serverinfo, false, null);
        });
    }

    private void refreshServers() {
        this.noticeStartTime = 0L;
        this.noticeText = "";
    }

    private int getCursorIndexAt(String s, float f) {
        int i = 1;
        int j = s.length();

        while (i < j) {
            int k = i + j + 1 >>> 1;
            if (k < s.length() && Character.isLowSurrogate(s.charAt(k))) {
                k--;
            }

            if (k <= i) {
                break;
            }

            if (this.getTextWidth(s.substring(0, k), 9.5F) <= f) {
                i = k;
            } else {
                j = k - 1;
            }
        }

        int l = Math.max(1, i);
        if (l < s.length() && Character.isLowSurrogate(s.charAt(l))) {
            l++;
        }

        return l;
    }

    private void loadServers() {
        if (!this.inputText.isEmpty()) {
            int last = Character.charCount(this.inputText.codePointBefore(this.inputText.length()));
            this.inputText = this.inputText.substring(0, this.inputText.length() - last);
            this.pollServers();
        } else if (this.noticeStartTime > 0L) {
            this.refreshServers();
        }
    }

    private void drawShareButton(CloudConfig cloudconfig, float f, float f1, float f2, float f3, int i) {
        boolean hovered = i >= f + 6.0F && i <= f + f2 - 6.0F;
        float f12 = f + 6.0F;
        float f14 = f2 - 12.0F;
        float f15 = f3 - 3.0F;
        Color color3;
        if (hovered) {
            Color color1 = GuiTheme.accentColor1;
            color3 = ColorUtil.withAlpha(color1, 70);
        } else {
            Color color2 = Color.black;
            color3 = ColorUtil.withAlpha(color2, 55);
        }

        Color color = color3;
        float f4 = 1.0F;
        float f5 = 5.0F;
        float f6 = f15;
        float f7 = f14;
        float f8 = f1;
        Render2DUtil.drawRoundedRect(f12, f8, f7, f6, f5, f4, color);
        VoilClient.fonts.getInterBold().render(cloudconfig.getAuthor(), f + 14.0F, f1 + 4.0F, 9.5F, -1);
        String s4 = cloudconfig.getUpdatedAt();
        String s2 = cloudconfig.getCreatedAt();
        String s = s2 + " · " + s4;
        VoilClient.fonts.getGreyscale().render(s, f + 14.0F, f1 + 15.0F, 8.0F, TEXT_MUTED.getRGB());
        float f9 = VoilClient.fonts.getSelectedFont().getWidth("Share", 8.5F);
        VoilClient.fonts.getSelectedFont().render("Share", f + f2 - f9 - 14.0F, f1 + 9.0F, 8.5F, (hovered ? Color.WHITE : GuiTheme.accentColor1).getRGB());
        String s1 = cloudconfig.getFileName();
        this.drawHoverButton(f12, f1, f14, f15, () -> {
            ApiClient.INSTANCE.shareConfig(s1);
            this.showPicker = false;
        });
    }

    private void drawServerRow(CommunityPost communitypost, float f, float f1) {
        Map<String, Integer> map = IrcState.getReactions(communitypost.id());
        boolean picking = this.cursorBlinkTime == communitypost.id();
        if (map.isEmpty() && !picking) {
            return;
        }

        for (Entry<String, Integer> entry : map.entrySet()) {
            String s1 = entry.getKey();
            String s2 = String.valueOf(entry.getValue());
            float f7 = this.getTextWidth(s1, 12.0F);
            float f8 = f7 + this.getTextWidth(s2, 8.5F) + 11.0F;
            boolean reacted = IrcState.hasReacted(communitypost.id(), s1);
            Color color5;
            if (reacted) {
                Color color1 = GuiTheme.accentColor1;
                color5 = ColorUtil.withAlpha(color1, 90);
            } else {
                Color color2 = Color.black;
                color5 = ColorUtil.withAlpha(color2, 70);
            }

            Color color = color5;
            float f2 = 1.0F;
            float f3 = 4.0F;
            float f4 = 12.0F;
            float f5 = f8;
            float f6 = f1;
            Render2DUtil.drawRoundedRect(f, f6, f5, f4, f3, f2, color);
            if (reacted) {
                Color color3 = GuiTheme.accentColor1;
                Render2DUtil.drawOutline(f, f1, f8, 12.0F, 4.0F, 0.3F, 0.4F, ColorUtil.withAlpha(color3, 200));
            }

            float f14 = f + 3.0F;
            float f9 = f1 - 3.5F;
            this.drawText(s1, f14, f9, 12.0F, TEXT_PRIMARY_RGB);
            float f12 = f + f7 + 7.0F;
            float f15 = f1 + 0.5F;
            int i = reacted ? GuiTheme.accentColor1.getRGB() : TEXT_SECONDARY.getRGB();
            this.drawText(s2, f12, f15, 8.5F, i);
            this.drawHoverButton(f, f1, f8, 12.0F, () -> {
                ApiClient apiclient = ApiClient.INSTANCE;
                long i1 = communitypost.id();
                apiclient.reactToMessage(i1, s1);
            });
            f += f8 + 3.0F;
        }

        if (!picking) {
            return;
        }

        for (String s3 : STRING_TABLE) {
            float f11 = this.getTextWidth(s3, 12.0F) + 6.0F;
            Color color4 = GuiTheme.moduleColor;
            Render2DUtil.drawRoundedRect(f, f1, f11, 12.0F, 4.0F, 1.0F, ColorUtil.withAlpha(color4, 235));
            float f13 = f + 3.0F;
            float f10 = f1 - 3.0F;
            this.drawText(s3, f13, f10, 12.0F, WHITE_RGB);
            this.drawHoverButton(f, f1, f11, 12.0F, () -> {
                ApiClient apiclient = ApiClient.INSTANCE;
                long i1 = communitypost.id();
                apiclient.reactToMessage(i1, s3);
                this.cursorBlinkTime = 0L;
            });
            f += f11 + 2.0F;
        }
    }

    private void drawRoundedPanel(float f, float f1, float f2, float f3, double d0) {
        float f4 = Math.max(18.0F, f2 * (f2 / f3));
        float f5 = d0 <= 0.0 ? 1.0F : (float)(1.0 - this.mouseX / d0);
        float f6 = f1 + (f2 - f4) * f5;
        Color color = Color.black;
        Render2DUtil.drawRoundedRect(f, f1, 2.0F, f2, 1.0F, 1.0F, ColorUtil.withAlpha(color, 70));
        Color color1 = GuiTheme.accentColor1;
        Render2DUtil.drawRoundedRect(f, f6, 2.0F, f4, 1.0F, 1.0F, ColorUtil.withAlpha(color1, 170));
    }

    private float getInputWidth() {
        return 24.0F;
    }

    private void drawReactionBadge(float f, float f1, float f2, float f3, int i, int j) {
        boolean flag = this.inputText.isBlank();
        double d0 = i;
        boolean flag1 = this.isOverMessage(d0, j);
        double d1 = flag1 ? 1.0 : 0.0;
        this.pickerAnimation.setTarget(d1);
        this.pickerAnimation.update();
        Color color = flag ? ColorUtil.lerpColor(GuiTheme.accentColor1, Color.WHITE, this.pickerAnimation.getFloat()) : TEXT_DISABLED;
        Render2DUtil.drawArrow(f + f1 - 20.0F, f2 + f3 / 2.0F - 4.0F, 8.0F, 8.0F, 5.0F, 0.5F, 1.0F, 40.0F, 0.0F, 1.0F, color);
    }

    private static boolean isControlDown(int i) {
        return (i & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    private boolean isOverMessage(double d0, double d1) {
        float f = this.getChatX();
        float f1 = this.getChatWidth();
        float f2 = this.getChatHeight();
        return d0 >= f + f1 - 22.0F && d0 <= f + f1 - 6.0F && d1 >= f2 + 5.0F && d1 <= f2 + this.getInputWidth() - 5.0F;
    }

    private List<PostCard> getVisibleMessages(float f) {
        if (!this.connecting && f == this.scrollOffset && this.cursorBlinkTime == this.lastMessageTime) {
            return this.servers;
        }

        List<PostCard> cards = new ArrayList<>();

        for (CommunityPost post : IrcState.getMessages()) {
            String text = post.authorName();
            if (text.length() > 4000) {
                text = text.substring(0, 4000) + "…";
            }

            List<String> lines = this.wrapText(text, f);
            float attachmentHeight = 0.0F;
            if (post.isConfigPost()) {
                attachmentHeight = 49.0F;
            } else if (post.isServerPost()) {
                attachmentHeight = 54.0F;
            }

            float editorHeight = post.isEdited() ? 12.0F : 0.0F;
            boolean hasReactionRow = !IrcState.getReactions(post.id()).isEmpty() || this.cursorBlinkTime == post.id();
            float reactionHeight = hasReactionRow ? 14.0F : 0.0F;
            cards.add(new PostCard(post, lines, editorHeight + 11.0F + 5.0F + attachmentHeight + reactionHeight));
        }

        this.servers = cards;
        this.scrollOffset = f;
        this.lastMessageTime = this.cursorBlinkTime;
        this.connecting = false;
        return this.servers;
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1, i);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }

    @Override
    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged();
    }

    @Override
    public void mouseScrolled2(double d0, double d1, double d2, double d3) {
        this.mouseScrolled$1();
    }

    @Override
    public void keyReleased2(int i, int j, int k) {
        this.keyPressed(i, k);
    }
}
