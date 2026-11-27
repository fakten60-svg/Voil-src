package lol.lwes.voil.module.crystalpvp;

import java.util.HashSet;
import java.util.Set;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EntitySpawnEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ExplosionEvent;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.event.PacketSendEvent;
import lol.lwes.voil.mixin.accessor.MouseAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
import lol.lwes.voil.util.RaycastUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.RaycastContext;

public class AutoCrystal extends Module {
    public RangeSetting placeTicks = new RangeSetting("Place Ticks", 0.0, 10.0, 0.0, 2.0, 0.1, "Delay before placing crystal");
    public RangeSetting breakTicks = new RangeSetting("Break Ticks", 0.0, 10.0, 0.0, 2.0, 0.1, "Delay before breaking crystal");
    private final BooleanSetting onKey = new BooleanSetting("On key", true, "Only activates on key");
    private final BooleanSetting clickSimulation = new BooleanSetting("Click Simulation", false, "Simulate clicks while placing/breaking");
    private final BooleanSetting damageTick = new BooleanSetting("Damage tick", false, "Predicts hurt time to double tap the target");
    private final BooleanSetting crystalOptimizer = new BooleanSetting("Crystal Optimizer", true, "Deletes crystals client side when you attack them");
    private final NumberSetting damageTickChance = new NumberSetting("Damage tick chance", 1.0, 100.0, 85.0, 1.0, "Chance to predict damage tick");
    public static final BooleanSetting extraSetting = new BooleanSetting("No Bounce", true, "Prevents bouncing crystals");
    private final BooleanSetting stopOnKill = new BooleanSetting("Stop On Kill", true, "Stops crystalling if someone dies");
    private final BooleanSetting antiWeakness = new BooleanSetting("Anti Weakness", false, "Prevents crystals not breaking when you have weakness");
    private final BooleanSetting sync = new BooleanSetting("Sync", false, "Sync crystal actions with server data");
    private final BooleanSetting entityIDPredict = new BooleanSetting("Entity ID predict", false, "Tries to predict entity ID before placing");
    private final BooleanSetting placeIfBroken = new BooleanSetting("Place if broken", true, "Places crystal if it breaks");
    private final KeybindSetting activate = new KeybindSetting("Activate", 1, false, "Key to activate AutoCrystal");
    private final BooleanSetting randomRotationOffset = new BooleanSetting("Random Rotation Offset", false, "Adds a small random offset to rotations");
    private final NumberSetting offsetAmount = new NumberSetting("Offset Amount", 0.1, 15.0, 0.5, 0.1, "Amount of random offset");
    private double ticksSinceAction;
    private final Set knownCrystalIds = new HashSet();
    private int lastCrystalId = -1;

    public AutoCrystal() {
        super("AutoCrystal", "Automatically places and breaks crystals", Category.CrystalPvP);
        Setting[] asetting = new Setting[]{
            this.placeTicks,
            this.breakTicks,
            this.onKey,
            this.clickSimulation,
            this.damageTick,
            this.crystalOptimizer,
            this.damageTickChance,
            this.stopOnKill,
            extraSetting,
            this.antiWeakness,
            this.sync,
            this.entityIDPredict,
            this.placeIfBroken,
            this.randomRotationOffset,
            this.offsetAmount,
            this.activate
        };
        this.addSettings(asetting);
        this.ticksSinceAction = 0.0;
        Setting.dependSetting(this.damageTickChance, this.damageTick);
        Setting.dependSetting(this.offsetAmount, this.randomRotationOffset);
    }

    public void placeCrystal() {
        double d0 = this.placeTicks.getRandomValue();
        if (this.hasElapsed(d0)) {
            Vec3d vec3d = mc.player.getCameraPosVec(0.0F);
            Vec3d vec3d1 = mc.player.getRotationVec(0.0F);
            Vec3d vec3d2 = vec3d.add(vec3d1.multiply(4.5));
            BlockHitResult blockhitresult = mc.world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
            if (blockhitresult.getType() == Type.BLOCK) {
                BlockPos blockpos = blockhitresult.getBlockPos();
                if (EntityUtil.isCrystalBase(blockpos)
                    && (
                        Blocks.OBSIDIAN == mc.world.getBlockState(blockhitresult.getBlockPos()).getBlock()
                            || Blocks.BEDROCK == mc.world.getBlockState(blockhitresult.getBlockPos()).getBlock()
                    )
                    && mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
                    if (this.clickSimulation.isEnabled()) {
                        VoilClient.getMouseSimulator().clickButton(1);
                    }

                    ActionResult actionresult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
                    if (actionresult.isAccepted()) {
                        if (actionresult instanceof Success success && success.swingSource() == SwingSource.CLIENT) {
                            mc.player.swingHand(Hand.MAIN_HAND);
                            if (this.entityIDPredict.isEnabled()) {
                                this.trackCrystal();
                            }
                        }

                        this.resetTimer();
                    }
                }
            }
        }
    }

    private boolean hasWeakness() {
        return mc.player.getStatusEffect(StatusEffects.WEAKNESS) != null;
    }

    private boolean damageTick() {
        boolean flag = mc.world
            .getPlayers()
            .parallelStream()
            .filter(abstractclientplayerentity -> abstractclientplayerentity != mc.player)
            .filter(abstractclientplayerentity -> abstractclientplayerentity.squaredDistanceTo(mc.player) < 64.0)
            .filter(abstractclientplayerentity -> abstractclientplayerentity.getLastAttacker() == null)
            .filter(abstractclientplayerentity -> !abstractclientplayerentity.isOnGround())
            .anyMatch(abstractclientplayerentity -> abstractclientplayerentity.hurtTime >= 2);
        return flag && !(mc.player.getAttacking() instanceof PlayerEntity);
    }

    static MinecraftClient getClient() {
        return mc;
    }

    @Override
    public void onTick() {
        if (!this.onKey.isEnabled() || this.activate.isPressed()) {
            if (this.damageTick.isEnabled() && this.damageTick()) {
                double d0 = (int)this.damageTickChance.getValue();
                if (AimMathUtil.rollChance(d0)) {
                    return;
                }
            }

            if (!this.stopOnKill.isEnabled() || !EntityUtil.isDeadBodyNearby()) {
                if (!mc.player.isUsingItem() && !mc.player.isBlocking()) {
                    if (!(mc.currentScreen instanceof HandledScreen)) {
                        if (isInGame()) {
                            this.ticksSinceAction++;
                        } else {
                            this.ticksSinceAction = 0.0;
                        }

                        if (mc.currentScreen == null) {
                            this.placeCrystal();
                            this.breakCrystal();
                        }

                        this.ticksSinceAction++;
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.lastCrystalId = -1;
        this.knownCrystalIds.clear();
    }

    public static boolean isInGame() {
        return mc.player != null && mc.world != null;
    }

    public boolean hasElapsed(double d0) {
        return this.ticksSinceAction >= d0;
    }

    private boolean isTool(ItemStack itemstack) {
        return itemstack.contains(DataComponentTypes.TOOL) && !(itemstack.getItem() instanceof HoeItem);
    }

    public void resetTimer() {
        this.ticksSinceAction = 0.0;
    }

    static MinecraftClient getClient2() {
        return mc;
    }

    private void switchToWeapon() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemstack = mc.player.getInventory().getStack(i);
            if (this.isWeapon(itemstack)) {
                mc.player.getInventory().setSelectedSlot(i);
                break;
            }
        }
    }

    static MinecraftClient getClient3() {
        return mc;
    }

    private void trackCrystal() {
        if (this.lastCrystalId != -1) {
            int j = this.lastCrystalId;
            int i = j + 1;
            this.knownCrystalIds.add(i);
            this.lastCrystalId = i;
        }
    }

    static MinecraftClient getClient4() {
        return mc;
    }

    private boolean isWeapon(ItemStack itemstack) {
        return itemstack.contains(DataComponentTypes.WEAPON);
    }

    public void breakCrystal() {
        double d0 = this.breakTicks.getRandomValue();
        if (this.hasElapsed(d0)) {
            int i = mc.player.getInventory().getSelectedSlot();
            if (this.antiWeakness.isEnabled() && this.hasWeakness() && !this.isWeapon(mc.player.getMainHandStack())) {
                this.switchToWeapon();
            }

            if (this.entityIDPredict.isEnabled()
                && this.lastCrystalId != -1
                && mc.world.getEntityById(this.lastCrystalId) instanceof EndCrystalEntity endcrystalentity1) {
                if (!RaycastUtil.isAimingAtEntity(endcrystalentity1, 3.0, 0.1F)) {
                    return;
                }

                mc.interactionManager.attackEntity(mc.player, endcrystalentity1);
                mc.player.swingHand(Hand.MAIN_HAND);
                if (this.clickSimulation.isEnabled()) {
                    VoilClient.getMouseSimulator().clickButton(0);
                }

                this.resetTimer();
                return;
            }

            if (mc.crosshairTarget instanceof EntityHitResult entityhitresult) {
                if (entityhitresult.getEntity() instanceof EndCrystalEntity endcrystalentity) {
                    if (this.sync.isEnabled() && !this.knownCrystalIds.contains(endcrystalentity.getId())) {
                        return;
                    }

                    mc.interactionManager.attackEntity(mc.player, endcrystalentity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    if (this.clickSimulation.isEnabled()) {
                        VoilClient.getMouseSimulator().clickButton(0);
                    }

                    this.resetTimer();
                } else if (entityhitresult.getEntity() instanceof MagmaCubeEntity magmacubeentity) {
                    mc.interactionManager.attackEntity(mc.player, magmacubeentity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    this.resetTimer();
                } else if (entityhitresult.getEntity() instanceof SlimeEntity slimeentity) {
                    mc.interactionManager.attackEntity(mc.player, slimeentity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    this.resetTimer();
                }
            }

            if (this.antiWeakness.isEnabled() && mc.player.getInventory().getSelectedSlot() != i) {
                mc.player.getInventory().setSelectedSlot(i);
            }

            if (this.randomRotationOffset.isEnabled()) {
                float f3 = 0.25F;
                float f4 = this.offsetAmount.getFloat();
                double d5 = mc.player.getYaw();
                double d6 = -f4;
                double d1 = f4;
                double d2 = d6;
                float f5 = (float)(d5 + AimMathUtil.randomDouble(d2, d1));
                d5 = mc.player.getPitch();
                d6 = -f4;
                double d3 = f4;
                double d4 = d6;
                float f6 = (float)AimMathUtil.clamp(d5 + AimMathUtil.randomDouble(d4, d3), -90.0, 90.0);
                float f7 = MathHelper.lerp(f3, mc.player.getYaw(), f5);
                float f = MathHelper.lerp(f3, mc.player.getPitch(), f6);
                MouseAccessor mouseaccessor = (MouseAccessor)mc.mouse;
                float f1 = MathHelper.wrapDegrees(f7 - mc.player.getYaw());
                float f2 = MathHelper.clamp(f - mc.player.getPitch(), -90.0F, 90.0F);
                mouseaccessor.setDeltaX(f1);
                mouseaccessor.setDeltaY(f2);
            }
        }
    }

    @EventHandler
    public void onPacket(PacketSendEvent packetsendevent) {
        if (packetsendevent.getPacket() instanceof PlayerInteractEntityC2SPacket playerinteractentityc2spacket && this.crystalOptimizer.isEnabled()) {
            playerinteractentityc2spacket.handle(new AutoCrystalAttackHandler(this));
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent packetreceiveevent) {
        Packet packet = packetreceiveevent.getPacket();
        if (packet instanceof EntitiesDestroyS2CPacket) {
            for (int i : ((EntitiesDestroyS2CPacket)packet).getEntityIds()) {
                this.knownCrystalIds.remove(i);
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent entityspawnevent) {
        if (entityspawnevent.getEntity() instanceof EndCrystalEntity) {
            this.lastCrystalId = entityspawnevent.getEntity().getId();
            this.knownCrystalIds.add(entityspawnevent.getEntity().getId());
        }
    }

    @EventHandler
    public void onExplosion(ExplosionEvent explosionevent) {
        if (this.placeIfBroken.isEnabled()) {
            this.placeCrystal();
        }
    }
}
