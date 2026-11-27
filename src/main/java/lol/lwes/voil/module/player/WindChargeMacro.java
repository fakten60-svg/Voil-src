package lol.lwes.voil.module.player;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class WindChargeMacro extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Silent", "How to rotate", new String[]{"Silent", "Looking", "Normal"});
    private NumberSetting pitch = new NumberSetting("Pitch", -90.0, 90.0, 90.0, 1.0, "Pitch of the wind charge");
    private NumberSetting smoothTicks = new NumberSetting("Smooth ticks", 1.0, 20.0, 3.0, 1.0, "Ticks to reach the aim before firing");
    private BooleanSetting rotateBack = new BooleanSetting("Rotate back", false, "Rotate back after using wind charge");
    private BooleanSetting switchBack = new BooleanSetting("Switch back", false, "Switch back after using wind charge");
    private NumberSetting switchBackDelay = new NumberSetting("Switch back delay", 0.0, 10.0, 0.0, 1.0, "Delay to switch back after using wind charge");
    private NumberSetting rotateBackTicks = new NumberSetting("Rotate Back ticks", 0.0, 10.0, 0.0, 1.0, "Ticks to rotate back after using wind charge");
    private BooleanSetting autoJump = new BooleanSetting("Auto Jump", true, "Automatically jumps after using wind charge");
    private KeybindSetting keybind = new KeybindSetting("Keybind", 0, false, "Keybind to use wind charge");
    private int tickCounter = 0;
    private Item previousItem = null;
    private boolean pendingJump = false;
    private boolean active = false;
    private boolean rotating = false;
    private float originalPitch;
    private SmoothRotationState rotationHandle;

    public WindChargeMacro() {
        super("WindChargeMacro", "Auto uses wind charge", Category.Player);
        Setting[] asetting = new Setting[]{
            this.mode, this.pitch, this.smoothTicks, this.rotateBackTicks, this.switchBackDelay, this.switchBack, this.rotateBack, this.autoJump, this.keybind
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.rotateBackTicks, this.rotateBack);
        Setting.dependSetting(this.switchBackDelay, this.switchBack);
        NumberSetting numbersetting = this.pitch;
        BooleanSupplier booleansupplier = () -> !this.mode.is("Looking");
        numbersetting.setVisibleWhen(booleansupplier);
        numbersetting = this.smoothTicks;
        BooleanSupplier booleansupplier1 = () -> !this.mode.is("Looking");
        numbersetting.setVisibleWhen(booleansupplier1);
        BooleanSetting booleansetting = this.rotateBack;
        BooleanSupplier booleansupplier2 = () -> !this.mode.is("Looking");
        booleansetting.setVisibleWhen(booleansupplier2);
    }

    private void useWindCharge() {
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (this.autoJump.isEnabled() && mc.player.isOnGround()) {
            this.pendingJump = true;
        }

        this.tickCounter = 0;
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null) {
            int i = InventoryUtil.findInventorySlot(Items.WIND_CHARGE);
            if (this.keybind.isPressed() && !this.active && this.rotationHandle == null && i != -1) {
                this.previousItem = this.switchBack.isEnabled() ? mc.player.getMainHandStack().getItem() : null;
                InventoryUtil.selectSlot(i);
                this.tickCounter = 0;
                if (this.mode.is("Silent")) {
                    this.rotationHandle = this.startRotation(this.pitch.getFloat(), true, RotationAxisMode.YawAndPitch);
                    this.active = true;
                } else if (this.mode.is("Normal")) {
                    this.originalPitch = mc.player.getPitch();
                    this.rotationHandle = this.startRotation(this.pitch.getFloat(), false, RotationAxisMode.Pitch);
                    this.active = true;
                } else {
                    this.useWindCharge();
                }
            }

            if (this.active && this.rotationHandle != null && (this.rotationHandle.isOnTarget() || this.rotationHandle.hasEnded())) {
                if (this.mode.is("Normal")) {
                    mc.player.setPitch(this.pitch.getFloat());
                }

                this.useWindCharge();
                this.active = false;
            }

            if (this.rotationHandle != null && !this.active) {
                if (this.mode.is("Normal")) {
                    if (this.rotating) {
                        if (this.rotationHandle.isOnTarget() || this.rotationHandle.hasEnded()) {
                            VoilClient.getRotationController().release(this);
                            this.rotationHandle = null;
                            this.rotating = false;
                        }
                    } else if (!this.rotateBack.isEnabled()) {
                        VoilClient.getRotationController().release(this);
                        this.rotationHandle = null;
                    } else if (this.tickCounter >= Math.max(1.0, this.rotateBackTicks.getValue())) {
                        this.rotationHandle = this.startRotation(this.originalPitch, false, RotationAxisMode.Pitch);
                        this.rotating = true;
                    }
                } else {
                    double d0 = this.rotateBack.isEnabled() ? Math.max(1.0, this.rotateBackTicks.getValue()) : 1.0;
                    if (this.tickCounter >= d0) {
                        VoilClient.getRotationController().release(this);
                        this.rotationHandle = null;
                    }
                }
            }

            if (this.switchBack.isEnabled() && this.previousItem != null && !this.active && this.tickCounter >= this.switchBackDelay.getValue()) {
                int j = InventoryUtil.findInventorySlot(this.previousItem);
                InventoryUtil.selectSlot(j);
                this.previousItem = null;
            }

            if (this.rotationHandle != null || this.previousItem != null) {
                this.tickCounter++;
            }
        }
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationController().release(this);
        this.rotationHandle = null;
        this.active = false;
        this.rotating = false;
    }

    private SmoothRotationState startRotation(float f, boolean flag, RotationAxisMode rotationaxismode) {
        SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
        Supplier supplier2 = SmoothRotationController.smoothForTicks(this.smoothTicks.getValue());
        Supplier supplier3 = () -> new Rotation(mc.player.getYaw(), f);
        EasingFunction easingfunction = UiEasings::linear;
        RotationAxisMode rotationaxismode1 = rotationaxismode;
        Object object = null;
        boolean flag1 = flag;
        Object object1 = null;
        Supplier supplier = supplier3;
        Supplier supplier1 = supplier2;
        WindChargeMacro windchargemacro1 = this;
        return smoothrotationcontroller.startWith(
            windchargemacro1, supplier1, supplier, (Supplier<Boolean>)object1, flag1, true, (SmoothRotationState)object, rotationaxismode1, easingfunction
        );
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (inputevent.localPlayer) {
            if (mc.player != null) {
                if (!(mc.currentScreen instanceof HandledScreen)) {
                    if (!mc.player.isOnFire()) {
                        if (!mc.player.isTouchingWater()) {
                            if (!mc.player.isInsideWall()) {
                                if (mc.player.isSprinting()) {
                                    if (this.autoJump.isEnabled() && mc.player.isOnGround() && this.pendingJump) {
                                        mc.player.jump();
                                        this.pendingJump = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
