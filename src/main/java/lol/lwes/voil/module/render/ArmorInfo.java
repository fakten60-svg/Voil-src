package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.ArrayList;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ArmorInfo extends Module {
    public ArmorInfo() {
        super("ArmorInfo", "Shows enemies armor durability", Category.Render);
    }

    private Vec3d getSlotPosition(LivingEntity livingentity, EquipmentSlot equipmentslot) {
        Vec3d pos = livingentity.getLerpedPos(SystemUtil.getTickDelta());
        return switch (equipmentslot) {
            case HEAD -> pos.add(0.0, livingentity.getHeight() - 0.3, 0.0);
            case CHEST -> pos.add(0.0, livingentity.getHeight() * 0.6, 0.0);
            case LEGS -> pos.add(0.0, livingentity.getHeight() * 0.3, 0.0);
            case FEET -> pos.add(0.0, 0.01, 0.0);
            default -> pos;
        };
    }

    private void renderDurability(LivingEntity livingentity, Matrix4f matrix4f, EquipmentSlot equipmentslot) {
        ItemStack itemstack = livingentity.getEquippedStack(equipmentslot);
        if (!itemstack.isEmpty() && itemstack.contains(DataComponentTypes.EQUIPPABLE)) {
            int j = itemstack.getDamage();
            int i = itemstack.getMaxDamage();
            float f = (float)(i - j) / itemstack.getMaxDamage();
            Color color = this.getDurabilityColor(f);
            Vec3d vec3d = this.getSlotPosition(livingentity, equipmentslot);
            Render3DUtil.drawPillar(matrix4f, vec3d, ColorUtil.withAlpha(color, 100), 0.1F, 0.15F);
        }
    }

    private Color getDurabilityColor(float f) {
        if (f > 0.75F) {
            return Color.GREEN;
        } else if (f > 0.5F) {
            return Color.YELLOW;
        } else {
            return f > 0.25F ? Color.ORANGE : Color.RED;
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (mc.world != null && this.isEnabled()) {
            ArrayList arraylist = TargetManager.getTargetList(true);

            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof PlayerEntity playerentity && entity != mc.player && arraylist.contains(entity)) {
                    this.renderDurability(playerentity, new Matrix4f(), EquipmentSlot.HEAD);
                    this.renderDurability(playerentity, new Matrix4f(), EquipmentSlot.CHEST);
                    this.renderDurability(playerentity, new Matrix4f(), EquipmentSlot.LEGS);
                    this.renderDurability(playerentity, new Matrix4f(), EquipmentSlot.FEET);
                }
            }
        }
    }
}
