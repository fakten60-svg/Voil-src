#version 330 core

layout (std140) uniform CornerRadii { vec4 cornerRadii[1024]; };
layout (std140) uniform Rect { vec4 rect[1024]; };
layout (std140) uniform Params { vec4 params[1024]; };
layout (std140) uniform Tint { vec4 tint[1024]; };

uniform sampler2D BlurSampler;
uniform vec2 InputResolution;

in vec4 v_scissor;
flat in int glInstID;

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float radius) {
    vec2 d = abs(pos) - size + radius;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - radius;
}

float cornerRadius(vec2 localPos, vec4 radii) {
    if (localPos.x < 0.0 && localPos.y < 0.0) {
        return radii.x;
    } else if (localPos.x >= 0.0 && localPos.y < 0.0) {
        return radii.y;
    } else if (localPos.x < 0.0 && localPos.y >= 0.0) {
        return radii.z;
    }
    return radii.w;
}

void main() {
    if (v_scissor.w < gl_FragCoord.y ||
        v_scissor.y > gl_FragCoord.y ||
        v_scissor.x > gl_FragCoord.x ||
        v_scissor.z < gl_FragCoord.x) {
        discard;
    }

    vec4 r = rect[glInstID];
    vec4 p = params[glInstID];
    vec2 location = r.xy;
    vec2 size = r.zw;
    float saturation = p.x;
    float backgroundTint = p.y;
    float opacity = p.z > 0.0 ? p.z : 1.0;
    float smoothness = max(p.w, 1e-4);

    vec2 center = size * 0.5;
    vec2 localPos = gl_FragCoord.xy - location - center;
    float dist = rdist(localPos, center, cornerRadius(localPos, cornerRadii[glInstID]));
    float alpha = 1.0 - smoothstep(0.0, smoothness, dist);
    if (alpha <= 0.0) {
        discard;
    }

    vec2 res = InputResolution.x > 0.0 ? InputResolution : vec2(textureSize(BlurSampler, 0));
    vec3 blurred = texture(BlurSampler, gl_FragCoord.xy / res).rgb;

    float luminance = dot(blurred, vec3(0.299, 0.587, 0.114));
    vec3 saturated = mix(vec3(luminance), blurred, saturation);
    vec3 finalColor = mix(saturated, tint[glInstID].rgb, backgroundTint);

    fragColor = vec4(finalColor, alpha * opacity);
}
