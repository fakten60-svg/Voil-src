package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.PacketReceiveEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.SystemUtil;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import lol.lwes.voil.util.render.Render3DUtil;
import lol.lwes.voil.util.world.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;

public class BlockESP extends Module {
    public ModeSetting render;
    public BooleanSetting showNames;
    public BooleanSetting tracers;
    public ColorSetting color;
    public ItemListSetting blockList;
    private final Long2ObjectOpenHashMap<LongOpenHashSet> chunkBlocks;
    private final Set<Block> scanQueue;
    private boolean running;
    private final LongOpenHashSet scannedChunks;
    private volatile boolean needsRescan;
    private static final String MODE_FILLED_BOX = "Filled box";

    public BlockESP() {
        super("BlockESP", "Highlights selected blocks", Category.Render);
        this.render = new ModeSetting("Render", "Filled box", "Render mode", new String[]{"Filled box", "Outline"});
        this.showNames = new BooleanSetting("ShowNames", true, "Shows block icons above tracked blocks");
        this.tracers = new BooleanSetting("Tracers", false, "Draws tracers to tracked blocks");
        Color color1 = new Color(255, 0, 0, 255);
        String s2 = "Color of the blocks";
        Color colorx = color1;
        this.color = new ColorSetting("Color", colorx, s2);
        ItemListSetting itemlistsetting1 = new ItemListSetting("Blocks", "Blocks to highlight (search any block)");
        Item[] aitem = new Item[]{
            Items.CHEST,
            Items.TRAPPED_CHEST,
            Items.ENDER_CHEST,
            Items.BARREL,
            Items.SHULKER_BOX,
            Items.HOPPER,
            Items.FURNACE,
            Items.SPAWNER,
            Items.DIAMOND_ORE,
            Items.DEEPSLATE_DIAMOND_ORE,
            Items.EMERALD_ORE,
            Items.DEEPSLATE_EMERALD_ORE,
            Items.ANCIENT_DEBRIS,
            Items.NETHER_QUARTZ_ORE
        };
        this.blockList = itemlistsetting1.setRecommendedItems(aitem);
        this.chunkBlocks = new Long2ObjectOpenHashMap<>();
        this.scanQueue = new HashSet<>();
        this.running = false;
        this.scannedChunks = new LongOpenHashSet();
        this.needsRescan = true;
        Setting[] asetting = new Setting[]{this.render, this.showNames, this.tracers, this.color, this.blockList};
        this.addSettings(asetting);
        ItemListSetting itemlistsetting = this.blockList;
        Runnable runnable = this::scanLoadedChunks;
        itemlistsetting.setOnChange(runnable);
    }

    private void drawBlockName(Block block, BlockPos blockpos) {
        Vec3d vec3d1 = new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 1.5, blockpos.getZ() + 0.5);
        Vec3d vec3d = EntityUtil.worldToScreen(vec3d1);
        if (!(vec3d.z < 0.0) && !(vec3d.z > 1.0)) {
            float f = (int)vec3d.x - 10;
            float f1 = (int)vec3d.y - 10;
            Render2DUtil.drawBlurRect(f, f1, 20.0F, 20.0F, 4.0F, 14.0F);
            Item item = block.asItem();
            if (item != Items.AIR) {
                Render2DUtil.drawItem(new ItemStack(item), (float)(vec3d.x - 8.0), (float)(vec3d.y - 8.0), 1.0F, true);
            }
        }
    }

    private void scanLoadedChunks() {
        this.scanQueue.clear();

        for (Item item : this.blockList.getItems()) {
            Block block = Block.getBlockFromItem(item);
            if (block != null && block != Blocks.AIR) {
                this.scanQueue.add(block);
            }
        }

        if (this.running) {
            synchronized (this.chunkBlocks) {
                this.chunkBlocks.clear();
            }

            if (!this.running || mc.world == null || mc.player == null) {
                return;
            }

            if (this.scanQueue.isEmpty()) {
                return;
            }

            ChunkPos chunkpos = mc.player.getChunkPos();
            int i1 = Math.max(2, mc.options.getClampedViewDistance()) + 2;

            for (int j1 = -i1; j1 <= i1; j1++) {
                for (int i = -i1; i <= i1; i++) {
                    int l = chunkpos.x;
                    int j = l + j1;
                    l = chunkpos.z;
                    int k = l + i;
                    if (mc.world.isChunkLoaded(j, k)) {
                        WorldChunk worldchunk = mc.world.getChunk(j, k);
                        if (worldchunk != null) {
                            this.scanChunkAsync(worldchunk);
                        }
                    }
                }
            }
        }
    }

    private void onBlockUpdate(BlockPos blockpos, BlockState blockstate) {
        long i = ChunkPos.toLong(blockpos);
        long j = blockpos.asLong();
        boolean flag = this.scanQueue.contains(blockstate.getBlock());
        synchronized (this.chunkBlocks) {
            LongOpenHashSet longopenhashset = (LongOpenHashSet)this.chunkBlocks.get(i);
            if (flag) {
                if (longopenhashset == null) {
                    longopenhashset = new LongOpenHashSet();
                    this.chunkBlocks.put(i, longopenhashset);
                }

                longopenhashset.add(j);
            } else if (longopenhashset != null) {
                longopenhashset.remove(j);
                if (longopenhashset.isEmpty()) {
                    this.chunkBlocks.remove(i);
                }
            }

            this.needsRescan = true;
        }
    }

    @Override
    public void onRender2D() {
        if (this.isEnabled() && this.showNames.isEnabled() && mc.world != null && mc.player != null) {
            if (!this.chunkBlocks.isEmpty()) {
                synchronized (this.chunkBlocks) {
                    for (LongOpenHashSet longopenhashset : this.chunkBlocks.values()) {
                        LongIterator longiterator = longopenhashset.iterator();

                        while (longiterator.hasNext()) {
                            BlockPos blockpos = BlockPos.fromLong(longiterator.nextLong());
                            BlockState blockstate = mc.world.getBlockState(blockpos);
                            this.drawBlockName(blockstate.getBlock(), blockpos);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.running = false;
        this.chunkBlocks.clear();
        this.scannedChunks.clear();
        this.needsRescan = true;
    }

    @Override
    public void onEnable() {
        this.running = true;
        this.scanLoadedChunks();
    }

    private void scanChunkAsync(WorldChunk worldchunk) {
        if (!this.scanQueue.isEmpty()) {
            HashSet<Block> hashset = new HashSet<>(this.scanQueue);
            ChunkPos chunkpos = worldchunk.getPos();
            ChunkSection[] achunksection = worldchunk.getSectionArray();
            int i = worldchunk.getBottomY();
            int j = chunkpos.getStartX();
            int k = chunkpos.getStartZ();
            long l = chunkpos.toLong();
            CompletableFuture.supplyAsync(() -> {
                LongOpenHashSet longopenhashset = new LongOpenHashSet();

                for (int i2 = 0; i2 < achunksection.length; i2++) {
                    ChunkSection chunksection = achunksection[i2];
                    if (chunksection == null || chunksection.isEmpty() || !chunksection.hasAny(blockstate1 -> hashset.contains(blockstate1.getBlock()))) {
                        continue;
                    }

                    int j2 = i + (i2 << 4);

                    for (int k2 = 0; k2 < 16; k2++) {
                        for (int l2 = 0; l2 < 16; l2++) {
                            for (int i3 = 0; i3 < 16; i3++) {
                                BlockState blockstate = chunksection.getBlockState(i3, k2, l2);
                                if (hashset.contains(blockstate.getBlock())) {
                                    longopenhashset.add(BlockPos.asLong(j + i3, j2 + k2, k + l2));
                                }
                            }
                        }
                    }
                }

                return longopenhashset;
            }).thenAccept(longopenhashset -> {
                if (this.running) {
                    synchronized (this.chunkBlocks) {
                        if (longopenhashset.isEmpty()) {
                            this.chunkBlocks.remove(l);
                        } else {
                            this.chunkBlocks.put(l, longopenhashset);
                        }

                        this.needsRescan = true;
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPacketReceive(PacketReceiveEvent packetreceiveevent) {
        if (this.running && mc.world != null) {
            if (packetreceiveevent.getPacket() instanceof ChunkDataS2CPacket chunkdatas2cpacket) {
                int j = chunkdatas2cpacket.getChunkX();
                int i = chunkdatas2cpacket.getChunkZ();
                mc.execute(() -> {
                    if (this.running && mc.world != null) {
                        if (mc.world.isChunkLoaded(j, i)) {
                            WorldChunk worldchunk = mc.world.getChunk(j, i);
                            if (worldchunk != null) {
                                this.scanChunkAsync(worldchunk);
                            }
                        }
                    }
                });
            } else {
                Packet packet = packetreceiveevent.getPacket();
                if (packet instanceof UnloadChunkS2CPacket) {
                    ChunkPos chunkpos = ((UnloadChunkS2CPacket)packet).pos();
                    synchronized (this.chunkBlocks) {
                        this.chunkBlocks.remove(chunkpos.toLong());
                        this.needsRescan = true;
                    }
                } else if (packetreceiveevent.getPacket() instanceof BlockUpdateS2CPacket blockupdates2cpacket) {
                    BlockPos blockpos = blockupdates2cpacket.getPos();
                    this.onBlockUpdate(blockpos, blockupdates2cpacket.getState());
                } else {
                    packet = packetreceiveevent.getPacket();
                    if (packet instanceof ChunkDeltaUpdateS2CPacket) {
                        ((ChunkDeltaUpdateS2CPacket)packet).visitUpdates(this::onBlockUpdate);
                    }
                }
            }
        }
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.isEnabled() && mc.world != null && mc.player != null) {
            if (!this.chunkBlocks.isEmpty()) {
                Color color3 = this.color.getColor();
                Color color1 = ColorUtil.withAlpha(color3, 128);
                Color color2 = this.color.getColor();
                boolean flag = this.render.is(MODE_FILLED_BOX);
                boolean flag1 = this.tracers.isEnabled();
                Vec3d vec3d = flag1 ? mc.player.getLerpedPos(SystemUtil.getTickDelta()) : null;
                Matrix4f matrix4f = new Matrix4f();
                synchronized (this.chunkBlocks) {
                    if (this.needsRescan) {
                        this.scannedChunks.clear();

                        for (LongOpenHashSet longopenhashset1 : this.chunkBlocks.values()) {
                            this.scannedChunks.addAll(longopenhashset1);
                        }

                        this.needsRescan = false;
                    }

                    Color colorx = flag ? color1 : null;
                    LongOpenHashSet longopenhashset = this.scannedChunks;
                    Render3DUtil.drawBlockSet(matrix4f, longopenhashset, colorx, color2);
                    if (flag1) {
                        LongIterator longiterator = this.scannedChunks.iterator();

                        while (longiterator.hasNext()) {
                            BlockPos blockpos = BlockPos.fromLong(longiterator.nextLong());
                            Render3DUtil.drawLine(vec3d, Vec3d.ofCenter(blockpos), color2);
                        }
                    }
                }
            }
        }
    }
}
