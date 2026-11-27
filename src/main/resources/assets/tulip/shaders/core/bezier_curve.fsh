#version 330 core

uniform vec2 uLocation;
uniform vec2 uSize;
uniform vec2 p0;
uniform vec2 p1;
uniform vec2 p2;
uniform vec2 p3;
uniform float thickness;
uniform float smoothness;
uniform vec4 color1;
uniform vec4 color2;

out vec4 fragColor;

vec2 bezier(float t) {
    float u = 1.0 - t;
    return u * u * u * p0 + 3.0 * u * u * t * p1 + 3.0 * u * t * t * p2 + t * t * t * p3;
}

void main() {
    vec2 p = gl_FragCoord.xy - uLocation;

    const int steps = 48;
    float bestDist = 1e20;
    float bestT = 0.0;
    vec2 prev = bezier(0.0);
    for (int i = 1; i <= steps; i++) {
        float t1 = float(i) / float(steps);
        vec2 cur = bezier(t1);
        vec2 ab = cur - prev;
        float h = clamp(dot(p - prev, ab) / max(dot(ab, ab), 1e-6), 0.0, 1.0);
        float d = length(p - (prev + ab * h));
        if (d < bestDist) {
            bestDist = d;
            bestT = (float(i - 1) + h) / float(steps);
        }
        prev = cur;
    }

    float d = bestDist - thickness * 0.5;
    float alpha = 1.0 - smoothstep(0.0, max(smoothness, 1e-4), d);
    vec4 color = mix(color1, color2, bestT);
    float a = color.a * alpha;
    if (a < 0.01) {
        discard;
    }
    fragColor = vec4(color.rgb, a);
}
