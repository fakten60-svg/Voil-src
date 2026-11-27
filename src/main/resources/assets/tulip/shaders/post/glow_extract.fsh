#version 330 core

uniform sampler2D texSampler;
uniform sampler2D jfaTex;
uniform float glowRadius;
uniform float colorFrequency;
uniform float time;
uniform float speed;
uniform float glowIntensity;
uniform int colorSource;
uniform vec4 damagedColor;
uniform vec4 fillColor;
uniform vec4 outlineColor;

in vec2 texCoord;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(c.xxx + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

vec3 resolveColor(vec2 seedPixel, vec2 uv) {
    if (colorSource == 1) {
        vec3 maskColor = texelFetch(texSampler, ivec2(seedPixel), 0).rgb;
        if (dot(maskColor, maskColor) > 0.0) {
            return maskColor;
        }
    } else if (colorSource == 2) {
        return damagedColor.rgb;
    }
    if (outlineColor.a > 0.0) {
        return outlineColor.rgb;
    }
    float hue = fract(time * speed * 0.2 + (uv.x + uv.y) * max(colorFrequency, 0.05));
    return hsv2rgb(vec3(hue, 0.75, 1.0));
}

void main() {
    vec2 jfaSize = vec2(textureSize(jfaTex, 0));
    vec2 seed = texture(jfaTex, texCoord).xy;
    if (seed.x < 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    float dist = length(seed - texCoord * jfaSize);
    float radius = max(glowRadius, 1.0);
    float falloff = clamp(1.0 - dist / radius, 0.0, 1.0);
    float g = falloff * falloff;
    if (dist < 1.0) {
        g = 1.0;
    }

    vec3 color = resolveColor(seed, texCoord);
    fragColor = vec4(color * g, g);
}
