#version 330 core

uniform sampler2D msdfTexture;
uniform float pxRange;

in vec2 texCoord0;
in vec4 vertexColor;
in vec4 v_scissor;

out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    if (v_scissor.w < gl_FragCoord.y || v_scissor.y > gl_FragCoord.y || v_scissor.x > gl_FragCoord.x || v_scissor.z < gl_FragCoord.x) {
        discard;
    }
    vec3 msd = texture(msdfTexture, texCoord0).rgb;
    float sd = median(msd.r, msd.g, msd.b);
    vec2 unitRange = vec2(pxRange) / vec2(textureSize(msdfTexture, 0));
    vec2 screenTexSize = vec2(1.0) / max(fwidth(texCoord0), vec2(1e-6));
    float screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);
    float opacity = clamp(screenPxRange * (sd - 0.5) + 0.5, 0.0, 1.0);
    float alpha = vertexColor.a * opacity;
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, alpha);
}
