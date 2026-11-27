package lol.lwes.voil.module.crystalpvp;

import java.util.function.Function;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.InputsHeadEvent;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.mixin.accessor.KeyBindingAccessor;
import lol.lwes.voil.mixin.accessor.MinecraftClientAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.crystalpvp.AutoHitCrystalState;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.CombatUtil;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;

public class AutoHitCrystal extends Module {
    private final BooleanSetting preferBiggestStack;
    private final KeybindSetting activateKey;
    private final BooleanSetting switchBack;
    private final RangeSetting obsidianDelay;
    private final RangeSetting switchDelay;
    private final RangeSetting switchBackDelay;
    private final ItemListSetting activationItems;
    private final AutoHitCrystalState idleState;
    private final AutoHitCrystalState switchBackState;
    private final AutoHitCrystalState finishedState;
    private final AutoHitCrystalState activateCrystalState;
    private final AutoHitCrystalState placeCrystalState;
    private final AutoHitCrystalState selectCrystalState;
    private final AutoHitCrystalState placeObsidianState;
    private final AutoHitCrystalState selectObsidianState;
    private int obsidianDelayTicks;
    private int switchDelayTicks;
    private int switchBackDelayTicks;
    private boolean active;
    private AutoHitCrystalState currentState;
    private int originalSlot;
    private int savedActivateKey;

    public AutoHitCrystal() {
        super("AutoHitCrystal", "Auto obsidian and crystal placement, Modernized", Category.CrystalPvP);
        this.preferBiggestStack = new BooleanSetting("Prefer biggest stack", true, "Prefers the biggest crystal stack in hotbar");
        this.activateKey = new KeybindSetting("Activate key", 1, false, "Key to activate");
        this.switchBack = new BooleanSetting("Switch back", false, "Switches back to previous item after crystalling");
        this.obsidianDelay = new RangeSetting("Obsidian delay", 0.0, 5.0, 1.0, 2.0, 1.0, "Delay before placing obsidian");
        this.switchDelay = new RangeSetting("Switch delay", 0.0, 10.0, 0.0, 1.0, 1.0, "Delay before switching crystal");
        this.switchBackDelay = new RangeSetting("Switch back delay", 0.0, 10.0, 2.0, 3.0, 1.0, "Delay before switching back to previous item");
        Item[] aitem1 = new Item[]{Items.NETHERITE_SWORD, Items.TOTEM_OF_UNDYING};
        ItemListSetting itemlistsetting = new ItemListSetting("Activation Items", "Items that allow activation when held in main hand", aitem1);
        Item[] aitem = new Item[]{Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.TOTEM_OF_UNDYING};
        this.activationItems = itemlistsetting.setRecommendedItems(aitem).allowCategoryMatch();
        this.idleState = new AutoHitCrystalState(null, null);
        this.switchBackState = new AutoHitCrystalState(null, hotbarkeyevent -> {
            if (!this.switchBack.isEnabled() || hotbarkeyevent.getSlot() != this.originalSlot) {
                return null;
            }

            if (this.switchBackDelayTicks > 0) {
                int i = this.switchBackDelayTicks--;
                return null;
            }

            hotbarkeyevent.addTimesPressed(1);
            AutoCrystal2 autocrystal2 = (AutoCrystal2)ModuleManager.INSTANCE.getModule(AutoCrystal2.class);
            if (autocrystal2 != null) {
                autocrystal2.activate.setKey(this.savedActivateKey);
                autocrystal2.setEnabled(false);
            }

            return this.idleState;
        });
        this.finishedState = new AutoHitCrystalState(null, null);
        Function<UseKeyEvent, AutoHitCrystalState> function = usekeyevent -> {
            AutoCrystal2 autocrystal2 = (AutoCrystal2)ModuleManager.INSTANCE.getModule(AutoCrystal2.class);
            if (autocrystal2 != null) {
                autocrystal2.setEnabled(true);
                this.savedActivateKey = autocrystal2.activate.getKey();
                autocrystal2.activate.setKey(this.activateKey.getKey());
                return this.finishedState;
            } else {
                return null;
            }
        };
        this.activateCrystalState = new AutoHitCrystalState(function, null);
        Function<UseKeyEvent, AutoHitCrystalState> function1 = usekeyevent -> {
            if (mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK) {
                BlockPos blockpos = blockhitresult.getBlockPos();
                if (EntityUtil.isCrystalBase(blockpos)) {
                    usekeyevent.addTimesPressed(1);
                    return this.activateCrystalState;
                }
            }

            return null;
        };
        this.placeCrystalState = new AutoHitCrystalState(function1, null);
        this.selectCrystalState = new AutoHitCrystalState(null, hotbarkeyevent -> {
            ItemStack itemstack = mc.player.getInventory().getStack(hotbarkeyevent.getSlot());
            if (!itemstack.isOf(Items.END_CRYSTAL) || hotbarkeyevent.getSlot() != this.findCrystalSlot()) {
                return null;
            } else if (this.switchDelayTicks > 0) {
                int i = this.switchDelayTicks--;
                return null;
            } else {
                hotbarkeyevent.addTimesPressed(1);
                return this.placeCrystalState;
            }
        });
        Function<UseKeyEvent, AutoHitCrystalState> function2 = usekeyevent -> {
            if (mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK) {
                if (mc.world.getBlockState(blockhitresult.getBlockPos()).getBlock() == Blocks.OBSIDIAN) {
                    return this.selectCrystalState;
                }

                BlockPos blockpos = blockhitresult.getBlockPos().offset(blockhitresult.getSide());
                if (EntityUtil.canPlaceCrystalClient(blockpos)) {
                    if (this.obsidianDelayTicks > 0) {
                        int i = this.obsidianDelayTicks--;
                        return null;
                    }

                    usekeyevent.addTimesPressed(1);
                    return this.selectCrystalState;
                }
            }

            return null;
        };
        this.placeObsidianState = new AutoHitCrystalState(function2, null);
        this.selectObsidianState = new AutoHitCrystalState(null, hotbarkeyevent -> {
            ItemStack itemstack = mc.player.getInventory().getStack(hotbarkeyevent.getSlot());
            if (mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK) {
                BlockPos blockpos = blockhitresult.getBlockPos();
                if (EntityUtil.isCrystalBase(blockpos)) {
                    return this.selectCrystalState;
                }
            }

            if (!itemstack.isOf(Items.OBSIDIAN) && !itemstack.isOf(Items.BEDROCK)) {
                return null;
            }

            hotbarkeyevent.addTimesPressed(1);
            return this.placeObsidianState;
        });
        this.obsidianDelayTicks = 0;
        this.switchDelayTicks = 0;
        this.switchBackDelayTicks = 0;
        Setting[] asetting = new Setting[]{
            this.activationItems, this.preferBiggestStack, this.activateKey, this.switchBack, this.obsidianDelay, this.switchDelay, this.switchBackDelay
        };
        this.addSettings(asetting);
    }

    private boolean canActivate() {
        if (Module.isNotInGame()) {
            return false;
        } else if (mc.currentScreen instanceof HandledScreen) {
            return false;
        } else if (InventoryUtil.findHotbarSlot(Items.END_CRYSTAL) == -1) {
            return false;
        } else {
            return InventoryUtil.findHotbarSlot(Items.OBSIDIAN) == -1 && InventoryUtil.findHotbarSlot(Items.BEDROCK) == -1
                ? false
                : mc.world
                        .raycast(
                            new RaycastContext(
                                mc.player.getCameraPosVec(1.0F),
                                mc.player.getCameraPosVec(1.0F).add(mc.player.getRotationVec(1.0F).multiply(CombatUtil.getBlockReach())),
                                ShapeType.OUTLINE,
                                FluidHandling.NONE,
                                mc.player
                            )
                        )
                        .getType()
                    == Type.BLOCK;
        }
    }

    @Override
    public void onEnable() {
        this.active = false;
        this.reset();
        this.currentState = this.idleState;
    }

    private void reset() {
        this.obsidianDelayTicks = this.obsidianDelay.getRandomInt();
        this.switchDelayTicks = this.switchDelay.getRandomInt();
        this.switchBackDelayTicks = this.switchBackDelay.getRandomInt();
        this.originalSlot = mc.player != null ? mc.player.getInventory().getSelectedSlot() : 0;
        this.currentState = this.selectObsidianState;
    }

    private void consumeUseKey() {
        if (((KeyBindingAccessor)mc.options.useKey).getBoundKey().getCode() == this.activateKey.getKey()) {
            while (mc.options.useKey.wasPressed()) {
            }

            mc.options.useKey.setPressed(false);
            ((MinecraftClientAccessor)mc).setItemUseCooldown(4);
        }
    }

    private int findCrystalSlot() {
        int i = 0;
        int j = -1;

        for (int k = 0; k < 9; k++) {
            ItemStack itemstack = mc.player.getInventory().getStack(k);
            if (itemstack.isOf(Items.END_CRYSTAL) && itemstack.getCount() > i) {
                if (!this.preferBiggestStack.isEnabled()) {
                    return k;
                }

                i = itemstack.getCount();
                j = k;
            }
        }

        return j;
    }

    @EventHandler
    public void onSwitchSlot(HotbarKeyEvent hotbarkeyevent) {
        if (this.canActivate()) {
            this.currentState = (AutoHitCrystalState)this.currentState.applyHotbar(hotbarkeyevent);
        }
    }

    @EventHandler
    public void onInputsHead(InputsHeadEvent inputsheadevent) {
        if (this.canActivate()) {
            if (this.activateKey.isPressed()) {
                if (!this.active) {
                    ItemStack itemstack = mc.player.getMainHandStack();
                    ItemListSetting itemlistsetting = this.activationItems;
                    Item item = itemstack.getItem();
                    if (!itemlistsetting.matches(item)) {
                        return;
                    }

                    this.active = true;
                    this.reset();
                }
            } else {
                if (this.active && this.currentState == this.finishedState) {
                    this.currentState = this.switchBackState;
                }

                this.active = false;
            }

            this.consumeUseKey();
        }
    }

    @EventHandler
    public void onRightClick(UseKeyEvent usekeyevent) {
        if (this.canActivate()) {
            this.currentState = (AutoHitCrystalState)this.currentState.applyUse(usekeyevent);
        }
    }
}
