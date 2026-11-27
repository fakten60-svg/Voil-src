#version 330 core

uniform sampler2D texSampler;
uniform sampler2D jfaTex;

uniform vec3 color;
uniform vec3 color2;
uniform vec3 color3;
uniform vec3 color4;
uniform float time;
uniform float speed;
uniform float wireAlpha;
uniform float thickness;
uniform float glowIntensity;
uniform float glowRadius;

in vec2 texCoord;

out vec4 fragColor;

vec3 gradient(float t) {
    t = fract(t) * 4.0;
    if (t < 1.0) {
        return mix(color, color2, t);
    } else if (t < 2.0) {
        return mix(color2, color3, t - 1.0);
    } else if (t < 3.0) {
        return mix(color3, color4, t - 2.0);
    }
    return mix(color4, color, t - 3.0);
}

void main() {
    float mask = texture(texSampler, texCoord).a;
    float inside = step(0.01, mask);

    vec2 seed = texture(jfaTex, texCoord).xy;
    float dist = seed.x < 0.0 ? 1e6 : length(seed - gl_FragCoord.xy);

    float phase = time * speed * 0.2 + (texCoord.x + texCoord.y) * 0.5;
    vec3 base = gradient(phase);

    vec4 result = vec4(0.0);

    float radius = max(glowRadius, 1.0);
    float falloff = clamp(1.0 - dist / radius, 0.0, 1.0);
    float glowA = falloff * falloff * glowIntensity * (1.0 - inside);
    result += vec4(base * glowA, glowA);

    if (inside > 0.5) {
        vec2 p = gl_FragCoord.xy;
        float spacing = 10.0;
        vec2 g = abs(fract(p / spacing - 0.5) - 0.5) * spacing;
        float lineWidth = max(thickness, 1.0) * 0.5;
        float gridLine = 1.0 - smoothstep(lineWidth - 0.5, lineWidth + 0.5, min(g.x, g.y));
        vec2 d = abs(fract((vec2(p.x + p.y, p.x - p.y)) / (spacing * 1.4142) - 0.5) - 0.5) * spacing * 1.4142;
        float diagLine = 1.0 - smoothstep(lineWidth - 0.5, lineWidth + 0.5, min(d.x, d.y));
        float wire = max(gridLine, diagLine * 0.6) * wireAlpha;
        result = vec4(base * wire, wire) + result * (1.0 - wire);
    }

    float edgeWidth = max(thickness, 1.0);
    float edge = (1.0 - smoothstep(edgeWidth - 1.0, edgeWidth, dist)) * (1.0 - inside);
    result = vec4(base * edge, edge) + result * (1.0 - edge);

    if (result.a < 0.002) {
        discard;
    }
    fragColor = result;
}
