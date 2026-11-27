package lol.lwes.voil.module.player;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import lol.lwes.voil.mixin.accessor.ClientPlayerInteractionManagerAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class FastMine extends Module {
    private NumberSetting speed;
    private BooleanSetting render;
    private ColorSetting color;
    private BlockPos breakingPos;
    private Block breakingBlock;

    public FastMine() {
        super("FastMine", "Fast mine blocks", Category.Player);
        this.speed = new NumberSetting("Speed", 0.0, 1.0, 0.3, 0.1, "Speed of block breaking");
        this.render = new BooleanSetting("Render", true, "Render the block breaking progress");
        String s2 = "Color of the block breaking progress";
        Color colorx = Color.RED;
        this.color = new ColorSetting("Color", colorx, s2);
        this.breakingPos = null;
        this.breakingBlock = null;
        Setting[] asetting = new Setting[]{this.speed, this.render, this.color};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            BlockPos blockpos = ((ClientPlayerInteractionManagerAccessor)mc.interactionManager).getCurrentBreakingPos();
            if (blockpos != null) {
                if (this.breakingPos == null || !this.breakingPos.equals(blockpos)) {
                    this.breakingPos = blockpos;
                    this.breakingBlock = mc.world.getBlockState(blockpos).getBlock();
                }

                if (this.breakingPos != null && mc.world.getBlockState(this.breakingPos).isAir()) {
                    this.breakingPos = null;
                    this.breakingBlock = null;
                }
            } else {
                this.breakingPos = null;
                this.breakingBlock = null;
            }

            ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor)mc.interactionManager;
            float f = accessor.getCurBlockDamageMP();
            if (f < this.speed.getValue()) {
                accessor.setCurBlockDamageMP(this.speed.getFloat());
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.render.isEnabled() && this.breakingPos != null) {
            Matrix4f matrix4f = new Matrix4f();
            Vec3d vec3d = this.breakingPos.toCenterPos().add(0.0, -0.5, 0.0);
            Color colorx = this.color.getColor();
            Render3DUtil.drawPillar(matrix4f, vec3d, ColorUtil.withAlpha(colorx, 128), 0.5F, 1.0F);
        }
    }
}
