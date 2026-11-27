#version 330 core

uniform vec2 uLocation;
uniform vec2 uSize;
uniform float radius;
uniform float smoothness;
uniform float uThickness;
uniform vec4 uFillColor;
uniform vec4 uStrokeColor;
uniform vec4 uGlowColor;
uniform vec2 uGlowCenter;
uniform vec2 uGlowHalf;
uniform float uGlowSigma;
uniform int uSamples;
uniform float uOpacity;

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float r) {
    vec2 d = abs(pos) - size + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

float erfApprox(float x) {
    float s = sign(x);
    x = abs(x);
    float t = 1.0 / (1.0 + 0.3275911 * x);
    float y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * exp(-x * x);
    return s * y;
}

float boxGlow(vec2 p, vec2 center, vec2 halfSize, float sigma) {
    vec2 q = p - center;
    float k = 1.0 / (max(sigma, 1e-3) * 1.41421356);
    vec2 g = 0.5 * (vec2(erfApprox((q.x + halfSize.x) * k), erfApprox((q.y + halfSize.y) * k))
        - vec2(erfApprox((q.x - halfSize.x) * k), erfApprox((q.y - halfSize.y) * k)));
    return g.x * g.y;
}

void main() {
    vec2 halfSize = uSize * 0.5;
    vec2 local = gl_FragCoord.xy - uLocation;
    float sm = max(smoothness, 1e-4);

    float dist = rdist(local - halfSize, halfSize, radius);
    float shape = 1.0 - smoothstep(-sm, 0.0, dist);
    if (shape <= 0.0) {
        discard;
    }

    float glowAmount = boxGlow(local, uGlowCenter, uGlowHalf, uGlowSigma);
    if (uSamples > 0) {
        float widen = float(clamp(uSamples, 1, 64)) / 64.0;
        glowAmount = pow(glowAmount, mix(1.0, 0.75, widen));
    }
    float glowA = clamp(glowAmount * uGlowColor.a, 0.0, 1.0);

    vec4 base = uFillColor;
    vec3 rgb = mix(base.rgb, uGlowColor.rgb, glowA);
    float a = base.a + glowA * (1.0 - base.a);

    if (uThickness > 0.0) {
        float strokeMask = (1.0 - smoothstep(-sm, 0.0, dist)) * smoothstep(-uThickness - sm, -uThickness, dist);
        float strokeA = strokeMask * uStrokeColor.a;
        rgb = mix(rgb, uStrokeColor.rgb, strokeA);
        a = a + strokeA * (1.0 - a);
    }

    float outA = a * shape * uOpacity;
    if (outA < 0.004) {
        discard;
    }
    fragColor = vec4(rgb, outA);
}
