#version 330 core

in vec2 vPos;
in float vRadius;
in vec2 vCenter;
in vec4 vColor;

out vec4 fragColor;

void main() {
    float dist = length(vPos - vCenter) - vRadius;
    float alpha = clamp(0.5 - dist, 0.0, 1.0);
    if (alpha <= 0.0) {
        discard;
    }
    fragColor = vec4(vColor.rgb, vColor.a * alpha);
}
