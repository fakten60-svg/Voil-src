package lol.lwes.voil.module.crystalpvp;

import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ExplosionEvent;
import lol.lwes.voil.event.PacketSendEvent;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.mixin.accessor.MouseAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.crystalpvp.CrystalAttackHandler;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.AimMathUtil;
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
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AutoCrystal2 extends Module {
    public RangeSetting placeTicks = new RangeSetting("Place Ticks", 0.0, 10.0, 0.0, 2.0, 1.0, "Delay before placing crystal");
    public RangeSetting breakTicks = new RangeSetting("Break Ticks", 0.0, 10.0, 0.0, 2.0, 1.0, "Delay before breaking crystal");
    private final BooleanSetting onKey = new BooleanSetting("On key", true, "Only activates on key");
    private final BooleanSetting damageTick = new BooleanSetting("Damage tick", false, "Predicts hurt time to double tap the target");
    private final NumberSetting damageTickChance = new NumberSetting("Damage tick chance", 1.0, 100.0, 85.0, 1.0, "Chance to predict damage tick");
    private final BooleanSetting crystalOptimizer = new BooleanSetting("Crystal Optimizer", true, "Deletes crystals client side when you attack them");
    public static final BooleanSetting extraSetting = new BooleanSetting("No Bounce", true, "Prevents bouncing crystals");
    private final BooleanSetting stopOnKill = new BooleanSetting("Stop On Kill", true, "Stops crystalling if someone dies");
    private final BooleanSetting placeIfBroken = new BooleanSetting("Place if broken", true, "Places crystal if it breaks");
    public final KeybindSetting activate = new KeybindSetting("Activate", 1, false, "Key to activate AutoCrystal");
    private final BooleanSetting randomRotationOffset = new BooleanSetting("Random Rotation Offset", false, "Adds a small random offset to rotations");
    private final NumberSetting offsetAmount = new NumberSetting("Offset Amount", 0.1, 15.0, 0.5, 0.1, "Amount of random offset");
    private double breakTimer;
    private double placeTimer;

    public AutoCrystal2() {
        super("AutoCrystal", "Auto place and break of crystals, Modernized", Category.CrystalPvP);
        Setting[] asetting = new Setting[]{
            this.placeTicks,
            this.breakTicks,
            this.onKey,
            this.damageTick,
            this.crystalOptimizer,
            this.damageTickChance,
            this.stopOnKill,
            extraSetting,
            this.placeIfBroken,
            this.randomRotationOffset,
            this.offsetAmount,
            this.activate
        };
        this.addSettings(asetting);
        Setting.dependSetting(this.damageTickChance, this.damageTick);
        Setting.dependSetting(this.offsetAmount, this.randomRotationOffset);
    }

    public static boolean damageTick(boolean flag) {
        boolean flag1 = mc.world
            .getPlayers()
            .parallelStream()
            .filter(abstractclientplayerentity -> abstractclientplayerentity != mc.player)
            .filter(abstractclientplayerentity -> abstractclientplayerentity.squaredDistanceTo(mc.player) < 64.0)
            .filter(abstractclientplayerentity -> abstractclientplayerentity.getLastAttacker() == null)
            .filter(abstractclientplayerentity -> !flag || !abstractclientplayerentity.isOnGround())
            .anyMatch(abstractclientplayerentity -> abstractclientplayerentity.hurtTime >= 2);
        return flag1 && !(mc.player.getAttacking() instanceof PlayerEntity);
    }

    private boolean hasWeakness() {
        return mc.player.getStatusEffect(StatusEffects.WEAKNESS) != null;
    }

    public boolean isTool(ItemStack itemstack) {
        return itemstack.contains(DataComponentTypes.TOOL) && !(itemstack.getItem() instanceof HoeItem);
    }

    @Override
    public void onEnable() {
        this.breakTimer = 0.0;
        this.placeTimer = 0.0;
    }

    private boolean canAct() {
        if (Module.isNotInGame()) {
            return false;
        }

        if (this.onKey.isEnabled() && !this.activate.isPressed()) {
            return false;
        }

        if (this.damageTick.isEnabled() && damageTick(true)) {
            double d0 = (int)this.damageTickChance.getValue();
            if (AimMathUtil.rollChance(d0)) {
                return false;
            }
        }

        if (this.stopOnKill.isEnabled() && EntityUtil.isDeadBodyNearby()) {
            return false;
        } else {
            return mc.player.isUsingItem() || mc.player.isBlocking() ? false : !(mc.currentScreen instanceof HandledScreen);
        }
    }

    public static MinecraftClient getClient() {
        return mc;
    }

    public static MinecraftClient getClient2() {
        return mc;
    }

    public static MinecraftClient getClient3() {
        return mc;
    }

    private boolean isWeapon(ItemStack itemstack) {
        return itemstack.contains(DataComponentTypes.WEAPON);
    }

    public static int getMinDelay() {
        return 0;
    }

    public static MinecraftClient getClient4() {
        return mc;
    }

    private boolean isHoldingCrystal() {
        return mc.player.isHolding(Items.END_CRYSTAL);
    }

    @EventHandler
    public void onPacket(PacketSendEvent packetsendevent) {
        if (packetsendevent.getPacket() instanceof PlayerInteractEntityC2SPacket playerinteractentityc2spacket && this.crystalOptimizer.isEnabled()) {
            playerinteractentityc2spacket.handle(new CrystalAttackHandler(this));
        }
    }

    @EventHandler
    public void onExplosion(ExplosionEvent explosionevent) {
        this.placeIfBroken.isEnabled();
    }

    @EventHandler
    public void onRightClick(UseKeyEvent usekeyevent) {
        if (this.canAct()) {
            this.placeTimer++;
            if (this.placeTicks.getRandomValue() <= this.placeTimer
                && mc.crosshairTarget instanceof BlockHitResult blockhitresult
                && blockhitresult.getType() == Type.BLOCK) {
                BlockPos blockpos = blockhitresult.getBlockPos();
                if (EntityUtil.isCrystalBase(blockpos)
                    && (
                        Blocks.OBSIDIAN == mc.world.getBlockState(blockhitresult.getBlockPos()).getBlock()
                            || Blocks.BEDROCK == mc.world.getBlockState(blockhitresult.getBlockPos()).getBlock()
                    )
                    && mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
                    usekeyevent.addTimesPressed(1);
                    this.placeTimer = 0.0;
                }
            }
        }
    }

    @EventHandler
    public void onLeftClick(AttackKeyEvent attackkeyevent) {
        if (this.canAct()) {
            this.breakTimer++;
            if (this.breakTicks.getRandomValue() <= this.breakTimer && mc.crosshairTarget instanceof EntityHitResult entityhitresult) {
                Class[] aclass = new Class[]{EndCrystalEntity.class, MagmaCubeEntity.class, SlimeEntity.class};
                boolean flag = false;

                for (Class oclass : aclass) {
                    if (oclass.isInstance(entityhitresult.getEntity())) {
                        flag = true;
                        break;
                    }
                }

                if (flag) {
                    attackkeyevent.addTimesPressed(1);
                    this.breakTimer = 0.0;
                    if (this.randomRotationOffset.isEnabled()) {
                        float f4 = this.offsetAmount.getFloat();
                        double d4 = mc.player.getYaw();
                        double d5 = -f4;
                        double d0 = f4;
                        double d1 = d5;
                        float f5 = (float)(d4 + AimMathUtil.randomDouble(d1, d0));
                        d4 = mc.player.getPitch();
                        d5 = -f4;
                        double d2 = f4;
                        double d3 = d5;
                        float f6 = (float)AimMathUtil.clamp(d4 + AimMathUtil.randomDouble(d3, d2), -90.0, 90.0);
                        float f = MathHelper.lerp(0.25F, mc.player.getYaw(), f5);
                        float f1 = MathHelper.lerp(0.25F, mc.player.getPitch(), f6);
                        MouseAccessor mouseaccessor = (MouseAccessor)mc.mouse;
                        float f2 = MathHelper.wrapDegrees(f - mc.player.getYaw());
                        float f3 = MathHelper.clamp(f1 - mc.player.getPitch(), -90.0F, 90.0F);
                        mouseaccessor.setDeltaX(f2);
                        mouseaccessor.setDeltaY(f3);
                    }
                }
            }
        }
    }
}
