package lol.lwes.voil.config;

import java.awt.Color;
import java.util.Map;

public class ThemeInfo {
    public final String id;
    public final String owner;
    public final String name;
    public final String description;
    public final String type;
    public final String preview;
    public final String forkedFrom;
    public final boolean publicTheme;
    public final boolean favorited;
    public final boolean shared;
    public final int upvotes;
    public final int downvotes;

    private ThemeInfo(String s, String s1, String s2, String s3, String s4, boolean flag, int i, int j, String s5, String s6, boolean flag1, boolean flag2) {
        this.id = s;
        this.owner = s1;
        this.name = s2;
        this.description = s3;
        this.type = s4;
        this.publicTheme = flag;
        this.upvotes = i;
        this.downvotes = j;
        this.preview = s5;
        this.forkedFrom = s6;
        this.favorited = flag1;
        this.shared = flag2;
    }

    private static String getString(Map map, String s) {
        Object object = map.get(s);
        return object instanceof String ? (String)object : "";
    }

    public static ThemeInfo fromSharedMap(Map map) {
        return new ThemeInfo(
            getString(map, "id"),
            "",
            getString(map, "name"),
            getString(map, "description"),
            getString(map, "type"),
            true,
            0,
            0,
            "",
            "",
            getBoolean(map, "favorited"),
            true
        );
    }

    public int getScore() {
        int j = this.downvotes;
        int i = this.upvotes;
        return i - j;
    }

    private static int getInt(Map map, String s) {
        Object object = map.get(s);
        return object instanceof Number ? ((Number)object).intValue() : 0;
    }

    public Color[] getPreviewColors() {
        if (this.preview != null && !this.preview.isBlank()) {
            String[] astring = this.preview.split(",");
            Color[] acolor = new Color[astring.length];

            for (int i = 0; i < astring.length; i++) {
                try {
                    acolor[i] = Color.decode(astring[i].trim());
                } catch (Exception exception) {
                    acolor[i] = Color.WHITE;
                }
            }

            return acolor;
        } else {
            return new Color[0];
        }
    }

    public static ThemeInfo fromCustomMap(Map map) {
        return new ThemeInfo(
            getString(map, "id"),
            getString(map, "owner"),
            getString(map, "name"),
            "",
            getString(map, "type"),
            getBoolean(map, "public"),
            getInt(map, "upvotes"),
            getInt(map, "downvotes"),
            getString(map, "preview"),
            getString(map, "forkedFrom"),
            getBoolean(map, "favorited"),
            false
        );
    }

    private static boolean getBoolean(Map map, String s) {
        return Boolean.TRUE.equals(map.get(s));
    }
}
