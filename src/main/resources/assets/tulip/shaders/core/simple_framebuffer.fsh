#version 330 core

uniform sampler2D frameSampler;
uniform float testUniform;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(frameSampler, texCoord0);
    if (color.a < 0.004) {
        discard;
    }
    if (testUniform > 1.0) {
        color.rgb = mix(color.rgb, vec3(1.0, 0.0, 1.0), 0.5);
    }
    fragColor = color;
}
