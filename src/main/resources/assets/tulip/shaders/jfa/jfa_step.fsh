#version 330 core

uniform sampler2D jfaTex;
uniform float uStep;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    ivec2 size = textureSize(jfaTex, 0);
    ivec2 base = ivec2(gl_FragCoord.xy);
    vec2 pixel = gl_FragCoord.xy;
    int stride = max(int(uStep + 0.5), 1);

    vec2 best = vec2(-1.0);
    float bestDist = 1e20;

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            ivec2 coord = base + ivec2(x, y) * stride;
            if (coord.x < 0 || coord.y < 0 || coord.x >= size.x || coord.y >= size.y) {
                continue;
            }
            vec2 seed = texelFetch(jfaTex, coord, 0).xy;
            if (seed.x < 0.0) {
                continue;
            }
            vec2 delta = seed - pixel;
            float d = dot(delta, delta);
            if (d < bestDist) {
                bestDist = d;
                best = seed;
            }
        }
    }

    fragColor = vec4(best, 0.0, 0.0);
}
