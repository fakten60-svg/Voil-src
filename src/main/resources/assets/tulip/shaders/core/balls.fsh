#version 330 core

uniform float radius;
uniform float smoothness;
uniform vec2 uLocation;
uniform vec2 uSize;
uniform vec4 uBalls[16];
uniform vec2 uPhase[16];

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float r) {
    vec2 d = abs(pos) - size + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

vec3 palette(float t) {
    return 0.5 + 0.5 * cos(6.2831853 * (vec3(0.0, 0.33, 0.67) + t));
}

void main() {
    vec2 halfSize = uSize * 0.5;
    vec2 local = gl_FragCoord.xy - uLocation;
    float dist = rdist(local - halfSize, halfSize, radius);
    float shape = 1.0 - smoothstep(0.0, max(smoothness, 1e-4), dist);
    if (shape <= 0.0) {
        discard;
    }

    float aspect = uSize.x / max(uSize.y, 1.0);
    vec2 uv = local / max(uSize.y, 1.0);

    float field = 0.0;
    vec3 tint = vec3(0.0);
    for (int i = 0; i < 16; i++) {
        float r = uBalls[i].z;
        if (r <= 0.0) {
            continue;
        }
        vec2 drift = vec2(uPhase[i].x * 0.12 * aspect, uPhase[i].y * 0.12);
        vec2 center = vec2(uBalls[i].x, uBalls[i].y) + drift;
        center.x = mod(center.x, aspect);
        vec2 d = uv - center;
        d.x = d.x - aspect * floor(d.x / aspect + 0.5);
        float influence = (r * r) / (dot(d, d) + 1e-4);
        field += influence;
        tint += palette(uBalls[i].w) * influence;
    }

    float blob = smoothstep(0.9, 1.1, field);
    vec3 rgb = tint / max(field, 1e-4);
    float glow = clamp(field * 0.35, 0.0, 1.0);
    float a = max(blob, glow * 0.45) * shape;
    if (a < 0.01) {
        discard;
    }
    fragColor = vec4(rgb, a);
}
