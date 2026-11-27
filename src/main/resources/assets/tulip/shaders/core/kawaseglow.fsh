#version 330 core

uniform sampler2D GlowSampler;
uniform float GlowIntensity;
uniform int useVariableWidth;
uniform int numSegments;
uniform float segmentHeights[32];
uniform float segmentWidths[32];
uniform int alignRight;
uniform vec4 cornerRadii;
uniform vec2 uShapeSize;
uniform float uPad;
uniform vec2 uQuadOrigin;
uniform vec2 uQuadSize;
uniform float uFlipY;

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

void main() {
    vec2 local = gl_FragCoord.xy - uQuadOrigin;
    vec2 uv = local / max(uQuadSize, vec2(1.0));
    if (uFlipY > 0.5) {
        uv.y = 1.0 - uv.y;
    }

    float outside = clamp(shapeDistance(local - vec2(uPad)) + 0.5, 0.0, 1.0);
    vec4 glow = texture(GlowSampler, uv) * GlowIntensity * outside;
    if (glow.a < 0.002) {
        discard;
    }
    fragColor = glow;
}
