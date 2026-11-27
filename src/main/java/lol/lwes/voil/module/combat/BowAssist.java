package lol.lwes.voil.module.combat;

import java.util.Map;
import java.util.WeakHashMap;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.rotation.ProjectileAimUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationPriority;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BowAssist extends Module {
    private final NumberSetting fov = new NumberSetting("FOV", 10.0, 360.0, 360.0, 1.0, "Field of view to target players");
    private final NumberSetting range = new NumberSetting("Range", 10.0, 100.0, 50.0, 1.0, "Max target range");
    private final NumberSetting prediction = new NumberSetting("Prediction", 0.0, 10.0, 3.0, 1.0, "Extra velocity smoothing ticks");
    private final BooleanSetting onlyCharged = new BooleanSetting("Only Charged", true, "Only aim when bow is sufficiently charged");
    private final NumberSetting minCharge = new NumberSetting("Min Charge", 0.1, 1.0, 0.3, 0.05, "Minimum bow charge to start aiming");
    private final Map<PlayerEntity, Vec3d[]> velocityHistory = new WeakHashMap<>();

    public BowAssist() {
        super("BowAssist", "Makes you hit every bow shot", Category.Combat);
        Setting[] asetting = new Setting[]{this.fov, this.range, this.prediction, this.onlyCharged, this.minCharge};
        this.addSettings(asetting);
    }

    private PlayerEntity findTarget() {
        PlayerEntity playerentity = null;
        double d0 = this.range.getValue();

        for (PlayerEntity playerentity1 : mc.world.getPlayers()) {
            if (playerentity1 != mc.player && !playerentity1.isSpectator() && playerentity1.isAlive()) {
                double d1 = mc.player.distanceTo(playerentity1);
                if (!(d1 > d0)) {
                    if (this.fov.getValue() < 360.0) {
                        Vec3d vec3d = playerentity1.getEntityPos().subtract(mc.player.getEntityPos());
                        float f = (float)(MathHelper.atan2(vec3d.z, vec3d.x) * 180.0F / (float)Math.PI) - 90.0F;
                        float f1 = Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - f));
                        if (f1 > this.fov.getValue() / 2.0) {
                            continue;
                        }
                    }

                    d0 = d1;
                    playerentity = playerentity1;
                }
            }
        }

        return playerentity;
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationManager().removeRequest(this);
        this.velocityHistory.clear();
    }

    private Vec3d getAverageVelocity(PlayerEntity playerentity) {
        Vec3d[] avec3d = (Vec3d[])this.velocityHistory.get(playerentity);
        if (avec3d == null) {
            return playerentity.getVelocity();
        }

        double d0 = 0.0;
        double d1 = 0.0;
        double d2 = 0.0;
        int i = 0;

        for (int j = 0; j < 5; j++) {
            if (avec3d[j] != null) {
                double d3 = 1.0 + j;
                d0 += avec3d[j].x * d3;
                d1 += avec3d[j].y * d3;
                d2 += avec3d[j].z * d3;
                i = (int)(i + d3);
            }
        }

        return i == 0 ? playerentity.getVelocity() : new Vec3d(d0 / i, d1 / i, d2 / i);
    }

    @EventHandler
    private void onTick(TickEvent tickevent) {
        if (!Module.isNotInGame()) {
            if (mc.player.isUsingItem() && mc.player.getActiveItem().getItem() instanceof BowItem) {
                if (this.onlyCharged.isEnabled()) {
                    float f = mc.player.getItemUseTime() / 20.0F;
                    f = Math.min((f * f + 2.0F * f) / 3.0F, 1.0F);
                    if (f < this.minCharge.getValue()) {
                        VoilClient.getRotationManager().removeRequest(this);
                        return;
                    }
                }

                for (PlayerEntity playerentity : mc.world.getPlayers()) {
                    if (playerentity != mc.player) {
                        Vec3d[] avec3d = this.velocityHistory.computeIfAbsent(playerentity, playerentity3 -> new Vec3d[5]);
                        System.arraycopy(avec3d, 1, avec3d, 0, 4);
                        avec3d[4] = playerentity.getVelocity();
                    }
                }

                PlayerEntity playerentity2 = this.findTarget();
                if (playerentity2 == null) {
                    VoilClient.getRotationManager().removeRequest(this);
                } else {
                    Vec3d vec3d1 = this.getAverageVelocity(playerentity2);
                    ItemStack itemstack1 = mc.player.getActiveItem();
                    ClientPlayerEntity clientplayerentity1 = mc.player;
                    int j = (int)this.prediction.getValue();
                    Rotation rotation = ProjectileAimUtil.aimBow(clientplayerentity1, playerentity2, itemstack1, vec3d1, j);
                    if (rotation != null) {
                        VoilClient.getRotationManager().requestRotationPriority(this, rotation, RotationPriority.HIGH);
                    } else {
                        VoilClient.getRotationManager().removeRequest(this);
                    }
                }
            } else {
                VoilClient.getRotationManager().removeRequest(this);
            }
        }
    }
}
