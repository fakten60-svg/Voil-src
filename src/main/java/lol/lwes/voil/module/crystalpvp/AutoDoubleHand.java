package lol.lwes.voil.module.crystalpvp;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.NamedSetEntry;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;

public class AutoDoubleHand extends Module {
    private final NumberSetting health = new NumberSetting("Health", 1.0, 20.0, 10.0, 1.0, "Minimum health to swap to totem");
    private final MultiSelectSetting conditions;
    private final NamedSetEntry airCondition;
    private final NamedSetEntry afterPopCondition;
    private final NamedSetEntry swordCondition;
    private final NamedSetEntry obsidianCondition;
    private final NamedSetEntry crystalCondition;
    private final BooleanSetting dontStick;
    private final BooleanSetting inventory;
    private final BooleanSetting autoOffhand;
    private final NumberSetting offhandDelay;
    private final ItemListSetting blacklist;
    private boolean wasHoldingTotem;
    private int swapCooldown;
    private int postSwapCooldown;
    private int offhandDelayTicks;

    public AutoDoubleHand() {
        super("AutoDoubleHand", "Automatically swaps totem on certain conditions", Category.CrystalPvP);
        List list = List.of("Air", "After Pop", "Sword", "Obsidian", "Crystal");
        this.conditions = MultiSelectSetting.create("Conditions", "When to double hand", list, new String[]{"Air", "After Pop", "Sword", "Obsidian", "Crystal"});
        this.airCondition = this.conditions.getOption("Air");
        this.afterPopCondition = this.conditions.getOption("After Pop");
        this.swordCondition = this.conditions.getOption("Sword");
        this.obsidianCondition = this.conditions.getOption("Obsidian");
        this.crystalCondition = this.conditions.getOption("Crystal");
        this.dontStick = new BooleanSetting("Dont stick", false, "Only switches to totem once");
        this.inventory = new BooleanSetting("Inventory", true, "Automatically doublehands if in inventory");
        this.autoOffhand = new BooleanSetting("Auto Offhand", true, "Automatically switches totem from hotbar to offhand");
        this.offhandDelay = new NumberSetting("Offhand Delay", 0.0, 20.0, 0.0, 1.0, "Ticks to wait before moving the totem to offhand");
        ItemListSetting itemlistsetting = new ItemListSetting("Blacklist", "Don't swap while holding these items");
        Item[] aitem = new Item[]{Items.ENDER_PEARL, Items.OBSIDIAN, Items.RESPAWN_ANCHOR, Items.GLOWSTONE, Items.END_CRYSTAL};
        this.blacklist = itemlistsetting.setRecommendedItems(aitem);
        Setting[] asetting = new Setting[]{this.health, this.conditions, this.inventory, this.dontStick, this.autoOffhand, this.offhandDelay, this.blacklist};
        this.addSettings(asetting);
    }

    private boolean isAllowedByAnchor() {
        AutoAnchor autoanchor = (AutoAnchor)ModuleManager.INSTANCE.getModule(AutoAnchor.class);
        return autoanchor == null || !autoanchor.ignoreDoubleHand.isEnabled() || autoanchor.currentState != autoanchor.chargeState;
    }

    private boolean isEnemyAimingAtBlock() {
        for (PlayerEntity playerentity : mc.world.getPlayers()) {
            if (playerentity != mc.player) {
                Vec3d vec3d = playerentity.getEyePos();
                RaycastContext raycastcontext = new RaycastContext(
                    vec3d, vec3d.add(playerentity.getRotationVec(1.0F).multiply(6.0)), ShapeType.OUTLINE, FluidHandling.NONE, playerentity
                );
                BlockHitResult blockhitresult = mc.world.raycast(raycastcontext);
                if (blockhitresult.getType() == Type.BLOCK) {
                    Block block = mc.world.getBlockState(blockhitresult.getBlockPos()).getBlock();
                    if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.RESPAWN_ANCHOR) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isAnyCrystalTargeted() {
        List list = mc.world.getEntitiesByClass(EndCrystalEntity.class, mc.player.getBoundingBox().expand(10.0), endcrystalentity -> true);
        if (list.isEmpty()) {
            return false;
        }

        for (PlayerEntity playerentity : mc.world.getPlayers()) {
            if (playerentity != mc.player) {
                Vec3d vec3d = playerentity.getEyePos();
                Vec3d vec3d1 = vec3d.add(playerentity.getRotationVec(1.0F).multiply(6.0));
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    if (((EndCrystalEntity)iterator.next()).getBoundingBox().raycast(vec3d, vec3d1).isPresent()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void onTick() {
        if (this.isEnabled() && mc.player != null && mc.world != null) {
            if (this.swapCooldown > 0) {
                int k = this.swapCooldown--;
            }

            if (this.postSwapCooldown > 0) {
                int l = this.postSwapCooldown--;
            }

            int i = this.offhandDelayTicks;
            this.offhandDelayTicks = 0;
            ClientPlayerEntity clientplayerentity = mc.player;
            if (this.inventory.isEnabled() && mc.currentScreen instanceof InventoryScreen) {
                if (this.autoOffhand.isEnabled()) {
                    ScreenHandler screenhandler = clientplayerentity.currentScreenHandler;
                    if (!clientplayerentity.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) && screenhandler == clientplayerentity.playerScreenHandler) {
                        int j = InventoryUtil.findInventorySlot(Items.TOTEM_OF_UNDYING);
                        if (j >= 0 && this.checkOffhandDelay(i)) {
                            mc.interactionManager
                                .clickSlot(screenhandler.syncId, InventoryUtil.getContainerSlotId(j), 40, SlotActionType.SWAP, clientplayerentity);
                            this.swapCooldown = 3;
                            InventoryUtil.hasItemInHotbar(Items.TOTEM_OF_UNDYING);
                        }
                    }
                }
            } else if (this.isAllowedByAnchor() && this.shouldDoubleHand()) {
                Item item = clientplayerentity.getMainHandStack().getItem();
                if (item != Items.TOTEM_OF_UNDYING) {
                    if (this.blacklist.matches(item)) {
                        return;
                    }
                }

                if (this.autoOffhand.isEnabled() && !clientplayerentity.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) && this.swapCooldown == 0) {
                    if (!clientplayerentity.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                        InventoryUtil.hasItemInHotbar(Items.TOTEM_OF_UNDYING);
                        if (!clientplayerentity.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                            return;
                        }
                    }

                    if (this.checkOffhandDelay(i)) {
                        mc.getNetworkHandler().getConnection().send(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                        this.swapCooldown = 3;
                    }
                } else if (this.swapCooldown <= 0) {
                    if (!clientplayerentity.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                        if (!this.dontStick.isEnabled() || !this.wasHoldingTotem) {
                            InventoryUtil.hasItemInHotbar(Items.TOTEM_OF_UNDYING);
                            this.wasHoldingTotem = clientplayerentity.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.wasHoldingTotem = false;
        this.swapCooldown = 0;
        this.postSwapCooldown = 0;
        this.offhandDelayTicks = 0;
    }

    private boolean checkOffhandDelay(int i) {
        if (i < (int)this.offhandDelay.getFloat()) {
            this.offhandDelayTicks = i + 1;
            return false;
        } else if (this.swapCooldown > 0) {
            this.offhandDelayTicks = i;
            return false;
        } else {
            return true;
        }
    }

    private boolean isEnemySwordClose() {
        Box box = mc.player.getBoundingBox().expand(0.5);

        for (PlayerEntity playerentity : mc.world.getPlayers()) {
            if (playerentity != mc.player && playerentity.getMainHandStack().isIn(ItemTags.SWORDS)) {
                Vec3d vec3d = playerentity.getEyePos();
                Optional optional = box.raycast(vec3d, vec3d.add(playerentity.getRotationVec(1.0F).multiply(6.0)));
                if (!optional.isEmpty()) {
                    RaycastContext raycastcontext = new RaycastContext(vec3d, (Vec3d)optional.get(), ShapeType.COLLIDER, FluidHandling.NONE, playerentity);
                    if (mc.world.raycast(raycastcontext).getType() == Type.MISS) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean shouldDoubleHand() {
        ClientPlayerEntity clientplayerentity = mc.player;
        if (clientplayerentity.getHealth() + clientplayerentity.getAbsorptionAmount() <= this.health.getFloat()) {
            return true;
        } else if (this.airCondition.isPresent()
            && !clientplayerentity.isOnGround()
            && !clientplayerentity.hasVehicle()
            && !clientplayerentity.getAbilities().flying
            && !clientplayerentity.isTouchingWater()
            && !clientplayerentity.isInLava()
            && !clientplayerentity.isClimbing()
            && !clientplayerentity.isGliding()
            && !clientplayerentity.hasStatusEffect(StatusEffects.LEVITATION)
            && !clientplayerentity.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return true;
        } else if (this.afterPopCondition.isPresent() && this.postSwapCooldown > 0) {
            return true;
        } else if (this.swordCondition.isPresent() && this.isEnemySwordClose()) {
            return true;
        } else {
            return this.obsidianCondition.isPresent() && this.isEnemyAimingAtBlock() ? true : this.crystalCondition.isPresent() && this.isAnyCrystalTargeted();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent packetreceiveevent) {
        if (packetreceiveevent.getPacket() instanceof EntityStatusS2CPacket entitystatuss2cpacket && mc.world != null) {
            if (entitystatuss2cpacket.getStatus() == 35 && entitystatuss2cpacket.getEntity(mc.world) == mc.player) {
                this.wasHoldingTotem = false;
                this.postSwapCooldown = 20;
            }
        }
    }
}
