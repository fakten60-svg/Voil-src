#version 330 core

uniform sampler2D texSampler;
uniform vec2 halfPixel;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 sum = texture(texSampler, texCoord) * 4.0;
    sum += texture(texSampler, texCoord - halfPixel);
    sum += texture(texSampler, texCoord + halfPixel);
    sum += texture(texSampler, texCoord + vec2(halfPixel.x, -halfPixel.y));
    sum += texture(texSampler, texCoord - vec2(halfPixel.x, -halfPixel.y));
    fragColor = sum / 8.0;
}
