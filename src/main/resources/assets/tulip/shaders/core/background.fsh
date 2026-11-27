#version 150

uniform vec4 color1;
uniform vec4 color2;
uniform vec2 uSize;
uniform vec2 uLocation;
uniform float radius;
uniform float smoothness;
uniform float u_time;
uniform float u_seed;

out vec4 fragColor;

vec2 seed2() {
    float a = fract(sin(u_seed * 12.9898) * 43758.5453);
    float b = fract(sin((u_seed + 1.0) * 78.233) * 12345.678);
    return vec2(a, b);
}

float hash(vec2 p) {
    vec2 s = seed2();
    vec2 k = vec2(127.1, 311.7) + s * 400.0;
    float h = sin(dot(p + s * 17.0, k));
    h += 0.5 * sin(dot(p * (1.7 + s.x * 0.9), k.yx));
    return fract(h * (43758.5453 + u_seed * 113.0));
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float ralpha(vec2 coord, vec2 halfSize, float r) {
    vec2 d = abs(coord) - halfSize + r;
    return smoothstep(smoothness, 0.0,
    length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - r);
}

void main() {
    vec2 coord = gl_FragCoord.xy - uLocation;

    vec2 halfSize = uSize * 0.5;
    if (any(greaterThan(abs(coord - halfSize), halfSize + smoothness))) discard;

    float alpha = ralpha(coord - halfSize, halfSize, radius);
    if (alpha < 0.01) discard;

    vec2 uv = coord / uSize;
    uv.x *= uSize.x / uSize.y;

    float t = u_time * 0.2;
    vec2 s2 = seed2();

    vec2 offset = vec2(t, t * 0.5) + s2 * 10.0;

    float w = noise(uv * 6.0 + offset) * 0.05;
    vec2 uvw = uv + vec2(w, w);

    float n = noise(uvw * 3.0 + offset);
    float blend = sin((uvw.x + uvw.y) * 10.0 + n * 6.2831) * 0.5 + 0.5;

    fragColor = vec4(mix(color1.rgb, color2.rgb, blend), alpha);
}
