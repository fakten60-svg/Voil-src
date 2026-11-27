#version 330 core

uniform vec2 uFboSize;
uniform float uPad;
uniform vec2 uShapeSize;
uniform vec4 cornerRadii;
uniform int useVariableWidth;
uniform int numSegments;
uniform float segmentHeights[32];
uniform float segmentWidths[32];
uniform int alignRight;
uniform vec4 GlowColor1;
uniform vec4 GlowColor2;
uniform vec4 GlowColor3;
uniform vec4 GlowColor4;
uniform float Time;

out vec4 fragColor;

float rdistCorners(vec2 pos, vec2 halfSize, vec4 r) {
    float rad;
    if (pos.x < 0.0 && pos.y < 0.0) {
        rad = r.x;
    } else if (pos.x >= 0.0 && pos.y < 0.0) {
        rad = r.y;
    } else if (pos.x < 0.0 && pos.y >= 0.0) {
        rad = r.z;
    } else {
        rad = r.w;
    }
    vec2 d = abs(pos) - halfSize + rad;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - rad;
}

float variableWidthDistance(vec2 coord) {
    float minDist = 1e10;
    float cumulativeHeight = 0.0;
    float fR = max(cornerRadii.y, cornerRadii.w);
    int count = clamp(numSegments, 1, 32);

    for (int i = 0; i < count; i++) {
        float segHeight = segmentHeights[i];
        float segWidth = segmentWidths[i];

        vec2 segmentCenter = vec2(segWidth * 0.5, cumulativeHeight + segHeight * 0.5);
        vec2 segmentHalfSize = vec2(segWidth * 0.5, segHeight * 0.5);
        float maxR = min(segmentHalfSize.x, segmentHalfSize.y);

        vec4 cr = vec4(0.0);
        if (i == 0) {
            cr.x = min(cornerRadii.x, maxR);
            cr.y = min(cornerRadii.y, maxR);
        } else if (segWidth > segmentWidths[i - 1]) {
            cr.y = min(min(fR, segWidth - segmentWidths[i - 1]), maxR);
        }
        if (i == count - 1) {
            cr.z = min(cornerRadii.z, maxR);
            cr.w = min(cornerRadii.w, maxR);
        } else if (segWidth > segmentWidths[i + 1]) {
            cr.w = min(min(fR, segWidth - segmentWidths[i + 1]), maxR);
        }

        minDist = min(minDist, rdistCorners(coord - segmentCenter, segmentHalfSize, cr));
        cumulativeHeight += segHeight;
    }
    return minDist;
}

float shapeDistance(vec2 coord) {
    if (useVariableWidth == 1 && numSegments > 0) {
        if (alignRight == 1) {
            coord.x = uShapeSize.x - coord.x;
        }
        return variableWidthDistance(coord);
    }
    return rdistCorners(coord - uShapeSize * 0.5, uShapeSize * 0.5, cornerRadii);
}

vec4 animatedColor(vec2 coord, vec2 size) {
    float cycle = Time * 0.2;
    float phase = fract(cycle + (coord.x + coord.y) / max(size.x + size.y, 1.0));
    if (phase < 0.25) {
        return mix(GlowColor1, GlowColor2, phase / 0.25);
    } else if (phase < 0.5) {
        return mix(GlowColor2, GlowColor3, (phase - 0.25) / 0.25);
    } else if (phase < 0.75) {
        return mix(GlowColor3, GlowColor4, (phase - 0.5) / 0.25);
    }
    return mix(GlowColor4, GlowColor1, (phase - 0.75) / 0.25);
}

void main() {
    vec2 coord = gl_FragCoord.xy - vec2(uPad);
    float coverage = clamp(0.5 - shapeDistance(coord), 0.0, 1.0);
    vec4 color = animatedColor(coord, uShapeSize);
    float a = color.a * coverage;
    fragColor = vec4(color.rgb * a, a);
}
