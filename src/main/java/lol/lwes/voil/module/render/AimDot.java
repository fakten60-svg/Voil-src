package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.rotation.Rotation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class AimDot extends Module {
    private ColorSetting dotColor;
    private BooleanSetting dynamicColor;
    private BooleanSetting onlyWhenRotating;
    private ModeSetting dotSize;
    private Vec3d dotPos;
    private final double smoothing;

    public AimDot() {
        super("AimDot", "Shows your server side rotations", Category.Render);
        String s2 = "Color of the dot";
        Color color = Color.GRAY;
        this.dotColor = new ColorSetting("Dot Color", color, s2);
        this.dynamicColor = new BooleanSetting("Dynamic Color", true, "Changes the color based on hit result");
        this.onlyWhenRotating = new BooleanSetting("Only When Rotating", false, "Only shows the dot when the player is rotating");
        this.dotSize = new ModeSetting("Dot Size", "Dot Size", "Dot Size", new String[]{"Small", "Medium", "Large"});
        this.dotPos = new Vec3d(0.0, 0.0, 0.0);
        this.smoothing = 0.1;
        Setting[] asetting = new Setting[]{this.dynamicColor, this.onlyWhenRotating, this.dotSize, this.dotColor};
        this.addSettings(asetting);
        Setting.dependSettingBoolFalse(this.dotColor, this.dynamicColor);
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (mc.player != null) {
            if (!this.onlyWhenRotating.isEnabled() || VoilClient.getRotationManager().isActive()) {
                Rotation rotation = VoilClient.getRotationManager().getTargetRotation();
                if (rotation.getYawFloat() == 0.0F && rotation.getPitchFloat() == 0.0F) {
                    rotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
                }

                ClientPlayerEntity clientplayerentity = mc.player;
                HitResult hitresult = RaycastUtil.getHitResultRanged(
                    clientplayerentity, entity -> true, rotation.getYawFloat(), rotation.getPitchFloat(), CombatUtil.getEntityReach(), false
                );
                Vec3d vec3d = hitresult.getPos();
                this.dotPos = this.dotPos.lerp(vec3d, 0.1);
                Color color2;
                if (this.dynamicColor.isEnabled()) {
                    color2 = switch (hitresult.getType()) {
                        case MISS -> this.dotColor.getColor();
                        case BLOCK -> Color.CYAN;
                        case ENTITY -> Color.RED;
                    };
                } else {
                    color2 = this.dotColor.getColor();
                }

                Color color1 = color2;
                Color color = ColorUtil.withAlpha(color1, 100);
                float f;
                float f1;
                switch (this.dotSize.getMode()) {
                    case "Small" -> {
                        f = 0.05F;
                        f1 = 0.1F;
                    }
                    case "Medium" -> {
                        f = 0.1F;
                        f1 = 0.2F;
                    }
                    case "Large" -> {
                        f = 0.2F;
                        f1 = 0.4F;
                    }
                    default -> {
                        f = 0.05F;
                        f1 = 0.1F;
                    }
                }

                Render3DUtil.drawPillar(new Matrix4f(), this.dotPos, color, f, f1);
            }
        }
    }
}
