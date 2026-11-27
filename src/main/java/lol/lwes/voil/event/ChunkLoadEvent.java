package lol.lwes.voil.event;

import net.minecraft.world.chunk.WorldChunk;

public class ChunkLoadEvent extends Event {
    private final WorldChunk chunk;

    public ChunkLoadEvent(WorldChunk worldchunk) {
        this.chunk = worldchunk;
    }

    public WorldChunk getChunk() {
        return this.chunk;
    }
}
