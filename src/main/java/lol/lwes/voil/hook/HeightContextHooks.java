package lol.lwes.voil.hook;

import net.minecraft.world.gen.chunk.ChunkGenerator;

public class HeightContextHooks {
    public static int getMinimumY(ChunkGenerator generator) {
        return generator.getMinimumY();
    }

    public static int getWorldHeight(ChunkGenerator generator) {
        return generator.getWorldHeight();
    }
}
