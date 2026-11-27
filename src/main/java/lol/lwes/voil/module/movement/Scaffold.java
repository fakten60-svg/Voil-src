package lol.lwes.voil.module.movement;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketSendEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.movement.ScaffoldTarget;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.packet.PacketUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationPriority;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

public class Scaffold extends Module {
    RangeSetting cps = new RangeSetting("CPS", 1.0, 20.0, 7.0, 14.0, 1.0, "Clicks per second");
    BooleanSetting showBlockCount = new BooleanSetting("ShowBlockCount", false, "Shows block count under crosshair");
    BooleanSetting spoofSlot = new BooleanSetting("SpoofSlot", false, "Spoofs item in hand");
    BooleanSetting vulcanDisabler = new BooleanSetting("VulcanDisabler", false, "Disables vulcan scaffold");
    BooleanSetting keepY = new BooleanSetting("KeepY", false, "Prevents placing extra blocks while jumping");
    BooleanSetting tower = new BooleanSetting("Tower", false, "Towers up faster");
    ModeSetting rotation = new ModeSetting("Rotation", "Backwards", "Scaffold mode", new String[]{"Backwards", "Telly"});
    private int blockSlot;
    private int previousSlot;
    private int rotationTicks = 0;
    private int placeTicks = 0;
    private BlockPos lastPlacePos = null;
    private float startY = 0.0F;
    private static final String TELLY_MODE = "Telly";

    public Scaffold() {
        super("Scaffold", "Places blocks under you", Category.Movement);
        Setting[] asetting = new Setting[]{this.rotation, this.cps, this.showBlockCount, this.spoofSlot, this.vulcanDisabler, this.keepY, this.tower};
        this.addSettings(asetting);
    }

    private Direction findPlaceDirection(BlockPos blockpos, BlockPos blockpos1) {
        double d0 = Double.MAX_VALUE;
        Direction direction = null;
        Vec3d vec3d = blockpos1.toCenterPos();

        for (Direction direction1 : Direction.values()) {
            if (direction1 != Direction.DOWN) {
                BlockPos blockpos2 = blockpos.offset(direction1);
                if (blockpos2.getY() <= blockpos1.getY()) {
                    double d1 = blockpos2.toCenterPos().squaredDistanceTo(vec3d);
                    if (d1 < d0) {
                        d0 = d1;
                        direction = direction1;
                    }
                }
            }
        }

        return direction;
    }

    private boolean isAirBelow(World world, BlockPos blockpos) {
        return !world.getBlockState(blockpos.down()).isAir()
            || !world.getBlockState(blockpos.north()).isAir()
            || !world.getBlockState(blockpos.south()).isAir()
            || !world.getBlockState(blockpos.east()).isAir()
            || !world.getBlockState(blockpos.west()).isAir();
    }

    private boolean placeBlock(BlockPos blockpos) {
        FoundSlot foundslot = InventoryUtil.findMainInventoryStack(itemstack -> itemstack.getItem() instanceof BlockItem);
        if (foundslot.isFound()) {
            ClientWorld clientworld = mc.world;
            if (this.isAirBelow(clientworld, blockpos)) {
                return BlockUtil.placeBlockWithSlot(blockpos, foundslot, true, true);
            }
        }

        return false;
    }

    @Override
    public void onTick() {
        super.onTick();
        if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            ClientWorld clientworld = mc.world;
            Vec3d vec3d = mc.player.getEntityPos();
            if (this.keepY.isEnabled()) {
                if (this.startY == 0.0F) {
                    this.startY = (float)Math.floor(vec3d.y);
                }
            } else {
                this.startY = (float)Math.floor(vec3d.y);
            }

            mc.player.getYaw();
            mc.player.getPitch();
            if (this.rotation.getMode().equalsIgnoreCase(TELLY_MODE)) {
                mc.player.setSprinting(true);
                boolean flag = mc.player.forwardSpeed > 0.0F;
                if (flag && mc.player.isOnGround() && this.placeTicks == 0) {
                    this.placeTicks = 1;
                }

                if (mc.player.isOnGround()) {
                    int k = this.placeTicks;
                    if (k != 1) {
                        if (k == 2) {
                            mc.player.jump();
                            this.placeTicks = 0;
                        }
                    } else {
                        this.rotationTicks = 5;
                        this.placeTicks = 2;
                    }
                }

                if (!mc.player.isOnGround() && this.keepY.isEnabled() && !flag) {
                    this.previousSlot = mc.player.getBlockY();
                }

                ScaffoldTarget scaffoldtarget = this.findPlacement();
                int i = this.findBlockSlot();
                if (i == -1) {
                    this.lastPlacePos = null;
                    return;
                }

                if (scaffoldtarget != null) {
                    this.lastPlacePos = scaffoldtarget.pos.offset(scaffoldtarget.side);
                } else {
                    this.lastPlacePos = null;
                }

                Vec3d vec3d1 = null;
                Rotation rotation1;
                if (this.rotationTicks > 0) {
                    int j = this.rotationTicks--;
                    float f2 = mc.player.getYaw();
                    float f3 = mc.player.getPitch();
                    float f4 = f2 + MathHelper.nextFloat(mc.player.getRandom(), -35.0F, 35.0F);
                    float f5 = MathHelper.nextFloat(mc.player.getRandom(), 45.0F, 75.0F);
                    float f6 = MathHelper.wrapDegrees(f4 - f2);
                    float f7 = MathHelper.nextFloat(mc.player.getRandom(), 6.0F, 10.0F);
                    float f8 = MathHelper.nextFloat(mc.player.getRandom(), 3.0F, 6.0F);
                    float f9 = MathHelper.clamp(f6 * 0.35F, -f7, f7);
                    float f10 = f5 - f3;
                    float f11 = MathHelper.clamp(f10 * 0.25F, -f8, f8);
                    f9 += MathHelper.nextFloat(mc.player.getRandom(), -0.15F, 0.15F);
                    f11 += MathHelper.nextFloat(mc.player.getRandom(), -0.1F, 0.1F);
                    float f = f2 + f9;
                    float f1 = MathHelper.clamp(f3 + f11, -90.0F, 90.0F);
                    rotation1 = new Rotation(f, f1);
                } else {
                    rotation1 = new Rotation(mc.player.getYaw(), 85.0);
                    float f12;
                    if (scaffoldtarget != null) {
                        vec3d1 = scaffoldtarget.pos.toCenterPos().add(Vec3d.of(scaffoldtarget.side.getVector()).multiply(0.5));
                        Vec3d vec3d2 = mc.player.getEyePos();
                        double d0 = vec3d1.x - vec3d2.x;
                        double d1 = vec3d1.y - vec3d2.y;
                        double d2 = vec3d1.z - vec3d2.z;
                        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                        f12 = (float)(Math.toDegrees(Math.atan2(d2, d0)) - 90.0);
                        float f13 = (float)(-Math.toDegrees(Math.atan2(d1, d3)));
                        rotation1 = new Rotation(f12, f13);
                    } else {
                        f12 = rotation1.getYawFloat();
                        rotation1.getPitchFloat();
                    }

                    MathHelper.wrapDegrees(f12);
                }

                VoilClient.getRotationManager().requestFromCaller(rotation1, RotationPriority.HIGH);
                if (scaffoldtarget != null && vec3d1 != null) {
                    if (this.spoofSlot.isEnabled()) {
                        UpdateSelectedSlotC2SPacket updateselectedslotc2spacket = new UpdateSelectedSlotC2SPacket(i);
                        PacketUtil.sendImmediate(updateselectedslotc2spacket);
                    } else {
                        mc.player.getInventory().setSelectedSlot(i);
                    }

                    BlockHitResult blockhitresult = new BlockHitResult(vec3d1, scaffoldtarget.side, scaffoldtarget.pos, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    if (this.spoofSlot.isEnabled()) {
                        UpdateSelectedSlotC2SPacket updateselectedslotc2spacket1 = new UpdateSelectedSlotC2SPacket(mc.player.getInventory().getSelectedSlot());
                        PacketUtil.sendImmediate(updateselectedslotc2spacket1);
                    }

                    if (this.vulcanDisabler.isEnabled()) {
                        ClientCommandC2SPacket clientcommandc2spacket = new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING);
                        PacketUtil.sendImmediate(clientcommandc2spacket);
                    }
                }
            } else {
                float f14 = mc.player.getYaw() + 180.0F;
                float f15 = 83.8F;
                int k1 = (int)Math.floor(vec3d.x);
                int j1 = (int)this.startY;
                BlockPos blockpos = new BlockPos(k1, j1 - 1, (int)Math.floor(vec3d.z));
                if (clientworld.getBlockState(blockpos).isAir()) {
                    this.lastPlacePos = blockpos;
                    int l = this.findBlockSlot();
                    if (l != -1) {
                        int i1 = mc.player.getInventory().getSelectedSlot();
                        if (this.vulcanDisabler.isEnabled()) {
                            ClientCommandC2SPacket clientcommandc2spacket1 = new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING);
                            PacketUtil.sendImmediate(clientcommandc2spacket1);
                        }

                        InventoryUtil.selectSlot(l);
                        this.placeBlock(blockpos);
                        if (this.spoofSlot.isEnabled()) {
                            mc.player.getInventory().setSelectedSlot(i1);
                        }
                    }
                } else {
                    this.lastPlacePos = null;
                }

                Rotation rotationx = new Rotation(f14, f15);
                VoilClient.getRotationManager().requestFromCaller(rotationx, RotationPriority.HIGH);
            }
        }
    }

    @Override
    public void onRender2D() {
        if (this.showBlockCount.isEnabled() && mc.player != null) {
            int i = 0;
            ItemStack itemstack = ItemStack.EMPTY;

            for (int j = 0; j < 9; j++) {
                ItemStack itemstack1 = mc.player.getInventory().getStack(j);
                if (itemstack1.getItem() instanceof BlockItem) {
                    int l = itemstack1.getCount();
                    int k = i;
                    i = k + l;
                    if (itemstack.isEmpty()) {
                        itemstack = itemstack1;
                    }
                }
            }

            if (!itemstack.isEmpty() && i > 0) {
                String s = String.valueOf(i);
                int i1 = (int)VoilClient.fonts.getSelectedFont().getWidth(s);
                int j1 = mc.getWindow().getScaledWidth();
                float f = (j1 - i1) / 2.0F;
                float f1 = mc.getWindow().getScaledHeight() / 2.0F;
                Render2DUtil.drawItem(itemstack, f - 20.0F, f1, 1.0F, false);
                VoilClient.fonts.getSelectedFont().renderWithShadow(s, f, f1, 11.0F, -1);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getInventory().setSelectedSlot(this.blockSlot);
        }

        this.rotationTicks = 0;
        this.placeTicks = 0;
        VoilClient.getRotationManager().reset();
        this.lastPlacePos = null;
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            this.previousSlot = mc.player.getBlockY();
            this.blockSlot = mc.player.getInventory().getSelectedSlot();
            this.rotationTicks = 0;
            this.placeTicks = 0;
            this.startY = (float)Math.floor(mc.player.getY());
        }

        VoilClient.getRotationManager().reset();
        this.lastPlacePos = null;
    }

    private boolean isAirAt(BlockPos blockpos) {
        BlockState blockstate = mc.world.getBlockState(blockpos);
        return blockstate.isAir() || blockstate.getBlock().getDefaultState().isReplaceable();
    }

    private int findBlockSlot() {
        int i = -1;
        int j = 0;

        for (int k = 0; k < 9; k++) {
            ItemStack itemstack = mc.player.getInventory().getStack(k);
            if (itemstack.getItem() instanceof BlockItem && itemstack.getCount() > j) {
                i = k;
                j = itemstack.getCount();
            }
        }

        return i;
    }

    private ScaffoldTarget findPlacement() {
        if (mc.player != null && mc.world != null) {
            int i1 = this.keepY.isEnabled() ? this.previousSlot : mc.player.getBlockY();
            int i = i1 - 1;
            BlockPos blockpos = BlockPos.ofFloored(mc.player.getX(), i, mc.player.getZ());
            if (!this.isAirAt(blockpos)) {
                return null;
            }

            BlockPos blockpos1 = null;
            double d0 = Double.MAX_VALUE;
            Mutable mutable = new Mutable();
            Mutable mutable1 = new Mutable();
            Vec3d vec3d = mc.player.getEyePos();
            Vec3d vec3d1 = blockpos.toCenterPos();

            for (int j = 0; j <= 4; j++) {
                for (int k = -4; k <= 0; k++) {
                    for (int l = -4; l <= 4; l++) {
                        i1 = blockpos.getX();
                        int j1 = i1 + j;
                        i1 = blockpos.getY();
                        int l1 = i1 + k;
                        i1 = blockpos.getZ();
                        mutable.set(j1, l1, i1 + l);
                        if (!(vec3d.squaredDistanceTo(Vec3d.ofCenter(mutable)) > 20.25)) {
                            double d1 = mutable.toCenterPos().squaredDistanceTo(vec3d1);
                            if (!(d1 >= d0) && !this.isAirAt(mutable) && (!this.keepY.isEnabled() || mutable.getY() < this.previousSlot)) {
                                boolean flag = false;

                                for (Direction direction : Direction.values()) {
                                    if (direction != Direction.DOWN) {
                                        mutable1.set(mutable.offset(direction));
                                        if (this.isAirAt(mutable1)) {
                                            flag = true;
                                            break;
                                        }
                                    }
                                }

                                if (flag) {
                                    d0 = d1;
                                    blockpos1 = mutable.toImmutable();
                                }
                            }
                        }
                    }
                }
            }

            if (blockpos1 == null) {
                return null;
            }

            Direction direction1 = this.findPlaceDirection(blockpos1, blockpos);
            return direction1 == null ? null : new ScaffoldTarget(blockpos1, direction1);
        } else {
            return null;
        }
    }

    @EventHandler
    public void onPacketSend(PacketSendEvent packetsendevent) {
        if (!Module.isNotInGame()) {
            if (Block.getBlockFromItem(mc.player.getMainHandStack().getItem()) != Blocks.AIR) {
                if (packetsendevent.getPacket() instanceof ClientCommandC2SPacket clientcommandc2spacket
                    && clientcommandc2spacket.getEntityId() == mc.player.getId()
                    && clientcommandc2spacket.getMode() == Mode.START_SPRINTING
                    && this.vulcanDisabler.isEnabled()) {
                    packetsendevent.cancel();
                }
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (mc.player != null && mc.world != null) {
            if (this.lastPlacePos != null) {
                double d0 = this.lastPlacePos.getX();
                double d1 = this.lastPlacePos.getY();
                double d2 = this.lastPlacePos.getZ();
                int i = this.lastPlacePos.getX();
                double d3 = i + 1;
                i = this.lastPlacePos.getY();
                double d4 = i + 1;
                i = this.lastPlacePos.getZ();
                Box box = new Box(d0, d1, d2, d3, d4, i + 1);
                Matrix4f matrix4f = new Matrix4f();
                Color color = GuiTheme.accentColor1;
                Render3DUtil.drawBoxFilled(matrix4f, box, ColorUtil.withAlpha(color, 90));
            }
        }
    }
}
