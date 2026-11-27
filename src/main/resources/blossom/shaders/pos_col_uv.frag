#version 330 core

uniform sampler2D texSampler;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(texSampler, texCoord0) * vertexColor;
    if (color.a < 0.01) {
        discard;
    }
    fragColor = color;
}
