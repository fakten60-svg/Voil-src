package lol.lwes.voil.module.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.function.Supplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationAxisMode;
import lol.lwes.voil.util.rotation.SmoothRotationController;
import lol.lwes.voil.util.rotation.SmoothRotationState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

public class AutoPot extends Module {
    NumberSetting health = new NumberSetting("Health", 1.0, 19.0, 8.0, 1.0, "Minimum health to use potion");
    RangeSetting delay = new RangeSetting("Delay", 1.0, 20.0, 3.0, 4.0, 1.0, "Delay before using potion");
    RangeSetting rotateBackTicks = new RangeSetting("Rotate Back Ticks", 1.0, 20.0, 4.0, 5.0, 1.0, "Ticks before rotating back");
    RangeSetting switchBackDelay = new RangeSetting("Switch Back Delay", 1.0, 20.0, 1.0, 2.0, 1.0, "Ticks before switching back to previous slot");
    BooleanSetting autoBuff = new BooleanSetting("AutoBuff", false, "Automatically buff with potion");
    ModeSetting mode = new ModeSetting("Mode", "Auto", "Activation mode", new String[]{"Auto", "On Key"});
    KeybindSetting keybind = new KeybindSetting("Keybind", 0, false, "Keybind for manual potion throw");
    ModeSetting rotations = new ModeSetting("Rotations", "Legit", "Rotations mode", new String[]{"Silent", "Legit", "Looking"});
    RangeSetting throwPitch = new RangeSetting("Throw Pitch", 0.0, 90.0, 80.0, 90.0, 0.1, "The pitch to throw potions at");
    BooleanSetting cancelOnSlotChange = new BooleanSetting("Cancel On Slot Change", true, "Cancels the potion throw if you change your slot");
    float originalPitch;
    boolean throwing;
    boolean rotationReady;
    boolean waiting;
    boolean finished;
    int originalSlot;
    int useTicks;
    int rotateTicks;
    int potionSlot;
    List queuedEffects;
    StatusEffect currentEffect;
    SmoothRotationState rotationHandle;
    private static final List<RegistryEntry<StatusEffect>> BUFF_EFFECTS = List.of(StatusEffects.REGENERATION, StatusEffects.STRENGTH, StatusEffects.SPEED, StatusEffects.FIRE_RESISTANCE);
    private static final StatusEffect HEAL_EFFECT = StatusEffects.INSTANT_HEALTH.value();

    public AutoPot() {
        super("AutoPot", "Automatically throws potion", Category.Combat);
        Setting[] asetting = new Setting[]{
            this.health,
            this.delay,
            this.rotateBackTicks,
            this.switchBackDelay,
            this.autoBuff,
            this.cancelOnSlotChange,
            this.mode,
            this.keybind,
            this.rotations,
            this.throwPitch
        };
        this.addSettings(asetting);
        ModeSetting modesetting = this.mode;
        Setting.dependSetting(this.keybind, "On Key", modesetting);
        ModeSetting modesetting1 = this.mode;
        Setting.dependSetting(this.health, "Auto", modesetting1);
        Setting.dependAny(this.rotateBackTicks, this.rotations, new String[]{"Silent", "Legit"});
        Setting.dependAny(this.throwPitch, this.rotations, new String[]{"Silent", "Legit"});
    }

    private void beginPotionThrow(int j, StatusEffect statuseffect) {
        if (!this.throwing && !this.waiting) {
            this.rotationReady = false;
            this.throwing = true;
            this.originalSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(j);
            this.potionSlot = j;
            this.currentEffect = statuseffect;
            double d0 = this.delay.getRandomValue();
            this.rotateTicks = (int)Math.ceil(d0);
            String s = this.rotations.getMode();
            if (s.equals("Silent") || s.equals("Legit")) {
                this.originalPitch = mc.player.getPitch();
                double d1 = this.throwPitch.getRandomValue();
                SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                Supplier supplier3 = SmoothRotationController.smoothForTicks(d0);
                Supplier supplier4 = () -> new Rotation(mc.player.getYaw(), d1);
                Supplier supplier5 = () -> this.rotationReady;
                boolean flag1 = this.rotations.is("Silent");
                RotationAxisMode rotationaxismode1 = RotationAxisMode.Pitch;
                EasingFunction easingfunction = UiEasings::linear;
                RotationAxisMode rotationaxismode = rotationaxismode1;
                Object object = null;
                boolean flag = flag1;
                Supplier supplier = supplier5;
                Supplier supplier1 = supplier4;
                Supplier supplier2 = supplier3;
                AutoPot autopot1 = this;
                this.rotationHandle = smoothrotationcontroller.startWith(
                    autopot1, supplier2, supplier1, supplier, flag, true, (SmoothRotationState)object, rotationaxismode, easingfunction
                );
            }
        }
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame()) {
            if (mc.currentScreen == null) {
                if (this.potionSlot != -1 && mc.player.getInventory().getSelectedSlot() != this.potionSlot && this.cancelOnSlotChange.isEnabled()) {
                    boolean flag1 = this.throwing;
                    this.throwing = false;
                    this.waiting = false;
                    this.rotateTicks = 0;
                    this.useTicks = 0;
                    this.potionSlot = -1;
                    if (flag1 && !this.rotations.is("Looking")) {
                        this.finished = true;
                    }
                } else {
                    if (this.waiting) {
                        int j = this.useTicks;
                        AutoPot autopot2 = this;
                        autopot2.useTicks = j - 1;
                        if (0 >= j) {
                            mc.player.getInventory().setSelectedSlot(this.originalSlot);
                            this.waiting = false;
                            this.potionSlot = -1;
                        }
                    }

                    if (this.finished) {
                        this.finished = false;
                        this.throwing = false;
                        this.rotationReady = true;
                        SmoothRotationController smoothrotationcontroller = VoilClient.getRotationController();
                        Supplier supplier3 = SmoothRotationController.smoothForTicks(this.rotateBackTicks.getRandomValue());
                        Supplier supplier4 = () -> new Rotation(mc.player.getYaw(), this.originalPitch);
                        Supplier supplier5 = () -> true;
                        boolean flag2 = this.rotations.is("Silent");
                        RotationAxisMode rotationaxismode1 = RotationAxisMode.Pitch;
                        EasingFunction easingfunction = UiEasings::linear;
                        RotationAxisMode rotationaxismode = rotationaxismode1;
                        Object object = null;
                        boolean flag = flag2;
                        Supplier supplier = supplier5;
                        Supplier supplier1 = supplier4;
                        Supplier supplier2 = supplier3;
                        AutoPot autopot1 = this;
                        this.rotationHandle = smoothrotationcontroller.startWith(
                            autopot1, supplier2, supplier1, supplier, flag, true, (SmoothRotationState)object, rotationaxismode, easingfunction
                        );
                    }

                    if (this.throwing) {
                        int k = this.rotateTicks;
                        AutoPot autopot3 = this;
                        autopot3.rotateTicks = k - 1;
                        if (0 >= k) {
                            StatusEffect statuseffect = this.currentEffect;
                            ItemStack itemstack = mc.player.getMainHandStack();
                            StatusEffect statuseffect1 = statuseffect;
                            if (!InventoryUtil.isSplashPotionWith(statuseffect1, itemstack)) {
                                int i = InventoryUtil.findSplashPotionSlot(this.currentEffect);
                                if (i != -1) {
                                    this.originalSlot = mc.player.getInventory().getSelectedSlot();
                                    InventoryUtil.selectSlot(i);
                                    this.potionSlot = i;
                                }
                            }

                            ItemStack itemstack1 = mc.player.getMainHandStack();
                            StatusEffect statuseffect2 = this.currentEffect;
                            if (InventoryUtil.isSplashPotionWith(statuseffect2, itemstack1)) {
                                double d0 = 0.0;

                                for (StatusEffectInstance statuseffectinstance : itemstack1.get(DataComponentTypes.POTION_CONTENTS).getEffects()) {
                                    if (statuseffectinstance.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                                        d0 = (4 << statuseffectinstance.getAmplifier()) + 0.5;
                                    }
                                }

                                this.queuedEffects.add(this.currentEffect);
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                                mc.player.swingHand(Hand.MAIN_HAND);
                                if (this.mode.is("Auto") || this.keybind.isPressed()) {
                                    List list = this.queuedEffects;
                                    double d1 = d0;
                                    Entry entry1 = this.findBestPotion(d1, list);
                                    if ((Integer)entry1.getKey() != -1) {
                                        this.currentEffect = (StatusEffect)entry1.getValue();
                                        mc.player.getInventory().setSelectedSlot((Integer)entry1.getKey());
                                        this.potionSlot = (Integer)entry1.getKey();
                                        this.rotateTicks = (int)Math.ceil(this.delay.getRandomValue());
                                        return;
                                    }
                                }
                            }

                            this.waiting = true;
                            this.useTicks = this.switchBackDelay.getRandomInt();
                            if (!this.rotations.is("Looking") && (this.rotationHandle.isOnTarget() || this.rotationHandle.hasEnded())) {
                                this.finished = true;
                            }
                        }
                    }

                    if (this.mode.is("On Key") && this.keybind.isPressed() || this.mode.is("Auto")) {
                        this.queuedEffects.clear();
                        List list1 = this.queuedEffects;
                        double d2 = 0.0;
                        Entry entry = this.findBestPotion(d2, list1);
                        if ((Integer)entry.getKey() != -1) {
                            int l = (Integer)entry.getKey();
                            Object object1 = entry.getValue();
                            this.beginPotionThrow(l, (StatusEffect)object1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        VoilClient.getRotationController().release(this);
    }

    @Override
    public void onEnable() {
        this.originalPitch = 0.0F;
        this.originalSlot = mc.player != null ? mc.player.getInventory().getSelectedSlot() : 0;
        this.throwing = false;
        this.rotationReady = false;
        this.waiting = false;
        this.finished = false;
        this.rotateTicks = 0;
        this.useTicks = 0;
        this.potionSlot = -1;
        this.currentEffect = null;
        this.queuedEffects = new ArrayList();
        VoilClient.getRotationController().release(this);
        this.rotationHandle = SmoothRotationState.createEnded();
    }

    private Entry findBestPotion(double d0, List list) {
        StatusEffect statuseffect = HEAL_EFFECT;
        int i = InventoryUtil.findSplashPotionSlot(HEAL_EFFECT);
        int j = -1;
        if (this.mode.is("Auto") && !(mc.player.getHealth() + d0 <= this.health.getFloat()) || i == -1) {
            if (!this.autoBuff.isEnabled()) {
                return Map.entry(-1, HEAL_EFFECT);
            }

            for (RegistryEntry<StatusEffect> registryentry : BUFF_EFFECTS) {
                int k = InventoryUtil.findSplashPotionSlot((StatusEffect)registryentry.value());
                if (!mc.player.hasStatusEffect(registryentry) && k != -1 && !list.contains(registryentry.value())) {
                    statuseffect = (StatusEffect)registryentry.value();
                    j = k;
                    break;
                }
            }
        } else {
            j = i;
        }

        return Map.entry(j, statuseffect);
    }
}
