package lol.lwes.voil.gui.clickgui;

import java.util.List;
import lol.lwes.voil.community.CommunityPost;

public record PostCard(CommunityPost post, List<String> lines, float baseHeight) {
    private static final int COLLAPSED_LINES = 6;

    public PostCard {
        lines = List.copyOf(lines);
    }

    public int getOverflowLineCount() {
        return Math.max(0, this.lines.size() - COLLAPSED_LINES);
    }

    public float getHeight(boolean flag) {
        float f = !flag ? 0.0F : 10.0F;
        return this.baseHeight + this.getVisibleLineCount(flag) * 11.0F + f;
    }

    public int getVisibleLineCount(boolean flag) {
        return flag ? this.lines.size() : Math.min(this.lines.size(), COLLAPSED_LINES);
    }

    public boolean isCollapsible() {
        return this.lines.size() > COLLAPSED_LINES;
    }

    public List<String> getLines() {
        return this.lines;
    }

    public float getBaseHeight() {
        return this.baseHeight;
    }

    public CommunityPost getPost() {
        return this.post;
    }
}
