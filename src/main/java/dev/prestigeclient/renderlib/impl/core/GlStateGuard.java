package dev.prestigeclient.renderlib.impl.core;

import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;

public class GlStateGuard implements AutoCloseable {
    public static final int DEFAULT_UNITS = 4;
    private static final int MAX_UNITS = 16;
    private final boolean prevDepth = GL30C.glIsEnabled(2929);
    private final boolean prevCull = GL30C.glIsEnabled(2884);
    private final boolean prevBlend = GL30C.glIsEnabled(3042);
    private final boolean prevScissor = GL30C.glIsEnabled(3089);
    private final boolean prevDepthMask = GL30C.glGetBoolean(2930);
    private final int prevProg = GL30C.glGetInteger(35725);
    private final int prevVAO = GL30C.glGetInteger(34229);
    private final int prevArrayBuf = GL30C.glGetInteger(34964);
    private final int prevElemBuf = GL30C.glGetInteger(34965);
    private final int prevActiveTex = GL30C.glGetInteger(34016);
    private final int[] prevTex;
    private final int[] prevSampler;
    private final int prevTexActive;
    private final int prevSamplerActive;
    private final int prevBlendEqRGB;
    private final int prevBlendEqA;
    private final int prevBlendSrcRGB;
    private final int prevBlendDstRGB;
    private final int prevBlendSrcA;
    private final int prevBlendDstA;
    private final int[] scissorRect;
    private final int[] viewport;
    private final int readFb;
    private final int drawFb;

    private GlStateGuard(int i) {
        this.prevTex = new int[i];
        this.prevSampler = new int[i];

        for (int j = 0; j < i; j++) {
            GL30C.glActiveTexture(33984 + j);
            this.prevTex[j] = GL30C.glGetInteger(32873);
            this.prevSampler[j] = GL30C.glGetInteger(35097);
        }

        GL30C.glActiveTexture(this.prevActiveTex);
        int k = this.prevActiveTex;
        int l = k - 33984;
        if (l >= 0 && l < i) {
            this.prevTexActive = this.prevTex[l];
            this.prevSamplerActive = this.prevSampler[l];
        } else {
            this.prevTexActive = GL30C.glGetInteger(32873);
            this.prevSamplerActive = GL30C.glGetInteger(35097);
        }

        this.prevBlendEqRGB = GL30C.glGetInteger(32777);
        this.prevBlendEqA = GL30C.glGetInteger(34877);
        this.prevBlendSrcRGB = GL30C.glGetInteger(32969);
        this.prevBlendDstRGB = GL30C.glGetInteger(32968);
        this.prevBlendSrcA = GL30C.glGetInteger(32971);
        this.prevBlendDstA = GL30C.glGetInteger(32970);
        this.scissorRect = new int[4];
        GL30C.glGetIntegerv(3088, this.scissorRect);
        this.viewport = new int[4];
        GL30C.glGetIntegerv(2978, this.viewport);
        this.drawFb = GL30C.glGetInteger(36006);
        this.readFb = GL30C.glGetInteger(36010);
    }

    @Override
    public void close() {
        this.restore();
    }

    public static GlStateGuard capture() {
        return capture(4);
    }

    public static GlStateGuard capture(int i) {
        return new GlStateGuard(Math.max(1, Math.min(i, 16)));
    }

    public void restore() {
        if (this.prevDepth) {
            GL30C.glEnable(2929);
        } else {
            GL30C.glDisable(2929);
        }

        if (this.prevCull) {
            GL30C.glEnable(2884);
        } else {
            GL30C.glDisable(2884);
        }

        if (this.prevBlend) {
            GL30C.glEnable(3042);
        } else {
            GL30C.glDisable(3042);
        }

        if (this.prevScissor) {
            GL30C.glEnable(3089);
        } else {
            GL30C.glDisable(3089);
        }

        GL30C.glDepthMask(this.prevDepthMask);
        GL30C.glUseProgram(this.prevProg);
        GL30C.glBindBuffer(34962, this.prevArrayBuf);
        GL30C.glBindBuffer(34963, this.prevElemBuf);
        GL30C.glBindVertexArray(this.prevVAO);

        for (int i = 0; i < this.prevTex.length; i++) {
            GL30C.glActiveTexture(33984 + i);
            GL30C.glBindTexture(3553, this.prevTex[i]);
            GL33C.glBindSampler(i, this.prevSampler[i]);
        }

        int j = this.prevActiveTex;
        int k = j - 33984;
        if (k >= 0 && k < this.prevTex.length) {
            GL30C.glActiveTexture(this.prevActiveTex);
        } else {
            GL30C.glActiveTexture(this.prevActiveTex);
            GL30C.glBindTexture(3553, this.prevTexActive);
            if (k >= 0) {
                GL33C.glBindSampler(k, this.prevSamplerActive);
            }
        }

        GL30C.glBlendEquationSeparate(this.prevBlendEqRGB, this.prevBlendEqA);
        GL30C.glBlendFuncSeparate(this.prevBlendSrcRGB, this.prevBlendDstRGB, this.prevBlendSrcA, this.prevBlendDstA);
        GL30C.glScissor(this.scissorRect[0], this.scissorRect[1], this.scissorRect[2], this.scissorRect[3]);
        GL30C.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
        GL30C.glBindFramebuffer(36008, this.readFb);
        GL30C.glBindFramebuffer(36009, this.drawFb);
    }
}
