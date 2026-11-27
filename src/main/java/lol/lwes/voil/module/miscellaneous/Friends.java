package lol.lwes.voil.module.miscellaneous;

import java.awt.Color;
import java.util.UUID;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.ChatUtil;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.input.KeyUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class Friends extends Module {
    private final KeybindSetting keybind = new KeybindSetting("Keybind", 0, false, "Keybind for adding and removing friends");
    private final BooleanSetting render = new BooleanSetting("Render", true, "Shows messages when friends are added or removed");
    private final BooleanSetting friendIndicator = new BooleanSetting("Friend Indicator", true, "Shows an indicator on head of your friend");
    private final BooleanSetting animate = new BooleanSetting("Animate", true, "Makes the indicator go up and down");
    private final NumberSetting distance = new NumberSetting("Distance", 3.0, 30.0, 25.0, 1.0, "Distance to show friends");
    private int unusedCounter = 0;

    public Friends() {
        super("Friends", "Adds friends with middle click", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{this.keybind, this.distance, this.friendIndicator, this.animate, this.render};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen == null) {
            int i = this.keybind.getKey();
            if (KeyUtil.isKeyDown(i)) {
                HitResult hitresult = mc.crosshairTarget;
                if (hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() instanceof PlayerEntity playerentity) {
                    this.toggleFriend(playerentity);
                }
            }
        }
    }

    @Override
    public void onRender2D() {
        if (mc.world != null && mc.player != null && this.friendIndicator.isEnabled()) {
            for (UUID uuid : PlayerTargetUtil.getFriends()) {
                PlayerEntity playerentity = null;

                for (Entity entity : mc.world.getEntities()) {
                    if (entity instanceof PlayerEntity playerentity1 && playerentity1.getUuid().equals(uuid)) {
                        playerentity = playerentity1;
                        break;
                    }
                }

                if (playerentity != null && playerentity != mc.player && !(playerentity.distanceTo(mc.player) > this.distance.getValue())) {
                    Vec3d vec3d = playerentity.getLerpedPos(SystemUtil.getTickDelta()).add(0.0, playerentity.getHeight() + 0.5, 0.0);
                    Vec3d vec3d1 = EntityUtil.worldToScreen(vec3d);
                    if (vec3d1.z > 0.0 && vec3d1.z < 1.0) {
                        double d2 = vec3d1.x;
                        double d0 = vec3d1.y;
                        if (this.animate.isEnabled()) {
                            double d1 = Math.sin(((float)mc.world.getTime() + SystemUtil.getTickDelta()) / 20.0 * 1.4) * 3.5;
                            d0 += d1;
                        }

                        Render2DUtil.drawAxes(Color.green, d2, d0 - 4.0, 12.0F, 12.0F, 2.0F, 315.0F);
                    }
                }
            }
        }
    }

    private void toggleFriend(PlayerEntity playerentity) {
        this.unusedCounter++;
        if (this.unusedCounter >= 5) {
            this.unusedCounter = 0;
            UUID uuid = playerentity.getUuid();
            if (PlayerTargetUtil.isFriend(uuid)) {
                PlayerTargetUtil.removeFriend(uuid);
                if (this.render.isEnabled()) {
                    String s2 = playerentity.getName().getString();
                    ChatUtil.info("§d§l[Voil] §rYou are no longer friends with " + s2);
                }
            } else {
                PlayerTargetUtil.addFriend(uuid);
                if (this.render.isEnabled()) {
                    String s5 = playerentity.getName().getString();
                    ChatUtil.info("§d§l[Voil] §rYou are now friends with " + s5);
                }
            }
        }
    }
}
