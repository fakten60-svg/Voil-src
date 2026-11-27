#version 330 core

uniform float armLength;
uniform float radius;
uniform float cornerRadius;
uniform float angle;
uniform float rotation;
uniform float smoothness;
uniform vec2 uLocation;
uniform vec2 uSize;
uniform vec4 uColor;

out vec4 fragColor;

float sdSegment(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / max(dot(ba, ba), 1e-6), 0.0, 1.0);
    return length(pa - ba * h);
}

float smin(float a, float b, float k) {
    if (k <= 0.0) {
        return min(a, b);
    }
    float h = max(k - abs(a - b), 0.0) / k;
    return min(a, b) - h * h * k * 0.25;
}

void main() {
    vec2 center = uLocation + uSize * 0.5;
    float rot = radians(rotation);
    float c = cos(rot);
    float s = sin(rot);
    vec2 p = gl_FragCoord.xy - center;
    p = vec2(c * p.x + s * p.y, -s * p.x + c * p.y);

    float half_ = radians(angle) * 0.5;
    vec2 dirA = vec2(-cos(half_), sin(half_));
    vec2 dirB = vec2(-cos(half_), -sin(half_));
    vec2 tip = vec2(armLength * cos(half_) * 0.5, 0.0);

    float dA = sdSegment(p, tip, tip + dirA * armLength);
    float dB = sdSegment(p, tip, tip + dirB * armLength);
    float d = smin(dA, dB, cornerRadius) - radius;

    float alpha = 1.0 - smoothstep(0.0, max(smoothness, 1e-4), d);
    float a = uColor.a * alpha;
    if (a < 0.01) {
        discard;
    }
    fragColor = vec4(uColor.rgb, a);
}
