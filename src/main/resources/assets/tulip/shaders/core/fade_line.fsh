#version 330 core

uniform vec2 uLocation;
uniform vec2 uSize;
uniform int uDirection;
uniform float uThickness;
uniform vec4 color1;

out vec4 fragColor;

void main() {
    vec2 local = gl_FragCoord.xy - uLocation;
    vec2 uv = clamp(local / max(uSize, vec2(1.0)), 0.0, 1.0);

    float along;
    float across;
    float halfThickness = max(uThickness, 1.0) * 0.5;
    if (uDirection == 0) {
        along = uv.x;
        across = abs(local.y - uSize.y * 0.5);
    } else {
        along = uv.y;
        across = abs(local.x - uSize.x * 0.5);
    }

    float edge = 1.0 - smoothstep(halfThickness - 1.0, halfThickness, across);
    float fade = 1.0 - along;
    fade *= fade;
    float a = color1.a * edge * fade;
    if (a < 0.004) {
        discard;
    }
    fragColor = vec4(color1.rgb, a);
}
