#version 330 core

uniform sampler2D colTex;
uniform vec4 color;

in vec2 texCoord;

out vec4 fragColor;

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
    float alpha = clamp(edge * 1.5, 0.0, 1.0) * color.a;
    if (alpha < 0.01) {
        discard;
    }
    fragColor = vec4(color.rgb * alpha, alpha);
}
