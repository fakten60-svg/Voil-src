#version 150

uniform vec2 InputResolution;
uniform vec2 uSize;
uniform vec2 uLocation;

uniform vec4 cornerRadii;
uniform vec4 GlowColor1;
uniform vec4 GlowColor2;
uniform vec4 GlowColor3;
uniform vec4 GlowColor4;
uniform float GlowWidth;
uniform float GlowIntensity;
uniform float smoothness;
uniform float Time;
uniform int useVariableWidth;
uniform int numSegments;
uniform float segmentHeights[32];
uniform float segmentWidths[32];

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float radius) {
    vec2 d = abs(pos) - size + radius;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - radius;
}

float rdistCorners(vec2 pos, vec2 halfSize, vec4 r) {
    float rad;
    if (pos.x < 0.0 && pos.y < 0.0)      rad = r.x;
    else if (pos.x >= 0.0 && pos.y < 0.0) rad = r.y;
    else if (pos.x < 0.0 && pos.y >= 0.0) rad = r.z;
    else                                    rad = r.w;
    vec2 d = abs(pos) - halfSize + rad;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - rad;
}

vec2 getSizeAtY(float y, float totalHeight) {
    if (numSegments <= 0) return vec2(100.0, totalHeight);

    float cumulativeHeight = 0.0;

    for (int i = 0; i < numSegments; i++) {
        if (i >= numSegments) break;

        float segHeight = segmentHeights[i];
        float nextHeight = cumulativeHeight + segHeight;

        if (y >= cumulativeHeight && y < nextHeight) {
            return vec2(segmentWidths[i], totalHeight);
        }

        cumulativeHeight = nextHeight;
    }

    return vec2(segmentWidths[numSegments - 1], totalHeight);
}

float getVariableWidthDistance(vec2 coord) {
    float minDist = 1e10;
    float cumulativeHeight = 0.0;
    float fR = max(cornerRadii.y, cornerRadii.w);

    for (int i = 0; i < numSegments; i++) {
        if (i >= numSegments) break;

        float segHeight = segmentHeights[i];
        float segWidth = segmentWidths[i];

        vec2 segmentCenter = vec2(segWidth * 0.5, cumulativeHeight + segHeight * 0.5);
        vec2 segmentHalfSize = vec2(segWidth * 0.5, segHeight * 0.5);
        float maxR = min(segmentHalfSize.x, segmentHalfSize.y);

        vec4 cr = vec4(0.0);

        if (i == 0) cr.x = min(cornerRadii.x, maxR);

        if (i == 0) {
            cr.y = min(cornerRadii.y, maxR);
        } else if (segWidth > segmentWidths[i - 1]) {
            float stepR = min(fR, segWidth - segmentWidths[i - 1]);
            cr.y = min(stepR, maxR);
        }

        if (i == numSegments - 1) cr.z = min(cornerRadii.z, maxR);

        if (i == numSegments - 1) {
            cr.w = min(cornerRadii.w, maxR);
        } else if (segWidth > segmentWidths[i + 1]) {
            float stepR = min(fR, segWidth - segmentWidths[i + 1]);
            cr.w = min(stepR, maxR);
        }

        float segDist = rdistCorners(coord - segmentCenter, segmentHalfSize, cr);
        minDist = min(minDist, segDist);

        cumulativeHeight += segHeight;
    }

    return minDist;
}

vec4 getAnimatedColor(vec2 coord, vec2 size) {
    float cycle = Time * 0.2;
    float positionOffset = (coord.x + coord.y) / (size.x + size.y);

    float phase = fract(cycle + positionOffset);

    vec4 color;
    if (phase < 0.25) {
        float t = phase / 0.25;
        color = mix(GlowColor1, GlowColor2, t);
    } else if (phase < 0.5) {
        float t = (phase - 0.25) / 0.25;
        color = mix(GlowColor2, GlowColor3, t);
    } else if (phase < 0.75) {
        float t = (phase - 0.5) / 0.25;
        color = mix(GlowColor3, GlowColor4, t);
    } else {
        float t = (phase - 0.75) / 0.25;
        color = mix(GlowColor4, GlowColor1, t);
    }

    return color;
}

float getGlowAlpha(vec2 coord, vec2 baseSize, vec4 radii, float smoothness) {
    float dist;

    if (useVariableWidth == 1) {
        dist = getVariableWidthDistance(coord);
    } else {
        vec2 center = baseSize * 0.5;

        dist = rdistCorners(coord - center, baseSize * 0.5, radii);
    }

    if (dist < 0.0) {
        return 0.0;
    } else {
        float normalizedDist = dist / GlowWidth;

        float innerGlow = exp(-normalizedDist * 4.0) * 0.4;
        float midGlow = exp(-normalizedDist * 2.0) * 0.3;
        float outerGlow = exp(-normalizedDist * 0.8) * 0.2;

        float combined = (innerGlow + midGlow + outerGlow);

        float fade = 1.0 - smoothstep(0.0, GlowWidth, dist);

        return pow(combined * fade, 1.2) * GlowIntensity * 0.5;
    }
}

void main() {
    vec2 fragPos = gl_FragCoord.xy - uLocation;

    float alpha = getGlowAlpha(fragPos, uSize, cornerRadii, smoothness);

    vec2 size = useVariableWidth == 1 ? getSizeAtY(fragPos.y, uSize.y) : uSize;
    vec4 color = getAnimatedColor(fragPos, size);

    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 saturated = mix(vec3(luminance), color.rgb, 1.15);

    fragColor = vec4(saturated, alpha * color.a);
}
