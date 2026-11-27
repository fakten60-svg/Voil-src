package lol.lwes.voil.gui.clickgui.component;

import dev.prestigeclient.renderlib.api.font.MSDFFontAtlas;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.gui.clickgui.ClickGuiComponent;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import lol.lwes.voil.setting.CurveSetting;
import net.minecraft.client.util.math.MatrixStack;

public class CurveSettingComponent extends SettingComponent {
    private final CurveSetting setting;

    public CurveSettingComponent(CurveSetting curvesetting, ClickGuiScreen clickguiscreen, int i, int j) {
        super(curvesetting, clickguiscreen);
        this.setting = curvesetting;
        this.setPosition(i, j);
    }

    public void render(int i, int j) {
        MSDFFontAtlas font = VoilClient.fonts.getSelectedFont();
        String value = this.setting.getPoints().size() + " points";
        float textY = this.getTextY();
        int color = GuiTheme.normalTextColor.getRGB();
        font.render(this.setting.getName(), this.getInnerLeft(), textY, 10.0F, color);
        font.render(value, this.getInnerRight() - font.getWidth(value, 10.0F), textY, 10.0F, color);
        if (ClickGuiComponent.isPointInRect(this.getLeft(), this.width, this.getRight(), this.width + this.getRowHeight(), i, j)) {
            this.setTooltip(this.setting.getDescription().toString(), i, j);
        }
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render(i, j);
    }
}
