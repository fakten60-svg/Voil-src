package dev.prestigeclient.renderlib.api.uniform;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL20;

public record RLUniforms(int programId, Map<String, Integer> nameToLocation) {
    private static final Int2ObjectOpenHashMap cachedUniforms = new Int2ObjectOpenHashMap();

    @Deprecated
    public RLUniforms(int i) {
        this(i, new HashMap());
    }

    public static RLUniforms get(int i) {
        RLUniforms rluniforms = (RLUniforms)cachedUniforms.get(i);
        if (rluniforms == null) {
            rluniforms = new RLUniforms(i, new HashMap());
            cachedUniforms.put(i, rluniforms);
        }

        return rluniforms;
    }

    public int getLocation(String s) {
        Integer integer = (Integer)this.nameToLocation.get(s);
        if (integer == null) {
            integer = GL20.glGetUniformLocation(this.programId, s);
            this.nameToLocation.put(s, integer);
        }

        return integer;
    }

    public RLUniforms setColor(String s, Color color) {
        GL20.glUniform4f(this.getLocation(s), color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        return this;
    }

    public RLUniforms setMatrix3fv(String s, boolean flag, float[] afloat) {
        GL20.glUniformMatrix3fv(this.getLocation(s), flag, afloat);
        return this;
    }

    public RLUniforms setMatrix2fv(String s, boolean flag, float[] afloat) {
        GL20.glUniformMatrix2fv(this.getLocation(s), flag, afloat);
        return this;
    }

    public RLUniforms set1f(String s, float f) {
        GL20.glUniform1f(this.getLocation(s), f);
        return this;
    }

    public RLUniforms set3f(String s, float f, float f1, float f2) {
        GL20.glUniform3f(this.getLocation(s), f, f1, f2);
        return this;
    }

    public RLUniforms set2f(String s, float f, float f1) {
        GL20.glUniform2f(this.getLocation(s), f, f1);
        return this;
    }

    public RLUniforms set1i(String s, int i) {
        GL20.glUniform1i(this.getLocation(s), i);
        return this;
    }

    public static void forget(int i) {
        cachedUniforms.remove(i);
    }

    public RLUniforms set4i(String s, int i, int j, int k, int l) {
        GL20.glUniform4i(this.getLocation(s), i, j, k, l);
        return this;
    }

    public RLUniforms set2i(String s, int i, int j) {
        GL20.glUniform2i(this.getLocation(s), i, j);
        return this;
    }

    public RLUniforms set3i(String s, int i, int j, int k) {
        GL20.glUniform3i(this.getLocation(s), i, j, k);
        return this;
    }

    public RLUniforms setiv(String s, int[] aint) {
        GL20.glUniform1iv(this.getLocation(s), aint);
        return this;
    }

    public RLUniforms set4f(String s, float f, float f1, float f2, float f3) {
        GL20.glUniform4f(this.getLocation(s), f, f1, f2, f3);
        return this;
    }

    public RLUniforms setColor4f(String s, float f, float f1, float f2, float f3) {
        GL20.glUniform4f(this.getLocation(s), f, f1, f2, f3);
        return this;
    }

    public RLUniforms setfv(String s, float[] afloat) {
        GL20.glUniform1fv(this.getLocation(s), afloat);
        return this;
    }

    public RLUniforms setMatrix4fv(String s, boolean flag, float[] afloat) {
        GL20.glUniformMatrix4fv(this.getLocation(s), flag, afloat);
        return this;
    }
}
