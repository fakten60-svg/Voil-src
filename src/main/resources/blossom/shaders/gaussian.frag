#version 330 core

uniform sampler2D colTex;
uniform vec2 direction;
uniform float radius;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 texel = 1.0 / vec2(textureSize(colTex, 0));
    float sigma = max(radius, 1.0) * 0.5;
    int taps = int(clamp(ceil(radius), 1.0, 64.0));
    vec4 sum = texture(colTex, texCoord);
    float weightSum = 1.0;
    for (int i = 1; i <= taps; i++) {
        float w = exp(-float(i * i) / (2.0 * sigma * sigma));
        vec2 offset = direction * texel * float(i);
        sum += texture(colTex, texCoord + offset) * w;
        sum += texture(colTex, texCoord - offset) * w;
        weightSum += 2.0 * w;
    }
    fragColor = sum / weightSum;
}
