package lol.lwes.voil.module.player;

import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.DamageEvent;
import lol.lwes.voil.event.EntitySpawnEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.gui.NotificationManager;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.crystalpvp.AutoCrystal;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.MovementUtil;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AutoBowBoost extends Module {
    private ModeSetting rotations = new ModeSetting("Rotations", "Silent", "How to rotate", new String[]{"Silent", "Normal"});
    private BooleanSetting autoMove = new BooleanSetting("Auto Move", true, "Automatically moves you forward and makes you jump");
    private NumberSetting boostCount = new NumberSetting("Boost Count", 1.0, 10.0, 1.0, 1.0, "Number of bow boosts to perform");
    private boolean released = false;
    private boolean disablePending = false;
    private long disableTime = System.currentTimeMillis();
    private int previousSlot = -1;
    private ArrowEntity arrow;
    private SmoothRotationState rotationHandle;
    private static int remainingBoosts = 0;
    private static boolean boostActive = false;
    private static final long RESTART_DELAY_MS = 750L;

    public AutoBowBoost() {
        super("AutoBowBoost", "Automatically boosts you with bow", Category.Player);
        Setting[] asetting = new Setting[]{this.rotations, this.autoMove, this.boostCount};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (AutoCrystal.isInGame()) {
            if (this.released) {
                if (this.arrow != null && this.arrow.age > 6 && MovementUtil.getPositionDelta(this.arrow).equals(Vec3d.ZERO)) {
                    NotificationManager notificationmanager = VoilClient.getNotifications();
                    String s = "BowBoost Failed, disabled!";
                    String s1 = "BowBoost Failed";
                    notificationmanager.addNotification(s1, s);
                    this.setEnabled(false);
                }
            } else {
                ClientPlayerEntity clientplayerentity = mc.player;
                if (InventoryUtil.isUsingItem(clientplayerentity, Items.BOW)) {
                    Rotation rotation = new Rotation(mc.player.getYaw(), -58.0);
                    SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                    Supplier supplier3 = () -> !mc.player.isUsingItem() ? 0.0 : mc.player.getItemUseTime() + SystemUtil.getTickDelta() - 4.0;
                    Supplier supplier4 = () -> rotation;
                    Supplier supplier5 = () -> this.released;
                    boolean flag1 = this.rotations.is("Silent");
                    RotationAxisMode rotationaxismode1 = RotationAxisMode.Pitch;
                    EasingFunction easingfunction = UiEasings::linear;
                    RotationAxisMode rotationaxismode = rotationaxismode1;
                    Object object = null;
                    boolean flag = flag1;
                    Supplier supplier = supplier5;
                    Supplier supplier1 = supplier4;
                    Supplier supplier2 = supplier3;
                    AutoBowBoost autobowboost1 = this;
                    this.rotationHandle = smoothrotationcontroller.startWith(
                        autobowboost1, supplier2, supplier1, supplier, flag, false, (SmoothRotationState)object, rotationaxismode, easingfunction
                    );
                    if (!mc.player.isUsingItem() && mc.player.getMainHandStack().getItem() == Items.BOW) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.options.useKey.setPressed(true);
                    }

                    if (mc.player.isUsingItem() && mc.player.getMainHandStack().isOf(Items.BOW)) {
                        if (mc.player.getItemUseTime() >= 4.0) {
                            mc.options.useKey.setPressed(false);
                            this.released = true;
                        } else {
                            mc.options.useKey.setPressed(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        mc.options.useKey.setPressed(false);
        VoilClient.getRotationController().release(this);
        if (boostActive && remainingBoosts > 0) {
            new Thread(() -> {
                try {
                    Thread.sleep(RESTART_DELAY_MS);
                    if (remainingBoosts > 0) {
                        mc.execute(() -> this.setEnabled(true));
                    }
                } catch (InterruptedException interruptedexception) {
                }
            }).start();
        } else {
            boostActive = false;
            remainingBoosts = 0;
            int i = this.previousSlot;
            if (InventoryUtil.isHotbarSlot(i)) {
                int j = this.previousSlot;
                InventoryUtil.selectSlot(j);
                this.previousSlot = -1;
            }
        }
    }

    @Override
    public void onEnable() {
        this.released = false;
        this.disablePending = false;
        this.disableTime = System.currentTimeMillis();
        this.arrow = null;
        VoilClient.getRotationController().release(this);
        this.rotationHandle = SmoothRotationState.createEnded();
        if (!boostActive) {
            remainingBoosts = this.boostCount.getInt();
        }

        if (AutoCrystal.isInGame()) {
            this.previousSlot = mc.player.getInventory().getSelectedSlot();
            if (InventoryUtil.containsItemInHotbar(Items.BOW)) {
                int i = InventoryUtil.findHotbarSlot(Items.BOW);
                InventoryUtil.selectSlot(i);
            }
        }
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (AutoCrystal.isInGame()) {
            if (this.autoMove.isEnabled()) {
                inputevent.input.playerInput.forward = true;
                inputevent.input.playerInput.jump = true;
                inputevent.input.forward = 1.0F;
            }

            if (this.disablePending && this.disableTime < System.currentTimeMillis()) {
                this.setEnabled(false);
            }
        }
    }

    @EventHandler
    private void onSpawn(EntitySpawnEvent entityspawnevent) {
        if (mc.player != null) {
            if (entityspawnevent.getEntity() instanceof ArrowEntity arrowentity && this.released) {
                Entity entity = arrowentity.getOwner();
                if (entity == null) {
                    return;
                }

                if (entity == mc.player) {
                    this.arrow = arrowentity;
                }
            }
        }
    }

    @EventHandler
    private void onDamage(DamageEvent damageevent) {
        if (AutoCrystal.isInGame()) {
            if (damageevent.entity == mc.player && this.released) {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }

                int i = remainingBoosts--;
                this.rotationHandle.markDone();
                boostActive = remainingBoosts > 0;
                NotificationManager notificationmanager = VoilClient.getNotifications();
                String s = "BowBoost Success disabled!";
                String s1 = "BowBoost Success";
                notificationmanager.addNotification(s1, s);
                this.setEnabled(false);
            }
        }
    }
}
