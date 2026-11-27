package lol.lwes.voil.module.player;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EntitySpawnEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.rotation.ProjectileAimUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;

public class PearlChase extends Module {
    private ModeSetting rotation = new ModeSetting("Rotation", "Normal", "Rotation type", new String[]{"Normal", "Silent"});
    private RangeSetting ticksToRotate = new RangeSetting("Ticks to Rotate", 0.0, 10.0, 5.0, 7.0, 1.0, "Over how many ticks shall the rotation take to occur?");
    private BooleanSetting healthCheck = new BooleanSetting("Health check", false, "Only chases pearl if your health is more than their health");
    private BooleanSetting ignoreTowardsYou = new BooleanSetting("Ignore towards you", true, "Don't chase pearls thrown in your direction");
    private BooleanSetting renderLandingPos = new BooleanSetting("Render landing pos", false, "Draw where you're going to throw");
    private EnderPearlEntity targetPearl;
    private Vec3d landingPos;
    private boolean chasing;
    private int previousSlot;
    private SmoothRotationState rotationHandle;
    private static final String SILENT_MODE = "Silent";

    public PearlChase() {
        super("PearlChase", "Makes you chase the enemy pearl", Category.Player);
        Setting[] asetting = new Setting[]{this.rotation, this.ticksToRotate, this.healthCheck, this.ignoreTowardsYou, this.renderLandingPos};
        this.addSettings(asetting);
    }

    private Vec3d predictLanding(EnderPearlEntity enderpearlentity) {
        double d0 = enderpearlentity.getX();
        double d1 = enderpearlentity.getY();
        double d2 = enderpearlentity.getZ();
        Vec3d vec3d = enderpearlentity.getVelocity();
        double d3 = vec3d.x;
        double d4 = vec3d.y;
        double d5 = vec3d.z;

        for (int i = 0; i < 600; i++) {
            Vec3d vec3d1 = new Vec3d(d0, d1, d2);
            d0 += d3;
            d1 += d4;
            d2 += d5;
            d3 *= 0.99;
            d4 = d4 * 0.99 - 0.03;
            d5 *= 0.99;
            Vec3d vec3d2 = new Vec3d(d0, d1, d2);
            BlockHitResult blockhitresult = mc.world.raycast(new RaycastContext(vec3d1, vec3d2, ShapeType.OUTLINE, FluidHandling.NONE, enderpearlentity));
            if (blockhitresult != null && blockhitresult.getType() == Type.BLOCK) {
                return blockhitresult.getPos().add(0.0, 0.1, 0.0);
            }

            if (d1 <= -65.0) {
                break;
            }
        }

        return new Vec3d(d0, d1, d2);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            if (this.previousSlot != -1) {
                int j = this.previousSlot;
                InventoryUtil.selectSlot(j);
                this.previousSlot = -1;
            }

            if (this.targetPearl == null || !this.targetPearl.isRemoved() && this.targetPearl.age <= 300) {
                if (this.targetPearl != null && !this.chasing) {
                    this.landingPos = this.predictLanding(this.targetPearl);
                    int i = InventoryUtil.findHotbarSlot(Items.ENDER_PEARL);
                    if (i != -1) {
                        if (this.landingPos != null && this.rotationHandle.hasEnded()) {
                            ClientPlayerEntity clientplayerentity1 = mc.player;
                            Vec3d vec3d1 = this.landingPos;
                            ItemStack itemstack = mc.player.getInventory().getStack(i);
                            Rotation rotationx = ProjectileAimUtil.aimThrowable(clientplayerentity1, vec3d1, itemstack);
                            SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                            Supplier supplier3 = SmoothRotationController.smoothForTicks(this.ticksToRotate.getRandomInt());
                            Supplier supplier4 = () -> rotationx;
                            Supplier supplier5 = () -> this.chasing && this.previousSlot == -1;
                            boolean flag = this.rotation.is(SILENT_MODE);
                            Supplier supplier = supplier5;
                            Supplier supplier1 = supplier4;
                            Supplier supplier2 = supplier3;
                            PearlChase pearlchase1 = this;
                            this.rotationHandle = smoothrotationcontroller.start(pearlchase1, supplier2, supplier1, supplier, flag);
                        }

                        if (this.rotationHandle.isOnTarget() && !this.rotationHandle.hasEnded()) {
                            int k = mc.player.getInventory().getSelectedSlot();
                            if (i != k) {
                                InventoryUtil.selectSlot(i);
                            }

                            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            mc.player.swingHand(Hand.MAIN_HAND);
                            this.chasing = true;
                            this.previousSlot = k;
                        }
                    }
                } else {
                    VoilClient.getRotationController().release(this);
                }
            } else {
                this.onEnable();
            }
        }
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationController().release(this);
    }

    @Override
    public void onEnable() {
        this.targetPearl = null;
        this.landingPos = null;
        this.chasing = false;
        this.previousSlot = -1;
        this.rotationHandle = SmoothRotationState.createEnded();
        VoilClient.getRotationController().release(this);
    }

    private boolean isThrownTowardsPlayer(EnderPearlEntity enderpearlentity) {
        if (mc.player == null) {
            return false;
        }

        Vec3d vec3d = enderpearlentity.getEntityPos();
        Vec3d vec3d1 = enderpearlentity.getVelocity();
        Vec3d vec3d2 = mc.player.getEntityPos();
        Vec3d vec3d3 = vec3d2.subtract(vec3d).normalize();
        if (vec3d1.normalize().dotProduct(vec3d3) > 0.5) {
            Vec3d vec3d4 = this.predictLanding(enderpearlentity);
            if (vec3d4 != null) {
                return vec3d2.distanceTo(vec3d4) < 10.0;
            }
        }

        return false;
    }

    @EventHandler
    private void onEntitySpawn(EntitySpawnEvent entityspawnevent) {
        if (entityspawnevent.getEntity() instanceof EnderPearlEntity enderpearlentity) {
            Entity entity = enderpearlentity.getOwner();
            if (entity != null && !entity.getUuid().equals(mc.player.getUuid())) {
                if (this.ignoreTowardsYou.isEnabled() && this.isThrownTowardsPlayer(enderpearlentity)) {
                    return;
                }

                if (this.healthCheck.isEnabled() && entity instanceof LivingEntity livingentity && mc.player.getHealth() < livingentity.getHealth()) {
                    return;
                }

                this.targetPearl = enderpearlentity;
                this.landingPos = null;
                this.chasing = false;
                return;
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.landingPos != null && this.renderLandingPos.isEnabled()) {
            Matrix4f matrix4f = new Matrix4f();
            Color color = Color.green;
            Render3DUtil.drawPillar(matrix4f, this.landingPos, ColorUtil.withAlpha(color, 100), 0.3F, 0.1F);
        }
    }
}
