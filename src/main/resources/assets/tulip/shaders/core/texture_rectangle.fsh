#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

uniform vec2 uSize;
uniform vec2 uLocation;
uniform vec4 radius;
uniform float smoothness;

in vec2 texCoord0;
out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float r) {
    vec2 d = abs(pos) - size + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

float ralpha(vec2 coord, vec2 size, vec4 radii, float smoothness) {
    vec2 center = size * 0.5;

    float r;
    if (coord.x < center.x && coord.y < center.y) {
        r = radii.x;
    } else if (coord.x >= center.x && coord.y < center.y) {
        r = radii.y;
    } else if (coord.x < center.x && coord.y >= center.y) {
        r = radii.z;
    } else {
        r = radii.w;
    }

    float dist = rdist(coord - center, center, r);
    return 1.0 - smoothstep(0.0, smoothness, dist);
}

void main() {
    float alpha = ralpha(gl_FragCoord.xy - uLocation, uSize, radius, smoothness);

    vec4 texColor = texture(Sampler0, texCoord0);
    vec4 color = vec4(texColor.rgb * ColorModulator.rgb, texColor.a * alpha * ColorModulator.a);

    if (color.a < 0.01) discard;

    fragColor = color;
}