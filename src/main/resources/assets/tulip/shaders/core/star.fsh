#version 330 core

uniform vec2 uLocation;
uniform vec2 uSize;
uniform float starRadius;
uniform float outlineThickness;
uniform float rotation;
uniform float pointiness;
uniform float cornerRadius;
uniform float filled;
uniform float outlined;
uniform float smoothness;
uniform vec4 fillColor;
uniform vec4 outlineColor;

out vec4 fragColor;

float sdStar5(vec2 p, float r, float rf) {
    const vec2 k1 = vec2(0.809016994375, -0.587785252292);
    const vec2 k2 = vec2(-k1.x, k1.y);
    p.x = abs(p.x);
    p -= 2.0 * max(dot(k1, p), 0.0) * k1;
    p -= 2.0 * max(dot(k2, p), 0.0) * k2;
    p.x = abs(p.x);
    p.y -= r;
    vec2 ba = rf * vec2(-k1.y, k1.x) - vec2(0.0, 1.0);
    float h = clamp(dot(p, ba) / dot(ba, ba), 0.0, r);
    return length(p - ba * h) * sign(p.y * ba.x - p.x * ba.y);
}

void main() {
    vec2 p = gl_FragCoord.xy - (uLocation + uSize * 0.5);
    float rot = radians(rotation);
    float c = cos(rot);
    float s = sin(rot);
    p = vec2(c * p.x + s * p.y, -s * p.x + c * p.y);

    float cr = clamp(cornerRadius, 0.0, starRadius * 0.5);
    float rf = clamp(pointiness, 0.05, 0.95);
    float d = sdStar5(p, starRadius - cr, rf) - cr;

    float sm = max(smoothness, 1e-4);
    float fillA = filled > 0.5 ? (1.0 - smoothstep(-sm, 0.0, d)) * fillColor.a : 0.0;

    float ring = abs(d + outlineThickness * 0.5) - outlineThickness * 0.5;
    float outA = outlined > 0.5 ? (1.0 - smoothstep(-sm, 0.0, ring)) * outlineColor.a : 0.0;

    float a = outA + fillA * (1.0 - outA);
    if (a < 0.004) {
        discard;
    }
    vec3 rgb = (outlineColor.rgb * outA + fillColor.rgb * fillA * (1.0 - outA)) / a;
    fragColor = vec4(rgb, a);
}
