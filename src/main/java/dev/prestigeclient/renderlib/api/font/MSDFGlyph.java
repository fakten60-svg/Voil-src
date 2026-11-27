package dev.prestigeclient.renderlib.api.font;

import com.google.gson.JsonObject;

public class MSDFGlyph {
    private int unicode;
    private float advance;
    private float planeLeft;
    private float planeBottom;
    private float planeRight;
    private float planeTop;
    private float atlasLeft;
    private float atlasBottom;
    private float atlasRight;
    private float atlasTop;

    public static MSDFGlyph parse(JsonObject jsonobject) {
        MSDFGlyph msdfglyph = new MSDFGlyph();
        msdfglyph.unicode = jsonobject.get("unicode").getAsInt();
        msdfglyph.advance = jsonobject.get("advance").getAsFloat();
        if (jsonobject.has("planeBounds")) {
            JsonObject jsonobject1 = jsonobject.get("planeBounds").getAsJsonObject();
            msdfglyph.planeLeft = jsonobject1.get("left").getAsFloat();
            msdfglyph.planeBottom = jsonobject1.get("bottom").getAsFloat();
            msdfglyph.planeRight = jsonobject1.get("right").getAsFloat();
            msdfglyph.planeTop = jsonobject1.get("top").getAsFloat();
        }

        if (jsonobject.has("atlasBounds")) {
            JsonObject jsonobject2 = jsonobject.get("atlasBounds").getAsJsonObject();
            msdfglyph.atlasLeft = jsonobject2.get("left").getAsFloat();
            msdfglyph.atlasBottom = jsonobject2.get("bottom").getAsFloat();
            msdfglyph.atlasRight = jsonobject2.get("right").getAsFloat();
            msdfglyph.atlasTop = jsonobject2.get("top").getAsFloat();
        }

        return msdfglyph;
    }

    public int getUnicode() {
        return this.unicode;
    }

    public float getAdvance() {
        return this.advance;
    }

    public float getPlaneRight() {
        return this.planeRight;
    }

    public float getPlaneLeft() {
        return this.planeLeft;
    }

    public float getPlaneTop() {
        return this.planeTop;
    }

    public float getPlaneBottom() {
        return this.planeBottom;
    }

    public float getAtlasLeft() {
        return this.atlasLeft;
    }

    public float getAtlasRight() {
        return this.atlasRight;
    }

    public float getAtlasTop() {
        return this.atlasTop;
    }

    public float getAtlasBottom() {
        return this.atlasBottom;
    }
}
