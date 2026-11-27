package lol.lwes.voil.util.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.inventory.FoundSlot;
import lol.lwes.voil.util.inventory.InventoryUtil;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SignBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public abstract class BlockUtil {
    private static final List storageBlocks = Arrays.asList(
        Blocks.FURNACE,
        Blocks.BLAST_FURNACE,
        Blocks.SMOKER,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.BARREL,
        Blocks.DISPENSER,
        Blocks.DROPPER,
        Blocks.HOPPER,
        Blocks.SHULKER_BOX,
        Blocks.WHITE_SHULKER_BOX,
        Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX,
        Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX,
        Blocks.PINK_SHULKER_BOX,
        Blocks.GRAY_SHULKER_BOX,
        Blocks.LIGHT_GRAY_SHULKER_BOX,
        Blocks.CYAN_SHULKER_BOX,
        Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX,
        Blocks.BROWN_SHULKER_BOX,
        Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX,
        Blocks.BLACK_SHULKER_BOX,
        Blocks.ENDER_CHEST,
        Blocks.SPAWNER,
        Blocks.BEACON,
        Blocks.JUKEBOX,
        Blocks.ENCHANTING_TABLE,
        Blocks.BREWING_STAND,
        Blocks.CAMPFIRE,
        Blocks.SOUL_CAMPFIRE,
        Blocks.BELL,
        Blocks.CONDUIT,
        Blocks.COMMAND_BLOCK,
        Blocks.REPEATING_COMMAND_BLOCK,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.DAYLIGHT_DETECTOR,
        Blocks.SCULK_SENSOR,
        Blocks.SCULK_CATALYST,
        Blocks.SCULK_SHRIEKER,
        Blocks.LECTERN,
        Blocks.PISTON,
        Blocks.MOVING_PISTON,
        Blocks.STRUCTURE_BLOCK,
        Blocks.JIGSAW,
        Blocks.BAMBOO_SIGN,
        Blocks.BAMBOO_HANGING_SIGN
    );
    private static final List oreBlocks = Arrays.asList(
        Blocks.COAL_ORE,
        Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE,
        Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COPPER_ORE,
        Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.GOLD_ORE,
        Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.REDSTONE_ORE,
        Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE,
        Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DIAMOND_ORE,
        Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE,
        Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.NETHER_QUARTZ_ORE,
        Blocks.NETHER_GOLD_ORE,
        Blocks.ANCIENT_DEBRIS
    );
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    private static List getEntityBlocksMatching(Predicate<Block> predicate) {
        ArrayList arraylist = new ArrayList();
        EntityUtil.getLoadedChunks().forEach(worldchunk -> worldchunk.getBlockEntities().values().forEach(blockentity -> {
            BlockPos blockpos = blockentity.getPos();
            BlockState blockstate = worldchunk.getBlockState(blockpos);
            if (predicate.test(blockstate.getBlock())) {
                arraylist.add(new Pair<>(blockpos, blockstate.getBlock()));
            }
        }));
        return arraylist;
    }

    private static boolean hasAirNeighborVertical(BlockPos blockpos) {
        return VoilClient.mc.world.getBlockState(blockpos.up()).isAir()
            || VoilClient.mc.world.getBlockState(blockpos.down()).isAir()
            || VoilClient.mc.world.getBlockState(blockpos.north()).isAir()
            || VoilClient.mc.world.getBlockState(blockpos.south()).isAir()
            || VoilClient.mc.world.getBlockState(blockpos.east()).isAir()
            || VoilClient.mc.world.getBlockState(blockpos.west()).isAir();
    }

    public static boolean isBlockAt(Block block, BlockPos blockpos) {
        return getState(blockpos).getBlock() == block;
    }
    public static boolean isAnchorCharged(BlockPos blockpos) {
        if (!isBlockAt(Blocks.RESPAWN_ANCHOR, blockpos)) {
            return false;
        }

        try {
            return getState(blockpos).get(RespawnAnchorBlock.CHARGES) != 0;
        } catch (IllegalArgumentException illegalargumentexception) {
            return false;
        }
    }

    public static void interactBlockFace(BlockPos blockpos, Direction direction) {
        interactAt(new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ()), direction);
    }

    public static CompletableFuture getStorageBlocksAsync() {
        return CompletableFuture.supplyAsync(() -> getEntityBlocksMatching(storageBlocks::contains), executor);
    }

    public static Direction getBestFace(BlockPos blockpos) {
        Vec3d vec3d = blockpos.toCenterPos().subtract(VoilClient.mc.player.getEyePos());
        double d0 = -Double.MAX_VALUE;
        Direction direction = null;

        for (Direction direction1 : Direction.values()) {
            BlockPos blockpos1 = blockpos.offset(direction1);
            BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos1);
            if (!blockstate.isAir()) {
                Block block = blockstate.getBlock();
                if (!isInteractable(block) && blockstate.getFluidState().isEmpty()) {
                    double d1 = direction1.getAxis().choose(vec3d.getX(), vec3d.getY(), vec3d.getZ()) * direction1.getDirection().offset();
                    if (d1 > d0) {
                        d0 = d1;
                        direction = direction1;
                    }
                }
            }
        }

        return direction;
    }

    public static int getAnchorCharges(BlockPos blockpos) {
        return VoilClient.mc.world != null && VoilClient.mc.world.getBlockState(blockpos).getBlock().equals(Blocks.RESPAWN_ANCHOR)
            ? VoilClient.mc.world.getBlockState(blockpos).get(RespawnAnchorBlock.CHARGES)
            : -1;
    }

    public static boolean isCobweb(BlockPos blockpos) {
        return VoilClient.mc.world.getBlockState(blockpos).isOf(Blocks.COBWEB);
    }

    public static ActionResult interactBlock(BlockHitResult blockhitresult) {
        return VoilClient.mc.interactionManager.interactBlock(VoilClient.mc.player, VoilClient.mc.player.getActiveHand(), blockhitresult);
    }

    public static BlockState getState(BlockPos blockpos) {
        return VoilClient.mc.world.getBlockState(blockpos);
    }

    public static boolean placeBlock(BlockPos blockpos, Hand hand, int i, boolean flag, boolean flag1) {
        if (i >= 0 && i <= 8) {
            Block block = Blocks.OBSIDIAN;
            if ((hand == Hand.MAIN_HAND
                    ? VoilClient.mc.player.getInventory().getStack(i)
                    : VoilClient.mc.player.getInventory().getStack(Hand.OFF_HAND.ordinal()))
                .getItem() instanceof BlockItem blockitem) {
                block = blockitem.getBlock();
            }

            if (!canPlaceBlockAt(blockpos, flag1, block)) {
                return false;
            }

            Vec3d vec3d = Vec3d.ofCenter(blockpos);
            Direction direction = getBestFace(blockpos);
            if (direction == null) {
                direction = Direction.UP;
            } else {
                blockpos = blockpos.offset(direction);
                vec3d = vec3d.add(direction.getOffsetX() * 0.5, direction.getOffsetY() * 0.5, direction.getOffsetZ() * 0.5);
            }

            interactBlockSneakless(new BlockHitResult(vec3d, direction.getOpposite(), blockpos, false), hand, flag);
            return true;
        } else {
            return false;
        }
    }

    public static ActionResult interactAt(Vec3d vec3d, Direction direction) {
        Vec3i vec3i = new Vec3i((int)vec3d.x, (int)vec3d.y, (int)vec3d.z);
        BlockPos blockpos = new BlockPos(vec3i);
        BlockHitResult blockhitresult = new BlockHitResult(vec3d, direction, blockpos, false);
        assert VoilClient.mc.interactionManager != null;
        assert VoilClient.mc.player != null;
        return VoilClient.mc.interactionManager.interactBlock(VoilClient.mc.player, VoilClient.mc.player.getActiveHand(), blockhitresult);
    }

    public static ActionResult interactAtUp(Vec3d vec3d) {
        return interactAt(vec3d, Direction.UP);
    }

    public static boolean isInCobweb(BlockPos blockpos) {
        BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos);
        BlockState blockstate1 = VoilClient.mc.world.getBlockState(blockpos.up());
        return blockstate.isOf(Blocks.COBWEB) && blockstate1.isOf(Blocks.COBWEB);
    }

    public static void interactBlockSneakless(BlockHitResult blockhitresult, Hand hand, boolean flag) {
        boolean flag2 = VoilClient.mc.options.sneakKey.isPressed();
        VoilClient.mc.player.setSneaking(false);
        ActionResult actionresult = VoilClient.mc.interactionManager.interactBlock(VoilClient.mc.player, hand, blockhitresult);
        if (actionresult.isAccepted()) {
            if (flag) {
                VoilClient.mc.player.swingHand(hand);
            } else {
                VoilClient.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
            }
        }

        VoilClient.mc.player.setSneaking(flag2);
    }

    public static boolean canPlaceBlockAt(BlockPos blockpos, boolean flag, Block block) {
        if (blockpos == null || !World.isValid(blockpos) || !VoilClient.mc.world.getBlockState(blockpos).isReplaceable()) {
            return false;
        }

        return !flag || VoilClient.mc.world.canPlace(block.getDefaultState(), blockpos, ShapeContext.absent());
    }

    public static boolean placeBlockWithSlot(BlockPos blockpos, FoundSlot foundslot, boolean flag, boolean flag1) {
        if (foundslot.isOffHand()) {
            return placeBlock(blockpos, Hand.OFF_HAND, VoilClient.mc.player.getInventory().getSelectedSlot(), flag, flag1);
        } else {
            return foundslot.isHotbar() ? placeBlock(blockpos, Hand.MAIN_HAND, foundslot.getSlot(), flag, flag1) : false;
        }
    }

    public static boolean canPlaceItemAt(Item item, BlockPos blockpos) {
        if (VoilClient.mc.player != null && VoilClient.mc.world != null) {
            InventoryUtil.hasItemInHotbar(item);
            BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos);
            if (!blockstate.isAir()
                && !blockstate.getFluidState().isEmpty()
                && !blockstate.getBlock().getDefaultState().canPlaceAt(VoilClient.mc.world, blockpos)) {
                return false;
            }

            BlockHitResult blockhitresult = new BlockHitResult(new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ()), Direction.UP, blockpos, false);
            return VoilClient.mc.interactionManager.interactBlock(VoilClient.mc.player, Hand.MAIN_HAND, blockhitresult) == ActionResult.SUCCESS;
        } else {
            return false;
        }
    }

    public static boolean isNotAir(BlockPos blockpos) {
        return !VoilClient.mc.world.getBlockState(blockpos).isAir();
    }

    public static BlockPos findAdjacentSolid(BlockPos blockpos) {
        for (Direction direction : Direction.values()) {
            BlockPos blockpos1 = blockpos.offset(direction);
            BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos1);
            if (blockstate.isSideSolidFullSquare(VoilClient.mc.world, blockpos1, direction.getOpposite())) {
                return blockpos1;
            }
        }

        return null;
    }

    public static boolean hasAnchorCharges(BlockPos blockpos) {
        try {
            return !isBlockAt(Blocks.RESPAWN_ANCHOR, blockpos) ? false : getState(blockpos).get(RespawnAnchorBlock.CHARGES) == 0;
        } catch (IllegalArgumentException illegalargumentexception) {
            return false;
        }
    }

    public static Box getOutlineBox(BlockPos blockpos) {
        VoxelShape voxelshape = VoilClient.mc.world.getBlockState(blockpos).getOutlineShape(VoilClient.mc.world, blockpos);
        return voxelshape != null && !voxelshape.isEmpty() ? voxelshape.getBoundingBox().offset(blockpos) : null;
    }

    public static CompletableFuture getOreBlocksAsync() {
        return CompletableFuture.supplyAsync(() -> getBlocksMatching(blockpos -> {
            BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos);
            return oreBlocks.contains(blockstate.getBlock()) && hasAirNeighborVertical(blockpos);
        }), executor);
    }

    public static boolean isInteractable(Block block) {
        return block instanceof CraftingTableBlock
            || block instanceof AnvilBlock
            || block instanceof LoomBlock
            || block instanceof CartographyTableBlock
            || block instanceof GrindstoneBlock
            || block instanceof StonecutterBlock
            || block instanceof ButtonBlock
            || block instanceof AbstractPressurePlateBlock
            || block instanceof BlockWithEntity
            || block instanceof BedBlock
            || block instanceof FenceGateBlock
            || block instanceof DoorBlock
            || block instanceof NoteBlock
            || block instanceof TrapdoorBlock
            || block instanceof ChestBlock
            || block instanceof EnderChestBlock
            || block instanceof BarrelBlock
            || block instanceof SignBlock;
    }

    public static boolean isExposed(BlockPos blockpos) {
        for (Direction direction : Direction.values()) {
            BlockPos blockpos1 = blockpos.offset(direction);
            BlockState blockstate = VoilClient.mc.world.getBlockState(blockpos1);
            if (blockstate.isSideSolidFullSquare(VoilClient.mc.world, blockpos1, direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    public static void interactBlockMainHand(BlockHitResult blockhitresult, boolean flag) {
        ActionResult actionresult = VoilClient.mc.interactionManager.interactBlock(VoilClient.mc.player, Hand.MAIN_HAND, blockhitresult);
        if (actionresult.isAccepted() && flag && actionresult instanceof Success success && success.swingSource() == SwingSource.CLIENT) {
            VoilClient.mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public static CompletableFuture getExposedOreAsync() {
        return CompletableFuture.supplyAsync(() -> getBlocksMatching(oreBlocks::contains), executor);
    }

    private static List getBlocksMatching(Predicate<BlockPos> predicate) {
        ArrayList arraylist = new ArrayList();
        EntityUtil.getLoadedChunks().forEach(worldchunk -> {
            ChunkPos chunkpos = worldchunk.getPos();
            int i = chunkpos.getStartX();
            int j = chunkpos.getStartZ();

            for (int k = 0; k < 16; k++) {
                for (int l = 0; l < 16; l++) {
                    for (int i1 = 0; i1 < VoilClient.mc.world.getHeight(); i1++) {
                        BlockPos blockpos = new BlockPos(i + k, i1, j + l);
                        BlockState blockstate = worldchunk.getBlockState(blockpos);
                        if (predicate.test(blockpos)) {
                            arraylist.add(new Pair<>(blockpos, blockstate.getBlock()));
                        }
                    }
                }
            }
        });
        return arraylist;
    }
}
