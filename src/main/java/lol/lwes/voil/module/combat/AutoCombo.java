package lol.lwes.voil.module.combat;

import java.util.List;
import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.InputEvent;
import lol.lwes.voil.event.KeyPressEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.movement.Sprint;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.MovementUtil;

public class AutoCombo extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "WTap", "Comboing mode", new String[]{"STap", "WTap", "ShiftTap", "Sprint Reset"});
    private final ListSetting cycleOrder = new ListSetting(
        "Cycle Order", "Order the Next/Prev binds step through", new String[]{"STap", "WTap", "ShiftTap", "Sprint Reset"}
    );
    private final KeybindSetting nextMode = new KeybindSetting("Next Mode", 0, false, "Cycle to the next mode in the order");
    private final KeybindSetting prevMode = new KeybindSetting("Prev Mode", 0, false, "Cycle to the previous mode in the order");
    private final NumberSetting chance = new NumberSetting("Chance", 1.0, 100.0, 75.0, 1.0, "Chance to perform the combo");
    public final NumberSetting minDelay = new NumberSetting("Min Delay", 0.0, 2500.0, 500.0, 1.0, "Delay Per Tap");
    public final RangeSetting tapStartTime = new RangeSetting("Tap start time", 0.0, 1000.0, 0.0, 50.0, 1.0, "Milliseconds after the attack to start tapping");
    public final RangeSetting tapStopTime = new RangeSetting(
        "Tap stop time", 0.0, 1000.0, 150.0, 250.0, 1.0, "Milliseconds after the tap start to stop tapping"
    );
    public final BooleanSetting onlyOnGround = new BooleanSetting("Only on ground", true, "Only when on ground");
    public final BooleanSetting onlyIfMoving = new BooleanSetting("Only if moving", true, "Only when moving");
    private long windowStart;
    private long windowEnd;
    private long lastHitTime;
    private boolean canHit;
    private boolean comboReady;

    public AutoCombo() {
        super("AutoCombo", "Auto W/S combos on hit", Category.Combat);
        Setting[] asetting = new Setting[]{
            this.mode,
            this.cycleOrder,
            this.nextMode,
            this.prevMode,
            this.chance,
            this.minDelay,
            this.tapStartTime,
            this.tapStopTime,
            this.onlyOnGround,
            this.onlyIfMoving
        };
        this.addSettings(asetting);
    }

    private boolean isInWindow() {
        long i = System.currentTimeMillis();
        return i >= this.windowStart && i <= this.windowEnd;
    }

    @Override
    public void onDisable() {
        if (this.mode.is("Sprint Reset")) {
            Sprint.setSprintBlocked(false);
        }
    }

    @Override
    public void onEnable() {
        this.canHit = true;
        this.comboReady = true;
    }

    private void runComboStep(int i) {
        List list = this.cycleOrder.getItems();
        if (!list.isEmpty()) {
            int j = list.indexOf(this.mode.getMode());
            int i1;
            if (j < 0) {
                if (i > 0) {
                    i1 = 0;
                } else {
                    int l = list.size();
                    i1 = l - 1;
                }
            } else {
                i1 = Math.floorMod(j + i, list.size());
            }

            int k = i1;
            ModeSetting modesetting = this.mode;
            Object object = list.get(k);
            String s = (String)object;
            modesetting.setMode(s);
        }
    }

    @EventHandler
    private void onAttack(AttackEntityPostEvent attackentitypostevent) {
        if (System.currentTimeMillis() >= this.lastHitTime + this.minDelay.getInt() && this.canHit && this.comboReady) {
            boolean flag = mc.player.isOnGround() && mc.player.verticalCollision && mc.player.fallDistance == 0.0 && Math.abs(mc.player.getVelocity().y) < 0.1;
            if (!this.onlyOnGround.isEnabled() || flag) {
                if (this.onlyIfMoving.isEnabled() && !MovementUtil.hasMovementInput()) {
                    return;
                }

                this.windowStart = (long)(System.currentTimeMillis() + this.tapStartTime.getRandomValue());
                this.windowEnd = (long)(this.windowStart + this.tapStopTime.getRandomValue());
                this.lastHitTime = System.currentTimeMillis();
                if (this.mode.is("Sprint Reset")) {
                    mc.player.setSprinting(true);
                    Sprint.setSprintBlocked(false);
                }

                this.canHit = false;
                this.comboReady = false;
                ((Sprint)ModuleManager.INSTANCE.getModule(Sprint.class)).setEnabled(true);
            }
        }
    }

    @EventHandler
    private void onInput(InputEvent inputevent) {
        if (inputevent.localPlayer) {
            if (!this.isInWindow()) {
                if (this.comboReady && !this.canHit) {
                    this.canHit = true;
                    if (this.mode.is("Sprint Reset")) {
                        mc.player.setSprinting(true);
                        Sprint.setSprintBlocked(false);
                    }
                }
            } else {
                switch (this.mode.getMode()) {
                    case "WTap" -> {
                        inputevent.input.playerInput.forward = false;
                        inputevent.input.playerInput.backward = false;
                        inputevent.input.forward = 0.0F;
                    }
                    case "STap" -> {
                        inputevent.input.playerInput.forward = false;
                        inputevent.input.playerInput.backward = true;
                        inputevent.input.playerInput.sprint = false;
                        inputevent.input.forward = -1.0F;
                        if (this.comboReady && !this.canHit) {
                            this.canHit = true;
                            mc.player.setSprinting(true);
                            Sprint.setSprintBlocked(false);
                        }
                    }
                    case "ShiftTap" -> inputevent.input.playerInput.sneak = true;
                    case "Sprint Reset" -> {
                        if (!this.comboReady) {
                            mc.player.setSprinting(false);
                            Sprint.setSprintBlocked(true);
                        }
                    }
                    default -> {
                    }
                }

                this.comboReady = true;
            }
        }
    }

    @EventHandler
    private void onKey(KeyPressEvent keypressevent) {
        if (keypressevent.action == 1) {
            if (mc.currentScreen == null && !KeybindSetting.isAnyListening()) {
                int i = keypressevent.key;
                if (i != 0) {
                    if (this.nextMode.getKey() != 0 && i == this.nextMode.getKey()) {
                        this.runComboStep(1);
                    } else if (this.prevMode.getKey() != 0 && i == this.prevMode.getKey()) {
                        this.runComboStep(-1);
                    }
                }
            }
        }
    }
}
