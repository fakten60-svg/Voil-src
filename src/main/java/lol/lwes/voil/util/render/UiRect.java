package lol.lwes.voil.util.render;

import lol.lwes.voil.config.CloudConfig;
import lol.lwes.voil.gui.clickgui.ConfigEntryInfo;
import org.joml.Vector2d;

public class UiRect {
    public CloudConfig owner;
    public ConfigEntryInfo source;
    public int x;
    public int y;
    public int width;
    public int height;

    public UiRect() {
        this(0, 0, 0, 0);
    }

    public UiRect(int i, int j, int k, int l) {
        this(null, null, i, j, k, l);
    }

    public UiRect(CloudConfig cloudconfig, ConfigEntryInfo configentryinfo, int i, int j, int k, int l) {
        this.owner = cloudconfig;
        this.source = configentryinfo;
        this.x = i;
        this.y = j;
        this.width = k;
        this.height = l;
    }

    public boolean contains(int i, int j) {
        if (i >= this.x) {
            int l = this.width;
            int k = this.x;
            if (i <= k + l && j >= this.y) {
                l = this.height;
                k = this.y;
                if (j <= k + l) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean overlaps(UiRect uirect1) {
        for (boolean flag : this.getCornerHits(uirect1)) {
            if (flag) {
                return true;
            }
        }

        return false;
    }

    public boolean[] getCornerHits(UiRect uirect1) {
        boolean[] aboolean = new boolean[4];
        int k = uirect1.y;
        aboolean[0] = this.contains(uirect1.x, k);
        int j = uirect1.width;
        int i = uirect1.x;
        int k1 = i + j;
        int l = uirect1.y;
        aboolean[1] = this.contains(k1, l);
        j = uirect1.width;
        i = uirect1.x;
        k1 = i + j;
        j = uirect1.height;
        i = uirect1.y;
        int i1 = i + j;
        aboolean[2] = this.contains(k1, i1);
        j = uirect1.height;
        i = uirect1.y;
        int j1 = i + j;
        aboolean[3] = this.contains(uirect1.x, j1);
        return aboolean;
    }

    public Vector2d getCenter() {
        return new Vector2d(this.x + this.width / 2.0, this.y + this.height / 2.0);
    }
}
