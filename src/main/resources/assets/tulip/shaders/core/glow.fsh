#version 150

layout (std140) uniform Color1 { vec4 color1[1024]; };
layout (std140) uniform Color2 { vec4 color2[1024]; };
layout (std140) uniform Color3 { vec4 color3[1024]; };
layout (std140) uniform Color4 { vec4 color4[1024]; };

layout (std140) uniform USize { vec4 uSize[1024]; };
layout (std140) uniform ULocation { vec4 uLocation[1024]; };
layout (std140) uniform Softness { vec4 softness[1024]; };
layout (std140) uniform Radius { vec4 radius[1024]; };
layout (std140) uniform Spread { vec4 spread[1024]; };

in vec4 v_scissor;
flat in int glInstID;

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float r) {
    vec2 d = abs(pos) - size + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

void main() {
    if (v_scissor.w < gl_FragCoord.y ||
    v_scissor.y > gl_FragCoord.y ||
    v_scissor.x > gl_FragCoord.x ||
    v_scissor.z < gl_FragCoord.x) {
        discard;
    }

    vec2 fragPos = gl_FragCoord.xy;
    vec2 center = uLocation[glInstID].xy + uSize[glInstID].xy * 0.5;
    vec2 halfSize = uSize[glInstID].xy * 0.5;

    float dist = rdist(fragPos - center, halfSize, radius[glInstID].x);

    float s = max(softness[glInstID].x * spread[glInstID].x, 0.001);

    float d1 = dist / (s * 0.2);
    float d2 = dist / (s * 0.5);
    float d3 = dist / s;

    float inner = exp(-d1 * d1);
    float mid   = exp(-d2 * d2 * 0.8);
    float outer = exp(-pow(d3, 1.4));

    float glow = inner * 0.6 + mid * 0.3 + outer * 0.15;
    glow = clamp(glow, 0.0, 1.0);

    if (glow < 0.002) discard;

    vec2 norm = clamp((fragPos - uLocation[glInstID].xy) / uSize[glInstID].xy, 0.0, 1.0);
    vec4 top    = mix(color1[glInstID], color2[glInstID], norm.x);
    vec4 bottom = mix(color3[glInstID], color4[glInstID], norm.x);
    vec4 grad   = mix(top, bottom, norm.y);

    fragColor = vec4(grad.rgb, glow * grad.a);
}