#version 330 core

uniform sampler2D texSampler;
uniform sampler2D jfaTex;
uniform sampler2D glowTex;

uniform float fillAlpha;
uniform float glowInstensity;
uniform float coreGlow;
uniform float haloIntensity;
uniform float time;
uniform int outline;
uniform float thickness;
uniform float glowRadius;
uniform float speed;
uniform int colorSource;
uniform float colorFrequency;
uniform vec4 damagedColor;
uniform vec4 fillColor;
uniform vec4 outlineColor;
uniform int shine;
uniform float shineIntensity;
uniform float shinePhase;
uniform float shineWidth;
uniform float shineAngle;

in vec2 texCoord;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(c.xxx + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

vec3 resolveColor(vec4 base, vec3 maskColor, vec2 uv) {
    if (colorSource == 1 && dot(maskColor, maskColor) > 0.0) {
        return maskColor;
    }
    if (colorSource == 2) {
        return damagedColor.rgb;
    }
    if (base.a > 0.0) {
        return base.rgb;
    }
    float hue = fract(time * speed * 0.2 + (uv.x + uv.y) * max(colorFrequency, 0.05));
    return hsv2rgb(vec3(hue, 0.75, 1.0));
}

void main() {
    vec4 mask = texture(texSampler, texCoord);
    float inside = step(0.01, mask.a);

    vec2 seed = texture(jfaTex, texCoord).xy;
    float dist = seed.x < 0.0 ? 1e6 : length(seed - gl_FragCoord.xy);

    vec3 outlineRgb = resolveColor(outlineColor, mask.rgb, texCoord);
    vec3 fillRgb = resolveColor(fillColor.a > 0.0 ? fillColor : outlineColor, mask.rgb, texCoord);

    vec4 result = vec4(0.0);

    vec4 glow = texture(glowTex, texCoord);
    float glowScale = glowInstensity * coreGlow + haloIntensity;
    result += glow * glowScale * (1.0 - inside);

    if (inside > 0.5 && fillAlpha > 0.0) {
        float a = fillAlpha;
        result = vec4(fillRgb * a, a) + result * (1.0 - a);
    }

    if (outline != 0) {
        float t = max(thickness, 1.0);
        float ring = (1.0 - smoothstep(t - 1.0, t, dist)) * (1.0 - inside);
        float edgeInside = inside * (1.0 - smoothstep(0.0, 1.0, texture(texSampler, texCoord).a));
        float a = clamp(ring + edgeInside, 0.0, 1.0);
        result = vec4(outlineRgb * a, a) + result * (1.0 - a);
    }

    if (shine != 0 && (inside > 0.5 || dist <= max(thickness, 1.0))) {
        float ang = radians(shineAngle);
        vec2 dir = vec2(cos(ang), sin(ang));
        float pos = dot(texCoord - 0.5, dir) + 0.5;
        float center = mix(-0.25, 1.25, fract(shinePhase));
        float band = 1.0 - smoothstep(0.0, max(shineWidth, 0.001), abs(pos - center));
        float a = band * shineIntensity;
        result.rgb += vec3(a) * result.a;
    }

    if (result.a < 0.002) {
        discard;
    }
    fragColor = result;
}
