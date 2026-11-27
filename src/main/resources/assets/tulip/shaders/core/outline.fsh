#version 150

struct BaseData {
    vec4 params;
    vec4 locSize;
    vec4 color1;
};

struct ExtraData {
    vec4 color2;
    vec4 color3;
    vec4 color4;
};

struct SegPair {
    vec4 a;
    vec4 b;
};

layout (std140) uniform Base  { BaseData  base[1024];  };
layout (std140) uniform Extra { ExtraData extra[1024]; };

layout (std140) uniform SegH0 { SegPair segH0[1024]; };
layout (std140) uniform SegH1 { SegPair segH1[1024]; };
layout (std140) uniform SegH2 { SegPair segH2[1024]; };
layout (std140) uniform SegH3 { SegPair segH3[1024]; };

layout (std140) uniform SegW0 { SegPair segW0[1024]; };
layout (std140) uniform SegW1 { SegPair segW1[1024]; };
layout (std140) uniform SegW2 { SegPair segW2[1024]; };
layout (std140) uniform SegW3 { SegPair segW3[1024]; };

in vec4 v_scissor;
flat in int glInstID;

out vec4 fragColor;

float getSegHeight(int i) {
    int pack = i / 4;
    int comp = i - pack * 4;
    vec4 v;
    if      (pack == 0) v = segH0[glInstID].a;
    else if (pack == 1) v = segH0[glInstID].b;
    else if (pack == 2) v = segH1[glInstID].a;
    else if (pack == 3) v = segH1[glInstID].b;
    else if (pack == 4) v = segH2[glInstID].a;
    else if (pack == 5) v = segH2[glInstID].b;
    else if (pack == 6) v = segH3[glInstID].a;
    else                v = segH3[glInstID].b;
    return v[comp];
}

float getSegWidth(int i) {
    int pack = i / 4;
    int comp = i - pack * 4;
    vec4 v;
    if      (pack == 0) v = segW0[glInstID].a;
    else if (pack == 1) v = segW0[glInstID].b;
    else if (pack == 2) v = segW1[glInstID].a;
    else if (pack == 3) v = segW1[glInstID].b;
    else if (pack == 4) v = segW2[glInstID].a;
    else if (pack == 5) v = segW2[glInstID].b;
    else if (pack == 6) v = segW3[glInstID].a;
    else                v = segW3[glInstID].b;
    return v[comp];
}

float sdSeg(vec2 p, vec2 a, vec2 b) {
    vec2 ab = b - a;
    vec2 ap = p - a;
    float l2 = dot(ab, ab);
    float t = (l2 > 0.0) ? clamp(dot(ap, ab) / l2, 0.0, 1.0) : 0.0;
    return length(ap - t * ab);
}

float sharpPolySDF(vec2 p, int nSeg) {
    float totalH = 0.0;
    for (int i = 0; i < 32; i++) {
        if (i >= nSeg) break;
        totalH += getSegHeight(i);
    }

    bool inside = false;
    {
        float cumH = 0.0;
        for (int i = 0; i < 32; i++) {
            if (i >= nSeg) break;
            float h = getSegHeight(i);
            float w = getSegWidth(i);
            if (p.x >= 0.0 && p.x <= w && p.y >= cumH && p.y <= cumH + h) {
                inside = true;
                break;
            }
            cumH += h;
        }
    }

    float u = 1e10;
    float sw0    = getSegWidth(0);
    float swLast = getSegWidth(nSeg - 1);

    u = min(u, sdSeg(p, vec2(0.0, 0.0),    vec2(0.0, totalH)));
    u = min(u, sdSeg(p, vec2(0.0, 0.0),    vec2(sw0, 0.0)));
    u = min(u, sdSeg(p, vec2(0.0, totalH), vec2(swLast, totalH)));

    float cumH = 0.0;
    for (int i = 0; i < 32; i++) {
        if (i >= nSeg) break;
        float h = getSegHeight(i);
        float w = getSegWidth(i);

        u = min(u, sdSeg(p, vec2(w, cumH), vec2(w, cumH + h)));

        if (i < nSeg - 1) {
            float wN = getSegWidth(i + 1);
            if (wN != w) {
                float yS = cumH + h;
                float x0 = min(w, wN);
                float x1 = max(w, wN);
                u = min(u, sdSeg(p, vec2(x0, yS), vec2(x1, yS)));
            }
        }
        cumH += h;
    }
    return inside ? -u : u;
}

float erodedPolySDF(vec2 p, int nSeg, float rad, float dSharp) {
    float totalH = 0.0;
    for (int i = 0; i < 32; i++) {
        if (i >= nSeg) break;
        totalH += getSegHeight(i);
    }

    float u = 1e10;
    float sw0    = getSegWidth(0);
    float swLast = getSegWidth(nSeg - 1);

    u = min(u, sdSeg(p, vec2(rad, rad),          vec2(rad, totalH - rad)));
    u = min(u, sdSeg(p, vec2(rad, rad),          vec2(sw0 - rad, rad)));
    u = min(u, sdSeg(p, vec2(rad, totalH - rad), vec2(swLast - rad, totalH - rad)));

    float cumH = 0.0;
    for (int i = 0; i < 32; i++) {
        if (i >= nSeg) break;
        float h = getSegHeight(i);
        float w = getSegWidth(i);

        float yBot;
        if (i == 0) {
            yBot = rad;
        } else {
            float wPrev = getSegWidth(i - 1);
            if      (w > wPrev) yBot = cumH + rad;
            else if (w < wPrev) yBot = cumH - rad;
            else                yBot = cumH;
        }

        float yTop;
        if (i == nSeg - 1) {
            yTop = cumH + h - rad;
        } else {
            float wNext = getSegWidth(i + 1);
            if      (w > wNext) yTop = cumH + h - rad;
            else if (w < wNext) yTop = cumH + h + rad;
            else                yTop = cumH + h;
        }

        u = min(u, sdSeg(p, vec2(w - rad, yBot), vec2(w - rad, yTop)));

        if (i < nSeg - 1) {
            float wNext = getSegWidth(i + 1);
            if (w > wNext) {
                float yS = cumH + h - rad;
                u = min(u, sdSeg(p, vec2(wNext - rad, yS), vec2(w - rad, yS)));
            } else if (wNext > w) {
                float yS = cumH + h + rad;
                u = min(u, sdSeg(p, vec2(w - rad, yS), vec2(wNext - rad, yS)));
            }
        }
        cumH += h;
    }

    return (dSharp <= -rad) ? -u : u;
}

float rdist(vec2 pos, vec2 size, float rad) {
    vec2 d = abs(pos) - size + rad;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - rad;
}

vec3 createGradient(vec2 coords, vec3 c1, vec3 c2, vec3 c3, vec3 c4) {
    return mix(mix(c1, c3, coords.x), mix(c2, c4, coords.x), coords.y);
}

void main() {
    if (v_scissor.w < gl_FragCoord.y ||
    v_scissor.y > gl_FragCoord.y ||
    v_scissor.x > gl_FragCoord.x ||
    v_scissor.z < gl_FragCoord.x) {
        discard;
    }

    vec4 p  = base[glInstID].params;
    vec4 ls = base[glInstID].locSize;
    float rad   = p.x;
    float smth  = p.y;
    float thick = p.z;
    int   nSeg  = int(abs(p.w));
    bool  alignRight = p.w < 0.0;
    vec2 loc  = ls.xy;
    vec2 size = ls.zw;

    vec2 center = loc + size * 0.5;
    vec2 position = gl_FragCoord.xy - center;

    float alpha;

    if (nSeg > 0) {
        vec2 coord = gl_FragCoord.xy - loc;
        if (alignRight) {
            coord.x = size.x - coord.x;
        }

        float dSharp   = sharpPolySDF(coord, nSeg);
        float dEroded  = erodedPolySDF(coord, nSeg, rad, dSharp);
        float dRounded = dEroded - rad;

        float combined = thick < 0.0 ? dRounded : max(dRounded, -thick - dRounded);
        alpha = 1.0 - smoothstep(-smth, smth, combined);
    } else if (thick < 0.0) {
        float dist = rdist(position, size * 0.5, rad);
        alpha = 1.0 - smoothstep(-smth, smth, dist);
    } else {
        vec2 halfSize = size * 0.5 - thick * 0.5;

        float outerDist = rdist(position, halfSize + thick, rad);
        float innerDist = rdist(position, halfSize - thick, max(rad - thick, 0.0));

        float combined = max(outerDist, -innerDist);
        alpha = 1.0 - smoothstep(-smth, smth, combined);
    }

    vec2 fragPos = (gl_FragCoord.xy - loc) / size;
    vec4 c1 = base[glInstID].color1;
    vec4 c2 = extra[glInstID].color2;
    vec4 c3 = extra[glInstID].color3;
    vec4 c4 = extra[glInstID].color4;
    vec4 color = vec4(createGradient(fragPos, c1.rgb, c2.rgb, c3.rgb, c4.rgb), c1.a * alpha);

    if (color.a < 0.01) discard;

    fragColor = color;
}
