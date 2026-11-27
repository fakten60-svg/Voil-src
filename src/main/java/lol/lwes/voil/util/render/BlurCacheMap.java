package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class BlurCacheMap extends LinkedHashMap {
    final KawaseBlur owner;
    private static final int MAX_CACHE_SIZE = 8;

    BlurCacheMap(KawaseBlur kawaseblur, int i, float f, boolean flag) {
        super(i, f, flag);
        this.owner = kawaseblur;
    }

    @Override
    protected boolean removeEldestEntry(Entry entry) {
        if (this.size() > MAX_CACHE_SIZE) {
            Long olong = (Long)this.owner.lastUsedFrame.get(entry.getKey());
            if (olong != null && olong == RenderAPI.getFrameCounter()) {
                return false;
            }

            this.owner.lastUsedFrame.remove(entry.getKey());
            KawaseBlur.deleteLevels((int[])entry.getValue());
            return true;
        } else {
            return false;
        }
    }
}
