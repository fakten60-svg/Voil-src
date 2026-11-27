package lol.lwes.voil.module.miscellaneous;

import java.util.function.BooleanSupplier;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.packet.PacketUtil;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;

public class AutoTool extends Module {
    private final BooleanSetting switchBack = new BooleanSetting("Switch back", true, "Switch back to old slot after you stop mining");
    private final BooleanSetting autoEquipFromInventory = new BooleanSetting(
        "Auto Equip from Inventory", true, "Automatically switch to the best tool from inventory"
    );
    private final ModeSetting invSwap = new ModeSetting(
        "Inv Swap", "Packet", "Whetever to wait or use packets to swap items in inventory", new String[]{"Packet", "Wait"}
    );
    private final BooleanSetting returnToolToSlot = new BooleanSetting(
        "Return Tool to Slot", true, "Put the tool back in the original slot after getting it from inventory"
    );
    private boolean hasSwitched;
    private int lastSlot = -1;
    private int toolSlot;
    private int originalSlot;
    private int tickCounter;

    public AutoTool() {
        super("AutoTool", "Automatically switches to best tool", Category.Miscellaneous);
        this.toolSlot = -1;
        this.originalSlot = -1;
        Setting[] asetting = new Setting[]{this.switchBack, this.autoEquipFromInventory, this.invSwap, this.returnToolToSlot};
        this.addSettings(asetting);
        ModeSetting modesetting = this.invSwap;
        BooleanSupplier booleansupplier = this.autoEquipFromInventory::isEnabled;
        modesetting.setVisibleWhen(booleansupplier);
    }

    private boolean isPacketMode() {
        return mc.currentScreen != null ? false : this.invSwap.is("Packet") || this.tickCounter >= 2;
    }

    private static float getMiningSpeed(ItemStack itemstack, BlockState blockstate, RegistryWrapper registrywrapper) {
        if (itemstack.isEmpty()) {
            return 0.0F;
        }

        if (blockstate.isToolRequired() && !itemstack.isSuitableFor(blockstate)) {
            return 0.0F;
        }

        float f = itemstack.getMiningSpeedMultiplier(blockstate);
        if (f <= 1.0F) {
            return 0.0F;
        }

        RegistryKey registrykey = Enchantments.EFFICIENCY;
        ItemStack itemstack1 = itemstack;
        int i = InventoryUtil.getEnchantmentLevel(itemstack1, registrykey, registrywrapper);
        if (i > 0) {
            int j = i * i;
            f += j + 1;
        }

        return f;
    }

    private void swapSlot(int i, int j) {
        boolean flag = this.invSwap.is("Packet") && this.tickCounter < 2;
        PlayerInput playerinput = mc.player.input.playerInput;
        boolean flag1 = mc.player.isSprinting();
        if (flag) {
            if (flag1) {
                ClientCommandC2SPacket clientcommandc2spacket = new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING);
                PacketUtil.sendPacket(clientcommandc2spacket);
            }

            PlayerInputC2SPacket playerinputc2spacket = new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, playerinput.sneak(), false));
            PacketUtil.sendPacket(playerinputc2spacket);
        }

        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, InventoryUtil.getContainerSlotId(i), j, SlotActionType.SWAP, mc.player);
        if (flag) {
            PlayerInputC2SPacket playerinputc2spacket1 = new PlayerInputC2SPacket(playerinput);
            PacketUtil.sendPacket(playerinputc2spacket1);
            if (flag1) {
                ClientCommandC2SPacket clientcommandc2spacket1 = new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING);
                PacketUtil.sendPacket(clientcommandc2spacket1);
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            int j1;
            if (this.isSprinting()) {
                int k = this.tickCounter;
                j1 = k + 1;
            } else {
                j1 = 0;
            }

            this.tickCounter = j1;
            BlockState blockstate = this.getTargetState();
            if (blockstate == null) {
                if (this.hasSwitched) {
                    this.resetSlots();
                }

                if (!this.hasSwitched && this.toolSlot != -1 && this.returnToolToSlot.isEnabled() && this.isPacketMode()) {
                    int l = this.originalSlot;
                    this.swapSlot(this.toolSlot, l);
                    this.originalSlot = -1;
                    this.toolSlot = -1;
                }
            } else {
                int i = mc.player.getInventory().getSelectedSlot();
                boolean flag = this.autoEquipFromInventory.isEnabled() && this.isPacketMode();
                int j = this.findBestToolSlot(blockstate, i, flag);
                if (j != -1) {
                    if (!this.hasSwitched) {
                        this.hasSwitched = true;
                        this.lastSlot = i;
                    }

                    if (j != i) {
                        if (InventoryUtil.isHotbarSlot(j)) {
                            InventoryUtil.selectSlot(j);
                        } else {
                            if (this.toolSlot != -1) {
                                int i1 = this.originalSlot;
                                this.swapSlot(this.toolSlot, i1);
                                this.originalSlot = -1;
                                this.toolSlot = -1;
                            }

                            this.swapSlot(j, i);
                            this.toolSlot = j;
                            this.originalSlot = i;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && this.hasSwitched) {
            this.resetSlots();
        }

        this.hasSwitched = false;
        this.originalSlot = -1;
        AutoTool autotool1 = this;
        autotool1.toolSlot = -1;
        this.lastSlot = -1;
        this.tickCounter = 0;
    }

    private boolean isSprinting() {
        return !mc.player.isSprinting()
            && !mc.options.forwardKey.isPressed()
            && !mc.options.backKey.isPressed()
            && !mc.options.leftKey.isPressed()
            && !mc.options.rightKey.isPressed()
            && !mc.options.jumpKey.isPressed()
            && !mc.options.sprintKey.isPressed();
    }

    private int findBestToolSlot(BlockState blockstate, int i, boolean flag) {
        Registry registry = mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        int j = -1;
        float f = getMiningSpeed(mc.player.getInventory().getStack(i), blockstate, registry);
        if (f > 0.0F) {
            j = i;
        }

        int k = flag ? 35 : 8;

        for (int l = 0; l <= k; l++) {
            if (l != i) {
                float f1 = getMiningSpeed(mc.player.getInventory().getStack(l), blockstate, registry);
                if (f1 > f) {
                    f = f1;
                    j = l;
                }
            }
        }

        return j;
    }

    private void resetSlots() {
        if (this.switchBack.isEnabled() && this.lastSlot != -1 && mc.player.getInventory().getSelectedSlot() != this.lastSlot) {
            int i = this.lastSlot;
            InventoryUtil.selectSlot(i);
        }

        this.hasSwitched = false;
        this.lastSlot = -1;
        if (!this.returnToolToSlot.isEnabled()) {
            this.originalSlot = -1;
            this.toolSlot = -1;
        }
    }

    private BlockState getTargetState() {
        if (mc.currentScreen == null && mc.options.attackKey.isPressed()) {
            if (mc.crosshairTarget instanceof BlockHitResult blockhitresult && blockhitresult.getType() == Type.BLOCK) {
                BlockState blockstate = mc.world.getBlockState(blockhitresult.getBlockPos());
                return blockstate.isAir() ? null : blockstate;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
