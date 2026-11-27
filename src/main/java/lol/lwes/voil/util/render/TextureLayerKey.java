package lol.lwes.voil.util.render;

import java.util.Objects;

public record TextureLayerKey(
    int textureId, int framebufferId, boolean scissorEnabled, int scissorX, int scissorY, int scissorWidth, int scissorHeight, Object program
) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof TextureLayerKey texturelayerkey1
                && Objects.equals(this.program, texturelayerkey1.program)
                && this.scissorHeight == texturelayerkey1.scissorHeight
                && this.scissorWidth == texturelayerkey1.scissorWidth
                && this.scissorY == texturelayerkey1.scissorY
                && this.scissorX == texturelayerkey1.scissorX
                && this.scissorEnabled == texturelayerkey1.scissorEnabled
                && this.framebufferId == texturelayerkey1.framebufferId
                && this.textureId == texturelayerkey1.textureId;
    }

    @Override
    public final String toString() {
        return "y["
            + "bu="
            + this.textureId
            + ", bv="
            + this.framebufferId
            + ", bw="
            + this.scissorEnabled
            + ", bx="
            + this.scissorX
            + ", by="
            + this.scissorY
            + ", bz="
            + this.scissorWidth
            + ", bA="
            + this.scissorHeight
            + ", bB="
            + this.program
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Integer.hashCode(this.textureId);
        i = i * 31 + Integer.hashCode(this.framebufferId);
        i = i * 31 + Boolean.hashCode(this.scissorEnabled);
        i = i * 31 + Integer.hashCode(this.scissorX);
        i = i * 31 + Integer.hashCode(this.scissorY);
        i = i * 31 + Integer.hashCode(this.scissorWidth);
        i = i * 31 + Integer.hashCode(this.scissorHeight);
        return i * 31 + Objects.hashCode(this.program);
    }
}
