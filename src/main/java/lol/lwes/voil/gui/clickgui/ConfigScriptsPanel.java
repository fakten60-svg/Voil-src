package lol.lwes.voil.gui.clickgui;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.api.ApiClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.CloudConfig;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ConfigAction;
import lol.lwes.voil.gui.clickgui.ConfigButton;
import lol.lwes.voil.gui.clickgui.dialog.CreateConfigDialog;
import lol.lwes.voil.gui.clickgui.dialog.CreateScriptDialog;
import lol.lwes.voil.gui.clickgui.dialog.ScriptDetailsDialog;
import lol.lwes.voil.script.ScriptManager;
import lol.lwes.voil.util.font.IconGlyphs;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.UiRect;
import net.minecraft.client.util.math.MatrixStack;

public class ConfigScriptsPanel extends ClickGuiPanel {
    public final ClickGuiScreen screen;
    private final String[] tabNames = new String[]{"Configs", "Scripts"};
    private final String[] visibilityNames = new String[]{"Public", "Private"};
    private String currentTab = this.tabNames[0];
    private String currentVisibility = this.visibilityNames[0];
    public ScriptDetailsDialog scriptDetailsDialog = null;
    private CreateConfigDialog createConfigDialog = null;
    private CreateScriptDialog createScriptDialog = null;
    private ConfigEntryInfo selectedInfo = null;
    private CloudConfig selectedEntry = null;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private String inputText = "";
    private boolean creating = false;
    private final List<UiRect> configs = new ArrayList<>();
    private final List<ConfigButton> scripts = new ArrayList<>();
    private final Map<String, Integer> userVotes = new HashMap<>();
    private UiRect bounds = new UiRect(0, 0, 0, 0);

    public ConfigScriptsPanel(ClickGuiScreen clickguiscreen) {
        super(clickguiscreen);
        this.screen = clickguiscreen;
        ClientSettings.cloudConfigs.values().clear();
        ApiClient apiclient = ApiClient.INSTANCE;
        boolean flag = this.currentVisibility.equals("Private");
        apiclient.getAllConfigs(flag);
        ClientSettings.cloudScripts.values().clear();
    }

    private void reloadList() {
        ClientSettings.cloudConfigs.values().clear();
        ApiClient apiclient = ApiClient.INSTANCE;
        boolean flag = this.currentVisibility.equals("Private");
        apiclient.getAllConfigs(flag);
    }

    public void clearSelection() {
        this.createConfigDialog = null;
        this.createScriptDialog = null;
        this.scriptDetailsDialog = null;
    }

    public void mouseClicked(double d0, double d1, int i) {
        if (this.createScriptDialog != null) {
            CreateScriptDialog createscriptdialog = this.createScriptDialog;
            createscriptdialog.mouseClicked(d0, d1, i);
        } else if (this.scriptDetailsDialog != null) {
            ScriptDetailsDialog scriptdetailsdialog = this.scriptDetailsDialog;
            scriptdetailsdialog.mouseClicked(d0, d1, i);
        } else if (this.createConfigDialog != null) {
            CreateConfigDialog createconfigdialog = this.createConfigDialog;
            createconfigdialog.mouseClicked(d0, d1, i);
        } else {
            if (i == 0 && this.currentTab.equals("Configs")) {
                int i4 = (int)d0;
                int j2 = (int)d1;
                if (this.bounds.contains(i4, j2)) {
                    for (ConfigButton configbutton : this.scripts) {
                        if (configbutton.contains(d0, d1)) {
                            String s = configbutton.getConfig().getFileName().replace(".json", "");
                            switch (configbutton.getAction()) {
                                case UPVOTE:
                                    this.userVotes.put(s, this.userVotes.getOrDefault(s, 0) == 1 ? 0 : 1);
                                    ApiClient apiclient5 = ApiClient.INSTANCE;
                                    apiclient5.upvoteConfig(s);
                                    break;
                                case DOWNVOTE:
                                    this.userVotes.put(s, this.userVotes.getOrDefault(s, 0) == -1 ? 0 : -1);
                                    ApiClient apiclient4 = ApiClient.INSTANCE;
                                    apiclient4.downvoteConfig(s);
                                    break;
                                case DOWNLOAD:
                                    ApiClient apiclient = ApiClient.INSTANCE;
                                    apiclient.getConfig(s);
                                    break;
                                case UPDATE:
                                    ApiClient apiclient7 = ApiClient.INSTANCE;
                                    CloudConfig cloudconfig = configbutton.getConfig();
                                    ApiClient apiclient3 = apiclient7;
                                    apiclient3.updateConfig(cloudconfig);
                                    this.reloadList();
                                    break;
                                case DELETE:
                                    if (this.selectedEntry == configbutton.getConfig()) {
                                        ApiClient.INSTANCE.deleteConfig(s);
                                        this.selectedEntry = null;
                                        this.reloadList();
                                    } else {
                                        this.selectedEntry = configbutton.getConfig();
                                    }
                                    break;
                                case CANCEL:
                                    this.selectedEntry = null;
                            }

                            return;
                        }
                    }

                    this.selectedEntry = null;
                }
            }

            int i1 = this.screen.windowX;
            int k3 = i1 + 180;
            int j1 = 223 / 2;
            i1 = k3;
            int j = i1 + j1;
            int k = j - 125;
            i1 = this.screen.windowY;
            int l = i1 + 55;
            float f = k - 25;
            float f1 = l - this.scrollOffset;
            this.creating = d0 >= f && d0 <= f + 365.0F && d1 >= f1 && d1 <= f1 + 15.0F;
            if (i == 0 && this.currentTab.equals("Configs")) {
                float f2 = VoilClient.fonts.getSelectedFont().getWidth("Create New");
                float f3 = VoilClient.fonts.getSelectedFont().getLineHeight();
                float f4 = j + 60 / 2.0F - (f2 - 200.0F);
                float f5 = this.screen.windowY + 20.0F;
                float f6 = 10.0F;
                float f7 = 12.0F;
                float f8 = f2 + f6 * 2.0F;
                if (d0 >= f4 - f6 && d0 <= f4 - f6 + f8 && d1 >= f5 - f7 && d1 <= f5 + f3 + f7) {
                    this.createConfigDialog = new CreateConfigDialog(this);
                }
            }

            if (i == 0 && this.currentTab.equals("Scripts")) {
                float f11 = j + 60 / 2.0F - (VoilClient.fonts.getSelectedFont().getWidth("Create New") - 200.0F);
                float f14 = this.screen.windowY + 25.0F;
                float f17 = VoilClient.fonts.getSelectedFont().getWidth("Create New") + 5.0F;
                float f18 = VoilClient.fonts.getSelectedFont().getLineHeight() / 2.0F;
                if (d0 >= f11 - 2.5F && d0 <= f11 - 2.5F + f17 && d1 >= f14 && d1 <= f14 + f18) {
                    this.createScriptDialog = new CreateScriptDialog(this);
                }
            }

            if (i == 0) {
                float f12 = 0.0F;
                float f15 = 20.0F;
                i1 = this.screen.windowY;
                int i3 = i1 + 25;

                for (String s1 : this.tabNames) {
                    float f9 = j + 60 / 2.0F - (VoilClient.fonts.getSelectedFont().getWidth(s1) + 135.0F) + f12;
                    float f10 = VoilClient.fonts.getSelectedFont().getWidth(s1) + 10.0F;
                    if (d0 >= f9 - 5.0F && d0 <= f9 + f10 + 5.0F && d1 >= i3 && d1 <= i3 + 15) {
                        this.currentTab = s1;
                    }

                    f12 += VoilClient.fonts.getSelectedFont().getWidth(s1) + f15;
                }
            }

            if (i == 0) {
                float f13 = 0.0F;
                float f16 = 20.0F;
                i1 = this.screen.windowY;
                int j3 = i1 + 25;

                for (String s7 : this.tabNames) {
                    f13 += VoilClient.fonts.getSelectedFont().getWidth(s7) + f16;
                }

                f13 += 55.0F;

                for (String s8 : this.visibilityNames) {
                    float f19 = j + 60 / 2.0F - (VoilClient.fonts.getSelectedFont().getWidth(s8) + 135.0F) + f13;
                    float f20 = VoilClient.fonts.getSelectedFont().getWidth(s8) + 10.0F;
                    if (d0 >= f19 - 5.0F && d0 <= f19 + f20 + 5.0F && d1 >= j3 && d1 <= j3 + 15) {
                        this.currentVisibility = s8;
                        this.selectedEntry = null;
                        ClientSettings.cloudConfigs.values().clear();
                        ApiClient apiclient6 = ApiClient.INSTANCE;
                        boolean flag = this.currentVisibility.equals("Private");
                        ApiClient apiclient2 = apiclient6;
                        apiclient2.getAllConfigs(flag);
                        return;
                    }

                    f13 += VoilClient.fonts.getSelectedFont().getWidth(s8) + f16;
                }
            }

            int l3 = (int)d0;
            int k2 = (int)d1;
            if (this.bounds.contains(l3, k2)) {
                for (UiRect uirect : this.configs) {
                    l3 = (int)d0;
                    int l2 = (int)d1;
                    if (uirect.contains(l3, l2) && uirect.source != null) {
                        if (i == 1) {
                            this.scriptDetailsDialog = new ScriptDetailsDialog(this, uirect.source);
                        } else {
                            ScriptManager scriptmanager = VoilClient.getScriptManager();
                            String s6 = uirect.source.getName();
                            if (!scriptmanager.getScripts().containsKey(s6)) {
                                String s2 = uirect.source.getId();
                                String s3 = s6;
                                scriptmanager.addScript(s3, s2);
                            } else {
                                String s5 = uirect.source.getId();
                                String s4 = s6;
                                scriptmanager.reloadScript(s4, s5);
                            }

                            if (scriptmanager.isScriptEnabled(s6)) {
                                scriptmanager.disableScript(s6);
                            } else {
                                scriptmanager.enableScript(s6);
                                scriptmanager.runScript(s6);
                            }
                        }
                    }
                }
            }
        }
    }

    public void render(MatrixStack matrixstack, int i, int j, float f) {
        if (this.createScriptDialog != null) {
            CreateScriptDialog createscriptdialog = this.createScriptDialog;
            createscriptdialog.render(i, j, f);
        } else if (this.scriptDetailsDialog != null) {
            ScriptDetailsDialog scriptdetailsdialog = this.scriptDetailsDialog;
            scriptdetailsdialog.render(i, j, f);
        } else if (this.createConfigDialog != null) {
            CreateConfigDialog createconfigdialog = this.createConfigDialog;
            createconfigdialog.render(i, j, f);
        } else {
            int k = this.screen.windowX;
            int l = this.screen.windowY;
            int i1 = (int)VoilClient.fonts.getSelectedFont().getWidth("Choose and load configs or scripts!");
            MSDFFontAtlas msdffontatlas = VoilClient.fonts.getSelectedFont();
            int j8 = k + 180;
            int l3 = (320 - i1) / 2;
            int k3 = j8;
            msdffontatlas.render("Choose and load configs or scripts!", k3 + l3, l + 7, -1);
            float f23 = k + 138;
            float f31 = l + 24;
            l3 = this.screen.sidebarWidth;
            k3 = this.screen.windowWidth;
            float f13 = k3 - l3;
            Render2DUtil.drawFadeLineHorizontal(f23, f31, f13, 0.8F, GuiTheme.accentColor1);
            int j1 = k + 180 + 111;
            float f1 = j1 + 60 / 2.0F - (VoilClient.fonts.getSelectedFont().getWidth("Create New") - 185.0F);
            float f24 = f1 - 2.5F;
            float f14 = l + 30;
            Render2DUtil.drawRoundedRect(
                f24,
                f14,
                VoilClient.fonts.getSelectedFont().getWidth("Create New"),
                VoilClient.fonts.getSelectedFont().getLineHeight(),
                3.0F,
                1.0F,
                GuiTheme.accentColor1
            );
            VoilClient.fonts.getSelectedFont().render("Create New", f1, l + 30, 10.0F, -1);
            float f2 = 0.0F;

            for (String s : this.tabNames) {
                float f3 = j1 + 60 / 2.0F - (VoilClient.fonts.getSelectedFont().getWidth(s) + 135.0F) + f2;
                f31 = f3 - 2.0F;
                float f28 = l + 30;
                boolean flag1 = s.equals(this.currentTab);
                Render2DUtil.drawText(s, f31, f28, flag1);
                f2 += VoilClient.fonts.getSelectedFont().getWidth(s) + 20.0F;
            }

            int k5 = (int)VoilClient.fonts.getSelectedFont().getSize();
            float f20 = l + 30 + 15 / 2.0F;

            for (String s1 : this.visibilityNames) {
                float f4 = j1 + 60 / 2.0F - (VoilClient.fonts.getSelectedFont().getWidth(s1) + 100.0F) + f2 + 40.0F;
                if (s1.equals(this.currentVisibility)) {
                    float f25 = f4 - 3.0F;
                    float f15 = l + 30;
                    Render2DUtil.drawRoundedRect(f25, f15, VoilClient.fonts.getSelectedFont().getWidth(s1) + 5.0F, 15, 3.0F, 1.0F, Color.darkGray);
                }

                VoilClient.fonts.getSelectedFont().render(s1, f4 - 1.0F, f20 - k5 + 3.0F, -1);
                f2 += VoilClient.fonts.getSelectedFont().getWidth(s1) + 20.0F;
            }

            this.configs.clear();
            this.scripts.clear();
            int l5 = l + 55;
            int i6 = j1 + -126 + 1;
            String s2 = (String)ClientSettings.username.getValue();
            int k1 = k - 1;
            int l1 = l + 48;
            k3 = k + this.screen.windowWidth;
            int i2 = k3 + 1;
            l3 = this.screen.windowHeight;
            k3 = l;
            k3 += l3;
            int j2 = k3 + -7 + 1;
            this.bounds = new UiRect(k1, l1, i2 - k1, j2 - l1);
            RenderManager.setScissor(k1, l1, i2, j2);
            float f5 = i6 + -26 + 1;
            l3 = this.scrollOffset;
            k3 = l5;
            float f6 = k3 - l3;
            Render2DUtil.drawRoundedRect(f5, f6, 365.0F, 15.0F, 6.0F, 1.0F, ColorUtil.scaleBrightness(GuiTheme.moduleDisabledBg, 1.3F));
            msdffontatlas = VoilClient.fonts.getSelectedFont();
            String s16;
            if (this.inputText.isEmpty() && !this.creating) {
                s16 = "Search...";
            } else {
                String s6 = this.creating ? "|" : "";
                String s5 = this.inputText;
                s16 = s5 + s6;
            }

            msdffontatlas.render(s16, f5 + 5.0F, f6, new Color(11184810));
            int j6;
            if (this.currentTab.equals("Configs")) {
                int k2 = i6 + -26 + 1;
                k3 = l5 - this.scrollOffset;
                int l2 = k3 + 23;
                j6 = 0;

                for (CloudConfig cloudconfig : ClientSettings.cloudConfigs.values()) {
                    if ((!this.currentVisibility.equals("Private") || cloudconfig.getName().equalsIgnoreCase(s2))
                        && (
                            this.inputText.isEmpty()
                                || cloudconfig.getAuthor().toLowerCase().contains(this.inputText.toLowerCase())
                                || cloudconfig.getName().toLowerCase().contains(this.inputText.toLowerCase())
                        )) {
                        UiRect uirect = new UiRect(cloudconfig, null, k2, l2, 180, 100);
                        if (this.bounds.overlaps(uirect)) {
                            boolean flag = cloudconfig.getName().equalsIgnoreCase(s2);
                            float f26 = k2;
                            float f16 = l2;
                            Render2DUtil.drawRoundedRect(f26, f16, 180, 100, 3.0F, 1.0F, new Color(27, 27, 27));
                            VoilClient.fonts.getSelectedFont().render(cloudconfig.getAuthor(), k2 + 5, l2, 13.0F, -1);
                            int i3 = l2 + 17;
                            ArrayList<String> arraylist = new ArrayList<>();

                            for (String s3 : new String[]{cloudconfig.getCreatedAt(), cloudconfig.getVersion(), cloudconfig.getUpdatedAt()}) {
                                if (s3 != null && !s3.isEmpty()) {
                                    arraylist.add(s3);
                                }
                            }

                            float f21 = k2 + 5;
                            float f22 = i3 + (12 - VoilClient.fonts.getSelectedFont().getLineHeight()) / 2.0F;

                            for (String s13 : arraylist) {
                                int j3 = (int)(VoilClient.fonts.getSelectedFont().getWidth(s13) + 6.0F);
                                float f17 = i3;
                                Render2DUtil.drawRoundedRect(f21, f17, j3, 12, 4.0F, 1.0F, new Color(40, 40, 40));
                                VoilClient.fonts.getSelectedFont().render(s13, f21 + 3.0F, f22, new Color(14540253));
                                f21 += j3 + 4;
                            }

                            String s12 = cloudconfig.getDescription();
                            int k7 = l2 + 33;
                            ArrayList<String> arraylist1 = new ArrayList<>();
                            StringBuilder stringbuilder = new StringBuilder();

                            for (String s4 : s12.split(" ")) {
                                int i8 = stringbuilder.length();
                                l3 = s4.length();
                                k3 = i8;
                                k3 += l3;
                                if (k3 + 1 > 28) {
                                    arraylist1.add(stringbuilder.toString());
                                    stringbuilder = new StringBuilder(s4);
                                } else {
                                    if (stringbuilder.length() > 0) {
                                        stringbuilder.append(' ');
                                    }

                                    stringbuilder.append(s4);
                                }
                            }

                            if (stringbuilder.length() > 0) {
                                arraylist1.add(stringbuilder.toString());
                            }

                            for (int l7 = 0; l7 < arraylist1.size() && l7 < 4; l7++) {
                                String s14 = arraylist1.get(l7);
                                if (l7 == 3 && arraylist1.size() > 4) {
                                    String s15;
                                    if (s14.length() > 24) {
                                        String s8 = s14.substring(0, 24);
                                        s15 = s8 + "…";
                                    } else {
                                        s15 = s14 + "…";
                                    }

                                    s14 = s15;
                                }

                                msdffontatlas = VoilClient.fonts.getSelectedFont();
                                float f29 = k2 + 5;
                                l3 = l7 * 10;
                                k3 = k7;
                                msdffontatlas.render(s14, f29, k3 + l3, new Color(11184810));
                            }

                            VoilClient.fonts
                                .getSelectedFont()
                                .render(cloudconfig.getName(), k2 + 5, l2 + 82, ColorUtil.scaleBrightness(GuiTheme.leftPanel, 4.0F));
                            this.renderEntry(cloudconfig, k2, l2, 180, flag);
                        }

                        if (++j6 % 2 == 0) {
                            l2 += 115;
                            k2 = i6 + -26 + 1;
                        } else {
                            k2 += 185;
                        }
                    }
                }
            } else {
                int k6 = i6;
                l3 = this.scrollOffset;
                k3 = l5;
                k3 -= l3;
                int i7 = k3 + 23;
                j6 = 0;
                ScriptManager scriptmanager = VoilClient.getScriptManager();

                for (ConfigEntryInfo configentryinfo : ClientSettings.cloudScripts.values()) {
                    if ((!this.currentVisibility.equals("Private") || configentryinfo.getDescription().equalsIgnoreCase(s2))
                        && (
                            this.inputText.isEmpty()
                                || configentryinfo.getName().toLowerCase().contains(this.inputText.toLowerCase())
                                || configentryinfo.getDescription().toLowerCase().contains(this.inputText.toLowerCase())
                        )) {
                        double d2 = k6;
                        double d4 = i7;
                        double d0 = k6 + 180;
                        Render2DUtil.drawTriangle(d2, d4, d0, i7 + 100, 8.0, 10.0, new Color(27, 27, 27));
                        double d3 = k6;
                        k3 = i7 + 100;
                        double d5 = k3 + -3 - 10;
                        double d1 = k6 + 180;
                        k3 = i7 + 200 + -3;
                        Render2DUtil.drawTriangle(d3, d5, d1, k3 + -41 + 1, 8.0, 10.0, new Color(34, 34, 34));
                        float f18 = k6;
                        k3 = i7 + 100;
                        k3 += -3;
                        f31 = k3 + -11 + 1;
                        float f30 = k6 + 180;
                        k3 = i7 + 100 + -3;
                        Render2DUtil.drawRectArgb(f18, f31, f30, k3 + -6 + 1, new Color(27, 27, 27).getRGB());
                        VoilClient.fonts.getSelectedFont().render(configentryinfo.getName(), k6 + 5, i7, -1);
                        VoilClient.fonts.getSelectedFont().render(configentryinfo.getDescription(), k6 + 5, i7 + 42, Color.white.getRGB());
                        String s11 = configentryinfo.getName();
                        boolean flag2 = scriptmanager.isScriptEnabled(s11);
                        if (flag2) {
                            Color color = flag2 ? new Color(12, 155, 68) : new Color(155, 12, 12);
                            float f27 = k6;
                            float f19 = i7;
                            Render2DUtil.drawOutline(f27, f19, 180, 100, 2.0F, 1.0F, 1.0F, color);
                        }

                        this.configs.add(new UiRect(null, configentryinfo, k6, i7, 180, 100));
                        if (++j6 % 3 == 0) {
                            i7 += 115;
                            k6 = i6;
                        } else {
                            k6 += 185;
                        }
                    }
                }
            }

            int l6 = this.currentTab.equals("Configs") ? 2 : 3;
            k3 = j6 + l6;
            k3 = (k3 - 1) / l6;
            int j7 = k3 * 115;
            k3 = this.screen.windowHeight;
            this.maxScroll = Math.max(0, j7 - (k3 - l5));
            this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll);
            RenderManager.disableScissor();
        }
    }

    @Override
    public void mouseDragged() {
    }

    public void mouseScrolled(double d0, double d1, double d2, double d3) {
        if (this.scriptDetailsDialog != null) {
            ScriptDetailsDialog scriptdetailsdialog = this.scriptDetailsDialog;
            scriptdetailsdialog.mouseScrolled(d0, d1, d2, d3);
        }

        int j = (int)(d3 * 20.0);
        int i = this.scrollOffset;
        this.scrollOffset = i - j;
        if (this.scrollOffset < 0) {
            this.scrollOffset = 0;
        }

        if (this.scrollOffset > this.maxScroll) {
            this.scrollOffset = this.maxScroll;
        }

        super.mouseScrolled();
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (this.creating) {
            if (!Character.isLetterOrDigit(chr) && chr != ' ' && chr != '_') {
                return;
            }

            String s = this.inputText;
            this.inputText = s + chr;
        } else {
            if (this.createScriptDialog != null) {
                CreateScriptDialog createscriptdialog = this.createScriptDialog;
                createscriptdialog.charTyped(chr, modifiers);
            }

            if (this.createConfigDialog != null) {
                CreateConfigDialog createconfigdialog = this.createConfigDialog;
                createconfigdialog.charTyped(chr, modifiers);
            } else if (this.scriptDetailsDialog != null) {
                ScriptDetailsDialog scriptdetailsdialog = this.scriptDetailsDialog;
                scriptdetailsdialog.charTyped(chr, modifiers);
            }
        }
    }

    public void keyReleased(int i, int j, int k) {
        if (this.creating && i == 259 && !this.inputText.isEmpty()) {
            String s = this.inputText;
            int l = this.inputText.length();
            this.inputText = s.substring(0, l - 1);
        } else {
            if (this.createScriptDialog != null) {
                CreateScriptDialog createscriptdialog = this.createScriptDialog;
                createscriptdialog.keyPressed(i, j, k);
            }

            if (this.createConfigDialog != null) {
                CreateConfigDialog createconfigdialog = this.createConfigDialog;
                createconfigdialog.keyPressed(i, j, k);
            }

            if (this.scriptDetailsDialog != null) {
                ScriptDetailsDialog scriptdetailsdialog = this.scriptDetailsDialog;
                scriptdetailsdialog.keyPressed(i, j, k);
            }
        }
    }

    private void renderEntry(CloudConfig cloudconfig, int i, int j, int k, boolean flag) {
        int l = this.userVotes.getOrDefault(cloudconfig.getFileName().replace(".json", ""), 0);
        int k1 = cloudconfig.getDownvotes();
        int j1 = l == -1 ? 1 : 0;
        int i1 = k1;
        String s = String.valueOf(i1 + j1);
        k1 = cloudconfig.getUpvotes();
        j1 = l == 1 ? 1 : 0;
        i1 = k1;
        String s1 = String.valueOf(i1 + j1);
        float f = VoilClient.fonts.getSelectedFont().getWidth(s);
        float f1 = VoilClient.fonts.getSelectedFont().getWidth(s1);
        float f2 = 14.0F + 4.0F + f + 9.0F + 14.0F + 4.0F + f1 + 9.0F + 14.0F;
        if (flag) {
            f2 += (9.0F + 14.0F) * 2.0F;
        }

        float f3 = j + 82 - 1.0F;
        float f4 = f3 + (14.0F - VoilClient.fonts.getSelectedFont().getLineHeight()) / 2.0F;
        float f5 = i + k - 6.0F - f2;
        Color color = new Color(120, 120, 120);
        Color color1 = l == -1 ? new Color(210, 70, 70) : color;
        Color color2 = l == 1 ? new Color(80, 195, 110) : color;
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE1E1(), f5, f3, 14, color1.getRGB());
        VoilClient.fonts.getSelectedFont().render(s, f5 + 14.0F + 4.0F, f4, color1);
        this.scripts.add(new ConfigButton(ConfigAction.DOWNVOTE, cloudconfig, f5 - 1.0F, f3 - 1.0F, 14.0F + 4.0F + f + 2.0F, 14.0F + 2.0F));
        f5 += 14.0F + 4.0F + f + 9.0F;
        VoilClient.fonts.getIcons().render(IconGlyphs.iconE1E4(), f5, f3, 14, color2.getRGB());
        VoilClient.fonts.getSelectedFont().render(s1, f5 + 14.0F + 4.0F, f4, color2);
        this.scripts.add(new ConfigButton(ConfigAction.UPVOTE, cloudconfig, f5 - 1.0F, f3 - 1.0F, 14.0F + 4.0F + f1 + 2.0F, 14.0F + 2.0F));
        f5 += 14.0F + 4.0F + f1 + 9.0F;
        VoilClient.fonts.getIcons().render("", f5, f3, 14, GuiTheme.accentColor1.getRGB());
        this.scripts.add(new ConfigButton(ConfigAction.DOWNLOAD, cloudconfig, f5 - 2.0F, f3 - 1.0F, 14.0F + 4.0F, 14.0F + 2.0F));
        f5 += 14.0F + 9.0F;
        if (flag) {
            if (this.selectedEntry == cloudconfig) {
                VoilClient.fonts.getIcons().render(IconGlyphs.iconE04C(), f5, f3, 14, new Color(160, 160, 160).getRGB());
                this.scripts.add(new ConfigButton(ConfigAction.CANCEL, cloudconfig, f5 - 2.0F, f3 - 1.0F, 14.0F + 4.0F, 14.0F + 2.0F));
                f5 += 14.0F + 9.0F;
                VoilClient.fonts.getIcons().render(IconGlyphs.iconE18E(), f5, f3, 14, new Color(225, 70, 70).getRGB());
                this.scripts.add(new ConfigButton(ConfigAction.DELETE, cloudconfig, f5 - 2.0F, f3 - 1.0F, 14.0F + 4.0F, 14.0F + 2.0F));
            } else {
                VoilClient.fonts.getIcons().render(IconGlyphs.iconE0D0(), f5, f3, 14, new Color(150, 170, 210).getRGB());
                this.scripts.add(new ConfigButton(ConfigAction.UPDATE, cloudconfig, f5 - 2.0F, f3 - 1.0F, 14.0F + 4.0F, 14.0F + 2.0F));
                f5 += 14.0F + 9.0F;
                VoilClient.fonts.getIcons().render(IconGlyphs.iconE18E(), f5, f3, 14, new Color(170, 90, 90).getRGB());
                this.scripts.add(new ConfigButton(ConfigAction.DELETE, cloudconfig, f5 - 2.0F, f3 - 1.0F, 14.0F + 4.0F, 14.0F + 2.0F));
            }
        }
    }

    @Override
    public void mouseClicked2(double d0, double d1, int i) {
        this.mouseClicked(d0, d1, i);
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(matrixstack, i, j, f);
    }

    @Override
    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged();
    }

    @Override
    public void mouseScrolled2(double d0, double d1, double d2, double d3) {
        this.mouseScrolled(d0, d1, d2, d3);
    }

    @Override
    public void keyReleased2(int i, int j, int k) {
        this.keyReleased(i, j, k);
    }
}
