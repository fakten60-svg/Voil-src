package lol.lwes.voil.util.render;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class GlowCacheMap extends LinkedHashMap {
    final KawaseGlowRenderer owner;

    GlowCacheMap(KawaseGlowRenderer kawaseglowrenderer, int i, float f, boolean flag) {
        super(i, f, flag);
        this.owner = kawaseglowrenderer;
    }

    @Override
    protected boolean removeEldestEntry(Entry entry) {
        if (this.size() > 8) {
            Object object = entry.getValue();
            KawaseGlowRenderer.deleteTargets((int[])object);
            return true;
        } else {
            return false;
        }
    }
}
