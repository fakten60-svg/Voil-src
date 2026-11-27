package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.api.font.RLFontEffect;

public class IndexedFontEffect implements RLFontEffect {
    private final RLFontEffect delegate;
    private int index = 0;
    private float offset = 0.0F;

    public IndexedFontEffect(RLFontEffect rlfonteffect) {
        this.delegate = rlfonteffect;
    }

    public void resetIndex() {
        this.index = 0;
    }

    public void setOffset(float f) {
        this.offset = f;
    }

    public void advanceIndex() {
        this.index++;
    }

    @Override
    public int getColorFor(int i, double d0) {
        if (this.delegate instanceof IndexedColorSource indexedcolorsource) {
            int k = this.index;
            int j = i;
            return indexedcolorsource.getGlyphColor2(j, k);
        } else {
            return this.delegate.getColorFor(i, d0);
        }
    }
}
