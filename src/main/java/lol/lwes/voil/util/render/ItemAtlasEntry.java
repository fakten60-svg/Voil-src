package lol.lwes.voil.util.render;

import java.util.Objects;
import net.minecraft.client.render.item.KeyedItemRenderState;

public record ItemAtlasEntry(Object modelKey, KeyedItemRenderState renderState, String itemName) {

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ItemAtlasEntry itematlasentry1
                && Objects.equals(this.itemName, itematlasentry1.itemName)
                && Objects.equals(this.renderState, itematlasentry1.renderState)
                && Objects.equals(this.modelKey, itematlasentry1.modelKey);
    }

    @Override
    public final String toString() {
        return "z[" + "bC=" + this.modelKey + ", bD=" + this.renderState + ", bE=" + this.itemName + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.modelKey);
        i = i * 31 + Objects.hashCode(this.renderState);
        return i * 31 + Objects.hashCode(this.itemName);
    }
}
