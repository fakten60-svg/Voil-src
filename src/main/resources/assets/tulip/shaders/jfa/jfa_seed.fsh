#version 330 core

uniform sampler2D texSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float coverage = texture(texSampler, texCoord).a;
    if (coverage > 0.01) {
        fragColor = vec4(floor(gl_FragCoord.xy) + 0.5, 0.0, 0.0);
    } else {
        fragColor = vec4(-1.0, -1.0, 0.0, 0.0);
    }
}
