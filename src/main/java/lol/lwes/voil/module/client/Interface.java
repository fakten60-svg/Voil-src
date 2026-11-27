package lol.lwes.voil.module.client;

import java.awt.Color;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.client.ChatEntry;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.UiAnimation;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

public class Interface extends Module {
    public MultiSelectSetting changes;
    public BooleanSetting glow;
    public BooleanSetting hotbarKeys;
    private final UiAnimation selectorAnim;
    private boolean selectorInitialized;
    private final List chatMessages;
    private final UiAnimation chatScrollAnim;
    private int chatScroll;
    private final UiAnimation chatOpenAnim;
    private boolean chatOpen;
    private final List chatSuggestions;
    private String chatInput;

    public Interface() {
        super("Interface", "Customizes the interface", Category.Client);
        String[] astring = new String[]{"Hotbar"};
        this.changes = new MultiSelectSetting("Changes", "What to change", astring);
        this.glow = new BooleanSetting("Glow", true, "Adds a glow effect to interface elements");
        this.hotbarKeys = new BooleanSetting("Hotbar Keys", true, "Shows the hotbar keys above the hotbar");
        this.selectorAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.selectorInitialized = false;
        this.chatMessages = new java.util.ArrayList();
        this.chatScrollAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.chatScroll = 0;
        this.chatOpenAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.chatOpen = false;
        this.chatSuggestions = new java.util.ArrayList();
        this.chatInput = "";
        Setting[] asetting = new Setting[]{this.changes, this.hotbarKeys, this.glow};
        this.addSettings(asetting);
    }

    public void setChatOpen(boolean flag) {
        this.chatOpen = flag;
        double d0 = flag ? 1.0 : 0.0;
        this.chatOpenAnim.setTarget(d0);
        if (!flag) {
            this.chatSuggestions.clear();
            this.chatInput = "";
        }
    }

    public void scrollChat(int i) {
        int k = this.chatMessages.size();
        int j = Math.max(0, k + -11 + 1);
        k = this.chatScroll;
        this.chatScroll = MathHelper.clamp(k + i, 0, j);
    }

    public void renderHotbar() {
        boolean flag = Boolean.FALSE;
        int i = mc.getWindow().getScaledWidth();
        int j = mc.getWindow().getScaledHeight();
        int k = i / 2 - 91;
        int l = j - 21;
        int k1 = mc.player.getInventory().getSelectedSlot();
        float f = k + k1 * 20;
        if (!this.selectorInitialized) {
            this.selectorAnim.snapTo(f);
            this.selectorInitialized = true;
        }

        double d0 = f;
        this.selectorAnim.setTarget(d0);
        this.selectorAnim.update();
        float f8 = k + -3 + 1;
        float f3 = l - 2;
        float f4 = f8;
        Render2DUtil.drawBlurRectEx(f4, f3, 184.0F, 22.0F, 3.0F, 14.0F, 0.0F);
        if (this.glow.isEnabled()) {
            Render2DUtil.drawGlowGradient(
                k + -3 + 1,
                l + -3 + 1,
                184.0F,
                22.0F,
                3.0F,
                1.0F,
                28.0F,
                35.0F,
                GuiTheme.accentColor1,
                GuiTheme.accentColor1,
                GuiTheme.accentColor2,
                GuiTheme.accentColor2
            );
        }

        f8 = this.selectorAnim.getFloat();
        float f5 = l - 1;
        Color color = Color.BLACK;
        Render2DUtil.drawRoundedRect(f8, f5, 20.0F, 20.0F, 3.0F, 1.0F, ColorUtil.withAlpha(color, 80));

        for (int i1 = 0; i1 < 9; i1++) {
            int l1 = i1 * 20;
            k1 = k;
            int j1 = k1 + l1;
            ItemStack itemstack = mc.player.getInventory().getStack(i1);
            if (!flag) {
                Render2DUtil.drawItem(itemstack, j1 + 2, l + 2, 1.0F, true);
            }

            String s = getHotbarKeyLabel(i1);
            float f1 = j1 + 2;
            float f2 = l + 10;
            if (this.hotbarKeys.isEnabled()) {
                VoilClient.fonts.getGreyscale().render(s, f1, f2, 8.0F, Color.white.getRGB());
            }
        }

        ItemStack itemstack1 = mc.player.getOffHandStack();
        if (!itemstack1.isEmpty()) {
            boolean flag1 = mc.options.getMainArm().getValue() == Arm.RIGHT;
            int j2;
            if (flag1) {
                j2 = k + -30 + 1;
            } else {
                k1 = k + 184;
                j2 = k1 + 5;
            }

            int i2 = j2;
            f8 = i2 + -3 + 1;
            float f6 = l + -3 + 1;
            float f7 = f8;
            Render2DUtil.drawBlurRectEx(f7, f6, 22.0F, 22.0F, 3.0F, 14.0F, 0.0F);
            if (!flag) {
                Render2DUtil.drawItem(itemstack1, i2, l, 1.0F, true);
            }
        }
    }

    public void addChatMessage(Text text, MessageIndicator messageindicator) {
        boolean flag = messageindicator != null && messageindicator != MessageIndicator.system();
        this.chatMessages.add(0, new ChatEntry(text, flag));

        while (this.chatMessages.size() > 100) {
            List list = this.chatMessages;
            int i = this.chatMessages.size();
            list.remove(i - 1);
        }
    }

    public void clearChat() {
        this.chatMessages.clear();
        this.chatScroll = 0;
    }

    private static String getHotbarKeyLabel(int i) {
        KeyBinding keybinding = mc.options.hotbarKeys[i];
        String s = keybinding.getBoundKeyTranslationKey();
        String s4;
        switch (s) {
            case "key.mouse.left" -> s4 = "LMB";
            case "key.mouse.right" -> s4 = "RMB";
            case "key.mouse.middle" -> s4 = "MMB";
            default -> {
                if (s.startsWith("key.mouse.")) {
                    String s3 = s.substring(10);
                    s4 = "MB" + s3;
                } else {
                    String keyName = keybinding.getBoundKeyLocalizedText().getString().toUpperCase();
                    switch (keyName) {
                        case "LEFT SHIFT":
                            s4 = "LSHIFT";
                            break;
                        case "RIGHT SHIFT":
                            s4 = "RSHIFT";
                            break;
                        case "LEFT CONTROL":
                            s4 = "LCTRL";
                            break;
                        case "RIGHT CONTROL":
                            s4 = "RCTRL";
                            break;
                        case "LEFT ALT":
                            s4 = "LALT";
                            break;
                        case "RIGHT ALT":
                            s4 = "RALT";
                            break;
                        case "CAPS LOCK":
                            s4 = "CAPS";
                            break;
                        case "SPACE":
                            s4 = "SPC";
                            break;
                        case "ENTER":
                            s4 = "ENT";
                            break;
                        case "BACKSPACE":
                            s4 = "BKSP";
                            break;
                        case "ESCAPE":
                            s4 = "ESC";
                            break;
                        default:
                            s4 = keyName;
                    }
                }
            }
        }

        return s4;
    }

    private void drawScrollbar(int i, int j, int k, int l, int i1) {
        float f6 = i;
        float f7 = j;
        float f2 = k;
        float f3 = 6.0F;
        float f4 = f7;
        float f5 = f6;
        Render2DUtil.drawBlurRect(f5, f4, f3, f2, 2.0F, 8.0F);
        float f = 10.0F / l;
        int j1 = Math.max(20, (int)(k * f));
        float f1 = (float)i1 / Math.max(1, l + -11 + 1);
        int i2 = (int)((k - j1) * (1.0F - f1));
        int l1 = j;
        int k1 = l1 + i2;
        Render2DUtil.drawBlurRoundedTinted(i, k1, 6.0F, j1, 2.0F, 10.0F, 1.0F, Color.WHITE);
    }
}
