package lol.lwes.voil.module.crystalpvp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.TaskExecuteEvent;
import lol.lwes.voil.mixin.accessor.HandledScreenAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.crystalpvp.AutoTotemState;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.DelayTimer;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.render.UiRect;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public class AutoTotem extends Module {
    private final ModeSetting mode = new ModeSetting(
        "mode", "InventoryTotem", "Mode to use for toteming", new String[]{"InventoryTotem", "LegitTotem", "AutoTotem"}
    );
    private final RangeSetting autoDelayMs = new RangeSetting("Auto Delay (ms)", 0.0, 600.0, 200.0, 300.0, 1.0, "Delay before toteming");
    private final RangeSetting manualDelayMs = new RangeSetting("Manual Delay (ms)", 0.0, 600.0, 40.0, 120.0, 1.0, "Delay before toteming");
    private final RangeSetting openInventoryDelayMs = new RangeSetting("Open Inventory Delay (ms)", 0.0, 500.0, 300.0, 500.0, 1.0, "Delay before opening inventory after toteming");
    private final RangeSetting closeInventoryDelayMs = new RangeSetting("Close Inventory Delay (ms)", 0.0, 500.0, 200.0, 300.0, 1.0, "Delay before closing inventory after toteming");
    private final BooleanSetting fillHotbar = new BooleanSetting("Fill Hotbar", false, "Fill hotbar automatically");
    private final BooleanSetting forceFillHotbar = new BooleanSetting("Force Fill Hotbar", false, "Force fill hotbar even if totem in offhand");
    private final NumberSetting hotbarTotemSlot = new NumberSetting("Hotbar Totem Slot", 1.0, 9.0, 8.0, 1.0, "Slot to use");
    private final BooleanSetting autoInventory = new BooleanSetting("Auto Inventory", false, "Auto open inventory");
    private final BooleanSetting autoCloseInventory = new BooleanSetting("Auto Close Inventory", true, "Auto close inventory");
    private final BooleanSetting onKey = new BooleanSetting("On Key", false, "Only works on key");
    private final KeybindSetting keybind = new KeybindSetting("Keybind", 0, false, "Keybind for On Key");
    private final AutoTotemState closeInventoryState = new AutoTotemState(taskexecuteevent -> {
        if (this.needsTotem()) {
            this.openDelayTimer.start();
            return this.openInventoryState;
        }

        if (mc.currentScreen == null) {
            this.reset();
            return this.idleState;
        }

        if (this.autoCloseInventory.isEnabled() && this.closeDelayTimer.hasFinished()) {
            mc.currentScreen.close();
        }

        return null;
    });
    private final AutoTotemState swapTotemState = new AutoTotemState(taskexecuteevent -> {
        if (this.canMoveTotem()) {
            return this.closeInventoryState;
        }

        if (!(mc.currentScreen instanceof InventoryScreen inventoryscreen)) {
            if (this.mode.is("InventoryTotem")) {
                this.inventoryOpenedByModule = true;
            }

            this.openDelayTimer.start();
            return this.openInventoryState;
        } else {
            if (this.targetSlots == null || this.targetSlots.isEmpty() || !((Slot)this.targetSlots.get(0)).getStack().isOf(CombatUtil.getTotemItem())) {
                this.targetSlots = this.findSlotDuoCloseToCursor(inventoryscreen.getScreenHandler().slots, this.antiTotemTargetSlotPred());
            }

            if (!this.targetSlots.isEmpty()) {
                int j = this.hotbarTotemSlot.getInt();
                int k1 = j + -1;
                if (this.mode.is("LegitTotem")) {
                    Supplier<Boolean> supplier = this.manualDelayTimer::hasFinished;
                    Boolean obool = this.tryMoveTotem(inventoryscreen, supplier);
                    if (obool == null || !obool) {
                        return null;
                    }
                } else {
                    switch (this.mode.getMode()) {
                        case "InventoryTotem" -> {
                            if (GLFW.glfwGetPlatform() == 393219) {
                                this.mode.setMode("AutoTotem");
                                return null;
                            }

                            if (this.slotsHash != this.targetSlots.hashCode()) {
                                this.slotsHash = this.targetSlots.hashCode();
                                this.cursorStart = new Vector2d(mc.mouse.getScaledX(mc.getWindow()), mc.mouse.getScaledY(mc.getWindow()));
                                this.cursorMoveTimer.stop();
                            }

                            this.handleAutoCursor(inventoryscreen, this.targetSlots);
                            Supplier<Boolean> supplier1 = this.autoDelayTimer::hasFinished;
                            Boolean obool1 = this.tryMoveTotem(inventoryscreen, supplier1);
                            if (obool1 == null) {
                                return null;
                            }

                            if (!obool1) {
                                this.targetSlots.remove(0);
                                return null;
                            }
                        }
                        case "AutoTotem" -> {
                            if (!mc.player.getOffHandStack().isOf(CombatUtil.getTotemItem())) {
                                if (this.autoDelayTimer.hasFinished()) {
                                    int l = ((Slot)this.targetSlots.remove(0)).id;
                                    InventoryUtil.clickSlot(l, 40, SlotActionType.SWAP);
                                }

                                return null;
                            }

                            if (this.fillHotbar.isEnabled() && !mc.player.getInventory().getStack(k1).isOf(CombatUtil.getTotemItem())) {
                                if (this.autoDelayTimer.hasFinished()) {
                                    int i1 = k1;
                                    int j1 = ((Slot)this.targetSlots.remove(0)).id;
                                    InventoryUtil.clickSlot(j1, i1, SlotActionType.SWAP);
                                }

                                return null;
                            }
                        }
                        default -> {
                        }
                    }
                }
            }

            this.inventoryOpenedByModule = false;
            this.closeDelayTimer.start();
            return this.closeInventoryState;
        }
    });
    private final AutoTotemState openInventoryState = new AutoTotemState(taskexecuteevent -> {
        if (mc.currentScreen instanceof InventoryScreen) {
            return this.swapTotemState;
        }

        if (this.autoInventory.isEnabled() && !this.inventoryOpenedByModule && this.openDelayTimer.hasFinished()) {
            mc.setScreen(new InventoryScreen(mc.player));
        }

        return null;
    });
    private final AutoTotemState idleState = new AutoTotemState(taskexecuteevent -> {
        if (this.needsTotem()) {
            this.openDelayTimer.start();
            return this.openInventoryState;
        } else {
            return null;
        }
    });
    private AutoTotemState currentState;
    private boolean inventoryOpenedByModule;
    private List<Slot> targetSlots;
    private final DelayTimer autoDelayTimer = new DelayTimer(this.autoDelayMs);
    private final DelayTimer manualDelayTimer = new DelayTimer(this.manualDelayMs);
    private final DelayTimer openDelayTimer = new DelayTimer(this.openInventoryDelayMs);
    private final DelayTimer closeDelayTimer = new DelayTimer(this.closeInventoryDelayMs);
    private final DelayTimer cursorMoveTimer = new DelayTimer(() -> (int)(this.autoDelayMs.getValueMax() * 2.0));
    private int slotsHash;
    private Vector2d cursorStart;

    public AutoTotem() {
        super("AutoTotem", "Automatically re-totems, modernized", Category.CrystalPvP);
        Setting.dependSetting(this.closeInventoryDelayMs, this.autoCloseInventory);
        Setting.dependSetting(this.openInventoryDelayMs, this.autoInventory);
        Setting.dependSetting(this.hotbarTotemSlot, this.fillHotbar);
        Setting.dependSetting(this.keybind, this.onKey);
        ModeSetting modesetting = this.mode;
        Setting.dependSetting(this.manualDelayMs, "LegitTotem", modesetting);
        Setting.dependAny(this.autoDelayMs, this.mode, new String[]{"InventoryTotem", "AutoTotem"});
        Setting.dependSetting(this.forceFillHotbar, this.fillHotbar);
        Setting[] asetting = new Setting[]{
            this.mode,
            this.autoDelayMs,
            this.manualDelayMs,
            this.openInventoryDelayMs,
            this.closeInventoryDelayMs,
            this.fillHotbar,
            this.forceFillHotbar,
            this.hotbarTotemSlot,
            this.autoInventory,
            this.autoCloseInventory,
            this.onKey,
            this.keybind
        };
        this.addSettings(asetting);
    }

    private void handleAutoCursor(HandledScreen handledscreen, List<Slot> list) {
        List<Slot> list1 = list;
        HandledScreen handledscreen2 = handledscreen;
        this.autoDelayTimer.start();
        this.cursorMoveTimer.start();
        Slot slot = list1.get(0);
        HandledScreen handledscreen1 = handledscreen2;
        Vector2d vector2d = this.getSlotScreenPos(handledscreen1, slot, null).getCenter();
        double d0 = MathHelper.lerp(this.cursorMoveTimer.getProgress(), this.cursorStart.x, vector2d.x);
        double d1 = MathHelper.lerp(this.cursorMoveTimer.getProgress(), this.cursorStart.y, vector2d.y);
        double d2 = mc.getWindow().getScaleFactor();
        d0 *= d2;
        d1 *= d2;
        if (Double.isFinite(d0) && Double.isFinite(d1)) {
            double d3 = d0;
            double d4 = d1;
            mc.execute(() -> GLFW.glfwSetCursorPos(mc.getWindow().getHandle(), d3, d4));
        }
    }

    private List<Slot> findSlotDuoCloseToCursor(List<Slot> list, Predicate<Slot> predicate) {
        ArrayList<Slot> arraylist = new ArrayList<>();
        Vector2d vector2d = this.getCursorSlotPos();
        Slot slot = this.findClosestSlot(list, vector2d, predicate);
        if (slot == null) {
            return arraylist;
        }

        arraylist.add(slot);
        Vector2d vector2d2 = new Vector2d(slot.x, slot.y);
        Predicate<Slot> predicate1 = predicate.and(slot3 -> slot3.id != slot.id);
        Vector2d vector2d1 = vector2d2;
        Slot slot1 = this.findClosestSlot(list, vector2d1, predicate1);
        if (slot1 != null) {
            arraylist.add(slot1);
        }

        return arraylist;
    }

    private Slot findClosestSlot(List<Slot> list, Vector2d vector2d, Predicate<Slot> predicate) {
        Predicate<Slot> predicate1 = predicate;
        vector2d = vector2d;
        Vector2d vector2d1 = new Vector2d(Double.MAX_VALUE, Double.MAX_VALUE);
        Vector2d vector2d2 = new Vector2d();
        Slot slot = null;

        for (Slot slot1 : list) {
            vector2d2.x = slot1.x;
            vector2d2.y = slot1.y;
            if (predicate1.test(slot1) && vector2d2.distance(vector2d) < vector2d1.distance(vector2d)) {
                vector2d1.set(vector2d2);
                slot = slot1;
            }
        }

        return slot;
    }

    public void reset() {
        this.autoDelayTimer.stop();
        this.manualDelayTimer.stop();
        this.openDelayTimer.stop();
        this.closeDelayTimer.stop();
        this.cursorMoveTimer.stop();
        this.currentState = this.idleState;
        this.targetSlots = null;
        this.slotsHash = 0;
        this.cursorStart = null;
    }

    private UiRect getSlotScreenPos(HandledScreen handledscreen, Slot slot, UiRect uirect) {
        if (uirect == null) {
            uirect = new UiRect();
        }

        HandledScreenAccessor handledscreenaccessor = (HandledScreenAccessor)handledscreen;
        uirect.x = handledscreenaccessor.getX() + slot.x - 1;
        uirect.y = handledscreenaccessor.getY() + slot.y - 1;
        uirect.width = 17;
        uirect.height = 17;
        return uirect;
    }

    private boolean needsTotem() {
        if (this.onKey.isEnabled() && !this.keybind.isPressed()) {
            return false;
        }

        if (!mc.player.getOffHandStack().isOf(CombatUtil.getTotemItem())) {
            return true;
        }

        if (this.forceFillHotbar.isEnabled()) {
            PlayerInventory playerinventory = mc.player.getInventory();
            int i = this.hotbarTotemSlot.getInt();
            if (!playerinventory.getStack((((i | -2) << 1) - (i ^ -2) ^ 1) + ((((i | -2) << 1) - (i ^ -2) & 1) << 1)).isOf(CombatUtil.getTotemItem())) {
                return true;
            }
        }

        return false;
    }

    private boolean canMoveTotem() {
        if (this.needsTotem()
            || this.fillHotbar.isEnabled() && !mc.player.getInventory().getStack(this.hotbarTotemSlot.getInt() - 1).isOf(CombatUtil.getTotemItem())) {
            Item[] aitem = new Item[]{CombatUtil.getTotemItem()};
            if (InventoryUtil.findHotbar(aitem).isFound()) {
                return false;
            }
        }

        return true;
    }

    private boolean hasTotemInInventory() {
        return Module.isNotInGame() ? false : InventoryUtil.findInventorySlot(CombatUtil.getTotemItem()) != -1;
    }

    private Boolean tryMoveTotem(InventoryScreen inventoryscreen, Supplier<Boolean> supplier) {
        int i = this.hotbarTotemSlot.getInt() - 1;
        Slot slot = ((HandledScreenAccessor)inventoryscreen).getFocusedSlot();
        if (slot != null && this.antiTotemTargetSlotPred().test(slot)) {
            if (!mc.player.getOffHandStack().isOf(CombatUtil.getTotemItem())) {
                if ((Boolean)supplier.get()) {
                    int j = slot.id;
                    InventoryUtil.clickSlot(j, 40, SlotActionType.SWAP);
                    return false;
                }
            } else {
                if (!this.fillHotbar.isEnabled() || mc.player.getInventory().getStack(i).isOf(CombatUtil.getTotemItem())) {
                    return true;
                }

                if ((Boolean)supplier.get()) {
                    int k = slot.id;
                    InventoryUtil.clickSlot(k, i, SlotActionType.SWAP);
                    return false;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private Predicate<Slot> antiTotemTargetSlotPred() {
        boolean flag = !mc.player.getOffHandStack().isOf(CombatUtil.getTotemItem());
        return slot -> {
            if (slot.getIndex() == 40) {
                return false;
            }

            if (!flag && this.fillHotbar.isEnabled() && slot.getIndex() == this.hotbarTotemSlot.getInt() - 1) {
                return false;
            }

            return slot.getStack().isOf(CombatUtil.getTotemItem());
        };
    }

    private Vector2d getCursorSlotPos() {
        double d0 = mc.mouse.getScaledX(mc.getWindow());
        double d1 = mc.mouse.getScaledY(mc.getWindow());
        if (mc.currentScreen instanceof HandledScreen<?> handledscreen) {
            d0 -= ((HandledScreenAccessor)handledscreen).getX();
            d1 -= ((HandledScreenAccessor)handledscreen).getY();
        }

        return new Vector2d(d0, d1);
    }

    private double logInterpolate(double d0, double d2, double d4, double d6, double d8) {
        double d12 = d0;
        double d13 = d2;
        double d14 = d4;
        double d15 = d6;
        double d16 = d8;
        double d1 = d12;
        double d3 = d13;
        double d5 = d14;
        double d7 = d15;
        double d9 = d16;
        double d10 = Math.min(d3, d5) - 1.0;
        d3 -= d10;
        d5 -= d10;
        d1 -= d10;
        double d11 = (d7 - d9) / Math.log(d3 / d5);
        return -d11 * Math.log(d3) + d7 + d11 * Math.log(d1);
    }

    @EventHandler
    public void onMcTaskExec(TaskExecuteEvent taskexecuteevent) {
        if (this.hasTotemInInventory()) {
            this.currentState = (AutoTotemState)this.currentState.applyTick(taskexecuteevent);
        }
    }

    @Override
    public void onEnable() {
        this.reset();
    }
}
