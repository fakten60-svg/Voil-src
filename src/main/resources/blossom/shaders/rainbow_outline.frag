#version 330 core

uniform sampler2D colTex;
uniform float time;
uniform float rainbowSpeed;
uniform float saturation;
uniform float brightness;

in vec2 texCoord;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(c.xxx + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

void main() {
    vec2 texel = 1.0 / vec2(textureSize(colTex, 0));
    float center = texture(colTex, texCoord).a;
    if (center > 0.01) {
        discard;
    }
    float edge = 0.0;
    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            if (x == 0 && y == 0) {
                continue;
            }
            float falloff = 1.0 - clamp(length(vec2(x, y)) / 3.0, 0.0, 1.0);
            edge = max(edge, texture(colTex, texCoord + vec2(x, y) * texel).a * falloff);
        }
    }
    float alpha = clamp(edge * 1.5, 0.0, 1.0);
    if (alpha < 0.01) {
        discard;
    }
    float hue = fract(time * rainbowSpeed + (texCoord.x + texCoord.y) * 0.5);
    vec3 rgb = hsv2rgb(vec3(hue, clamp(1.0 - saturation, 0.0, 1.0), brightness));
    fragColor = vec4(rgb * alpha, alpha);
}
