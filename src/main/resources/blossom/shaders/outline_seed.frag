#version 330 core

uniform sampler2D texSampler;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float coverage = texture(texSampler, texCoord0).a;
    if (coverage < 0.1) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, 1.0);
}
