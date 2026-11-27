package lol.lwes.voil.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import lol.lwes.voil.util.render.ColorUtil;
import lol.lwes.voil.util.render.GradientType;

public class GradientSetting extends Setting {
    private final int maxStops = 8;
    private final boolean allowAlpha;
    private final List<GradientStop> stops = new ArrayList<>();
    private final List<GradientStop> stopsView = Collections.unmodifiableList(this.stops);
    private GradientType mode = GradientType.LINEAR;
    private int angle = 90;
    private static final long ALPHA_MASK = 255L;

    public GradientSetting(String s, String s1, Color[] acolor) {
        this(s, s1, false, acolor);
    }

    public GradientSetting(String s, String s1, boolean flag, Color[] acolor) {
        super(s, s1);
        if (acolor.length >= 2 && acolor.length <= 8) {
            this.allowAlpha = flag;

            for (int i = 0; i < acolor.length; i++) {
                List list = this.stops;
                float f = i * 100.0F;
                int j = acolor.length;
                int k = Math.round(f / (j - 1));
                Color color = acolor[i];
                list.add(new GradientStop(k, this.normalizeColor(color)));
            }
        } else {
            throw new IllegalArgumentException("gradient needs 2 to 8 colors");
        }
    }

    public void setAngle(int i) {
        this.angle = Math.floorMod(i, 360);
    }

    public GradientStop addStop(int i) {
        if (this.stops.size() >= 8) {
            return null;
        }

        int j = Math.clamp(i, 0, 100);
        float f = j / 100.0F;
        GradientStop gradientstop = new GradientStop(j, this.getColorAt(f));
        this.stops.add(gradientstop);
        this.sortStops();
        return gradientstop;
    }

    public boolean removeStop(GradientStop gradientstop) {
        return this.stops.size() <= 2 ? false : this.stops.remove(gradientstop);
    }

    public void setStopColor(GradientStop gradientstop, Color color) {
        if (color != null) {
            gradientstop.color = this.normalizeColor(color);
        }
    }

    public Color getColorAt(float f) {
        float f1 = ColorUtil.clamp01(f) * 100.0F;
        GradientStop gradientstop = (GradientStop)this.stops.getFirst();
        if (f1 <= gradientstop.position) {
            return gradientstop.color;
        }

        for (int i = 1; i < this.stops.size(); i++) {
            GradientStop gradientstop1 = (GradientStop)this.stops.get(i);
            if (f1 <= gradientstop1.position) {
                GradientStop gradientstop2 = (GradientStop)this.stops.get(i - 1);
                int j = gradientstop1.position - gradientstop2.position;
                float f2 = j == 0 ? 1.0F : (f1 - gradientstop2.position) / j;
                Color color = gradientstop2.color;
                return blend(color, gradientstop1.color, f2);
            }
        }

        return ((GradientStop)this.stops.getLast()).color;
    }

    public Color getColorAtPoint(float f, float f1, float f2, float f3) {
        float f4 = f - f2 / 2.0F;
        float f5 = f3 / 2.0F - f1;
        if (this.mode == GradientType.RADIAL) {
            float f12 = f4 / Math.max(f2 / 2.0F, 1.0E-4F);
            float f6 = f5 / Math.max(f3 / 2.0F, 1.0E-4F);
            float f10 = (float)(Math.hypot(f12, f6) / Math.sqrt(2.0));
            return this.getColorAt(f10);
        } else {
            double d0 = Math.toRadians(this.angle);
            float f7 = (float)Math.sin(d0);
            float f8 = (float)Math.cos(d0);
            float f9 = Math.abs(f2 * f7) + Math.abs(f3 * f8);
            float f11 = (f4 * f7 + f5 * f8) / Math.max(f9, 1.0E-4F) + 0.5F;
            return this.getColorAt(f11);
        }
    }

    public List<GradientStop> getStops() {
        return this.stopsView;
    }

    @Override
    public JsonElement toJson() {
        JsonArray jsonarray = new JsonArray();

        for (GradientStop gradientstop : this.stops) {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("position", gradientstop.position);
            boolean flag = this.allowAlpha;
            jsonobject.addProperty("color", toHex(gradientstop.color, flag));
            jsonarray.add(jsonobject);
        }

        JsonObject jsonobject1 = new JsonObject();
        jsonobject1.addProperty("type", this.mode.name());
        jsonobject1.addProperty("angle", this.angle);
        jsonobject1.add("stops", jsonarray);
        return jsonobject1;
    }

    public boolean isAllowAlpha() {
        return this.allowAlpha;
    }

    public void setMode(GradientType gradienttype) {
        if (gradienttype != null) {
            this.mode = gradienttype;
        }
    }

    public int getStopCount() {
        return 0;
    }

    public void fromJson(JsonElement jsonelement) {
        if (jsonelement.isJsonObject()) {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            if (jsonobject.has("stops")) {
                ArrayList arraylist = new ArrayList<>();
                Iterator iterator = jsonobject.getAsJsonArray("stops").iterator();

                while (iterator.hasNext()) {
                    JsonObject jsonobject1 = ((JsonElement)iterator.next()).getAsJsonObject();
                    Color color = parseHex(jsonobject1.get("color").getAsString(), 255);
                    if (color != null && arraylist.size() < 8) {
                        arraylist.add(new GradientStop(Math.clamp(jsonobject1.get("position").getAsInt(), 0, 100), this.normalizeColor(color)));
                    }
                }

                if (arraylist.size() >= 2) {
                    this.stops.clear();
                    this.stops.addAll(arraylist);
                    this.sortStops();
                }
            }

            if (jsonobject.has("type")) {
                this.mode = GradientType.valueOf(jsonobject.get("type").getAsString());
            }

            if (jsonobject.has("angle")) {
                int i = jsonobject.get("angle").getAsInt();
                this.setAngle(i);
            }
        }
    }

    public GradientType getMode() {
        return this.mode;
    }

    public void setStopPosition(GradientStop gradientstop, int i) {
        gradientstop.position = Math.clamp(i, 0, 100);
        this.sortStops();
    }

    public int getAngle() {
        return this.angle;
    }

    public static Color parseHex(String s, int i) {
        String s1 = s.startsWith("#") ? s.substring(1) : s;
        if (!s1.matches("[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?")) {
            return null;
        }

        long j = Long.parseLong(s1, 16);
        if (s1.length() == 6) {
            int i1 = Math.clamp(i, 0, 255) << 24;
            int l = (int)j;
            int k = i1;
            return new Color(k | l, true);
        } else {
            return new Color((int)((j & ALPHA_MASK) << 24 | j >>> 8), true);
        }
    }

    private void sortStops() {
        this.stops.sort(Comparator.comparingInt(GradientStop::getPosition));
    }

    private Color normalizeColor(Color color) {
        if (this.allowAlpha) {
            return color;
        }

        int i = color.getRGB();
        return new Color(i & 16777215);
    }

    private static int lerpChannel(float f, float f1, float f2, float f3) {
        return Math.clamp(Math.round((f + (f1 - f) * f2) / f3), 0, 255);
    }

    public Color[] getColors() {
        Color[] acolor = new Color[this.stops.size()];

        for (int i = 0; i < acolor.length; i++) {
            acolor[i] = ((GradientStop)this.stops.get(i)).color;
        }

        return acolor;
    }

    private static Color blend(Color color, Color color1, float f) {
        float f1 = color.getAlpha() / 255.0F;
        float f2 = color1.getAlpha() / 255.0F;
        float f3 = f1 + (f2 - f1) * f;
        if (f3 <= 0.0F) {
            return new Color(0, 0, 0, 0);
        }

        float f10 = color.getRed() * f1;
        float f4 = color1.getRed() * f2;
        float f5 = f10;
        int i = lerpChannel(f5, f4, f, f3);
        f10 = color.getGreen() * f1;
        float f6 = color1.getGreen() * f2;
        float f7 = f10;
        int j = lerpChannel(f7, f6, f, f3);
        f10 = color.getBlue() * f1;
        float f8 = color1.getBlue() * f2;
        float f9 = f10;
        int k = lerpChannel(f9, f8, f, f3);
        return new Color(i, j, k, Math.round(f3 * 255.0F));
    }

    public static String toHex(Color color, boolean flag) {
        if (flag) {
            return String.format("#%02X%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }

        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    public float[] getPositions() {
        float[] afloat = new float[this.stops.size()];

        for (int i = 0; i < afloat.length; i++) {
            afloat[i] = ((GradientStop)this.stops.get(i)).position / 100.0F;
        }

        return afloat;
    }

    @Override
    public void fromJson2(JsonElement jsonelement) {
        this.fromJson(jsonelement);
    }
}
