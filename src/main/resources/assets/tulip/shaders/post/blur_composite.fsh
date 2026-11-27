#version 330 core

uniform sampler2D BlurSampler;
uniform vec2 InputResolution;
uniform vec2 uSize;
uniform vec2 uLocation;

uniform vec4 cornerRadii;
uniform float Saturation;
uniform float BackgroundTint;
uniform vec3 TintColor;
uniform float Opacity;
uniform float smoothness;

uniform int useVariableWidth;
uniform int numSegments;
uniform int alignRight;
uniform float segmentHeights[32];
uniform float segmentWidths[32];

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float radius) {
    vec2 d = abs(pos) - size + radius;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - radius;
}

float cornerRadius(vec2 localPos, vec4 radii) {
    if (localPos.x < 0.0 && localPos.y < 0.0) {
        return radii.x;
    } else if (localPos.x >= 0.0 && localPos.y < 0.0) {
        return radii.y;
    } else if (localPos.x < 0.0 && localPos.y >= 0.0) {
        return radii.z;
    }
    return radii.w;
}

float widthAtY(float y) {
    float cumulative = 0.0;
    int count = clamp(numSegments, 1, 32);
    for (int i = 0; i < count; i++) {
        float next = cumulative + segmentHeights[i];
        if (y >= cumulative && y < next) {
            return segmentWidths[i];
        }
        cumulative = next;
    }
    return segmentWidths[count - 1];
}

float shapeAlpha(vec2 coord) {
    float sm = max(smoothness, 1e-4);
    if (useVariableWidth == 1 && numSegments > 0) {
        if (alignRight == 1) {
            coord.x = uSize.x - coord.x;
        }
        float totalHeight = 0.0;
        int count = clamp(numSegments, 1, 32);
        for (int i = 0; i < count; i++) {
            totalHeight += segmentHeights[i];
        }
        if (coord.y < 0.0 || coord.y > totalHeight) {
            return 0.0;
        }
        float currentWidth = widthAtY(coord.y);
        vec2 halfSize = vec2(currentWidth * 0.5, totalHeight * 0.5);
        vec2 localPos = coord - halfSize;
        float dist = rdist(localPos, halfSize, cornerRadius(localPos, cornerRadii));
        return 1.0 - smoothstep(0.0, sm, dist);
    }
    vec2 center = uSize * 0.5;
    vec2 localPos = coord - center;
    float dist = rdist(localPos, center, cornerRadius(localPos, cornerRadii));
    return 1.0 - smoothstep(0.0, sm, dist);
}

void main() {
    vec2 fragPos = gl_FragCoord.xy - uLocation;
    float alpha = shapeAlpha(fragPos);
    if (alpha <= 0.0) {
        discard;
    }

    vec2 res = InputResolution.x > 0.0 ? InputResolution : vec2(textureSize(BlurSampler, 0));
    vec3 blurred = texture(BlurSampler, gl_FragCoord.xy / res).rgb;

    float luminance = dot(blurred, vec3(0.299, 0.587, 0.114));
    vec3 saturated = mix(vec3(luminance), blurred, Saturation);
    vec3 finalColor = mix(saturated, TintColor, BackgroundTint);

    float opacity = Opacity > 0.0 ? Opacity : 1.0;
    fragColor = vec4(finalColor, alpha * opacity);
}
