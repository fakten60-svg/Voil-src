#version 330 core

uniform sampler2D texSampler;
uniform vec2 halfPixel;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 sum = texture(texSampler, texCoord + vec2(-halfPixel.x * 2.0, 0.0));
    sum += texture(texSampler, texCoord + vec2(-halfPixel.x, halfPixel.y)) * 2.0;
    sum += texture(texSampler, texCoord + vec2(0.0, halfPixel.y * 2.0));
    sum += texture(texSampler, texCoord + vec2(halfPixel.x, halfPixel.y)) * 2.0;
    sum += texture(texSampler, texCoord + vec2(halfPixel.x * 2.0, 0.0));
    sum += texture(texSampler, texCoord + vec2(halfPixel.x, -halfPixel.y)) * 2.0;
    sum += texture(texSampler, texCoord + vec2(0.0, -halfPixel.y * 2.0));
    sum += texture(texSampler, texCoord + vec2(-halfPixel.x, -halfPixel.y)) * 2.0;
    fragColor = sum / 12.0;
}
