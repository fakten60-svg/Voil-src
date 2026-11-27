package dev.prestigeclient.renderlib.api.font;

import com.google.gson.JsonObject;

public class MSDFFontMetrics {
    private int emSize;
    private float lineHeight;
    private float ascender;
    private float descender;
    private float underlineY;
    private float underlineThickness;

    public int getEmSize() {
        return this.emSize;
    }

    public MSDFFontMetrics(int i, float f, float f1, float f2, float f3, float f4) {
        this.emSize = i;
        this.lineHeight = f;
        this.ascender = f1;
        this.descender = f2;
        this.underlineY = f3;
        this.underlineThickness = f4;
    }

    public MSDFFontMetrics() {
    }

    public static MSDFFontMetrics parse(JsonObject jsonobject) {
        MSDFFontMetrics msdffontmetrics = new MSDFFontMetrics();
        msdffontmetrics.emSize = jsonobject.get("emSize").getAsInt();
        msdffontmetrics.lineHeight = jsonobject.get("lineHeight").getAsFloat();
        msdffontmetrics.ascender = jsonobject.get("ascender").getAsFloat();
        msdffontmetrics.descender = jsonobject.get("descender").getAsFloat();
        msdffontmetrics.underlineY = jsonobject.get("underlineY").getAsFloat();
        msdffontmetrics.underlineThickness = jsonobject.get("underlineThickness").getAsFloat();
        return msdffontmetrics;
    }

    public float getUnderlineThickness() {
        return this.underlineThickness;
    }

    public float getDescender() {
        return this.descender;
    }

    public float getUnderlineY() {
        return this.underlineY;
    }

    public float getAscender() {
        return this.ascender;
    }

    public float getLineHeight() {
        return this.lineHeight;
    }
}
