#version 330 core

layout (std140) uniform Color1 { vec4 color1[1024]; };
layout (std140) uniform Color2 { vec4 color2[1024]; };
layout (std140) uniform Color3 { vec4 color3[1024]; };
layout (std140) uniform Color4 { vec4 color4[1024]; };

layout (std140) uniform USize { vec4 uSize[1024]; };
layout (std140) uniform ULocation { vec4 uLocation[1024]; };
layout (std140) uniform CornerRadii { vec4 cornerRadii[1024]; };
layout (std140) uniform Smoothness { vec4 smoothness[1024]; };

in vec4 v_scissor;
flat in int glInstID;

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float radius) {
    vec2 d = abs(pos) - size + radius;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - radius;
}

float ralpha(vec2 coord, vec2 size, vec4 radii, float smoothness) {
    vec2 center = size * 0.5;

        float radius;
    if (coord.x < center.x && coord.y < center.y) {
                radius = radii.x;
    } else if (coord.x >= center.x && coord.y < center.y) {
                radius = radii.y;
    } else if (coord.x < center.x && coord.y >= center.y) {
                radius = radii.z;
    } else {
                radius = radii.w;
    }

    float dist = rdist(coord - center, center, radius);
    return 1.0 - smoothstep(0.0, smoothness, dist);
}

vec3 createGradient(vec2 coords, vec3 c1, vec3 c2, vec3 c3, vec3 c4) {
    return mix(mix(c1, c3, coords.x), mix(c2, c4, coords.x), coords.y);
}

void main() {
    if (v_scissor.w < gl_FragCoord.y ||
    v_scissor.y > gl_FragCoord.y ||
    v_scissor.x > gl_FragCoord.x ||
    v_scissor.z < gl_FragCoord.x) {
        discard;
    }

    vec2 fragPos = (gl_FragCoord.xy - uLocation[glInstID].xy) / uSize[glInstID].xy;
    float alpha = ralpha(gl_FragCoord.xy - uLocation[glInstID].xy, uSize[glInstID].xy, cornerRadii[glInstID], smoothness[glInstID].x);

    vec4 color = vec4(createGradient(fragPos, color1[glInstID].rgb, color2[glInstID].rgb, color3[glInstID].rgb, color4[glInstID].rgb), color1[glInstID].a * alpha);

    if (color.a < 0.01) discard;

    fragColor = color;
}