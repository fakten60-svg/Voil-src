#version 150

uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

uniform vec2  uSize;
uniform vec2  uLocation;
uniform float radius;
uniform float smoothness;

uniform float uAngleDeg;
uniform float uThickness;
uniform vec2  uArm;

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float radius) {
    vec2 d = abs(pos) - size + radius;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - radius;
}
float ralpha(vec2 coord, vec2 size, float radius, float smoothness) {
    vec2 center = size * 0.5;
    float dist = rdist(coord - center, center, radius);
    return 1.0 - smoothstep(0.0, smoothness, dist);
}
float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 d = abs(p) - b + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}
vec3 createGradient(vec2 coords, vec3 c1, vec3 c2, vec3 c3, vec3 c4) {
    return mix(mix(c1, c3, coords.x), mix(c2, c4, coords.x), coords.y);
}
float sdBox(vec2 p, vec2 b) {
    vec2 d = abs(p) - b;
    return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
}
mat2 rot(float a){ float c=cos(a), s=sin(a); return mat2(c,-s,s,c); }

void main() {
    vec2 posPx = gl_FragCoord.xy - uLocation;
    vec2 center = 0.5 * uSize;
    vec2 lp = rot(-radians(uAngleDeg)) * (posPx - center) + center;

    float t = max(uThickness, 0.0);
    float r = max(radius, 0.0);

    float hx = max(uArm.x, 0.0);
    vec2 hHalf = vec2(0.5 * (r + hx), 0.5 * t);
    vec2 hCtr  = vec2(hHalf.x, 0.5 * t);
    float dH = sdRoundedBox(lp - hCtr, hHalf, r);

    float vy = max(uArm.y, 0.0);
    vec2 vHalf = vec2(0.5 * t, 0.5 * (r + vy));
    vec2 vCtr  = vec2(0.5 * t, vHalf.y);
    float dV = sdRoundedBox(lp - vCtr, vHalf, r);

    float d = min(dH, dV);
    float alpha = 1.0 - smoothstep(0.0, max(1e-5, smoothness), d);

    vec2 uv = clamp((gl_FragCoord.xy - uLocation) / uSize, 0.0, 1.0);
    vec3 grad = createGradient(uv, color1.rgb, color2.rgb, color3.rgb, color4.rgb);
    vec4 col = vec4(grad, color1.a * alpha);

    if (col.a < 0.01) discard;
    fragColor = col;
}
