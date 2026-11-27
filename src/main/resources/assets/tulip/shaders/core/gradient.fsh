#version 330 core

uniform float radius;
uniform float smoothness;
uniform float uThickness;
uniform vec2 uLocation;
uniform vec2 uSize;
uniform vec2 uFrameLocation;
uniform vec2 uFrameSize;
uniform float uType;
uniform float uAngle;
uniform float uCount;
uniform float uChecker;
uniform float uOpacity;
uniform vec4 uStops0;
uniform vec4 uStops1;
uniform vec4 uColor0;
uniform vec4 uColor1;
uniform vec4 uColor2;
uniform vec4 uColor3;
uniform vec4 uColor4;
uniform vec4 uColor5;
uniform vec4 uColor6;
uniform vec4 uColor7;

out vec4 fragColor;

float rdist(vec2 pos, vec2 size, float r) {
    vec2 d = abs(pos) - size + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

float stopAt(int i) {
    return i < 4 ? uStops0[i] : uStops1[i - 4];
}

vec4 colorAt(int i) {
    if (i == 0) return uColor0;
    if (i == 1) return uColor1;
    if (i == 2) return uColor2;
    if (i == 3) return uColor3;
    if (i == 4) return uColor4;
    if (i == 5) return uColor5;
    if (i == 6) return uColor6;
    return uColor7;
}

vec4 sampleGradient(float t) {
    int n = clamp(int(uCount + 0.5), 1, 8);
    if (n == 1) {
        return colorAt(0);
    }
    t = clamp(t, 0.0, 1.0);
    vec4 result = colorAt(0);
    float prevStop = stopAt(0);
    if (t <= prevStop) {
        return result;
    }
    for (int i = 1; i < 8; i++) {
        if (i >= n) {
            break;
        }
        float nextStop = stopAt(i);
        vec4 nextColor = colorAt(i);
        if (t <= nextStop) {
            float span = max(nextStop - prevStop, 1e-5);
            return mix(result, nextColor, (t - prevStop) / span);
        }
        result = nextColor;
        prevStop = nextStop;
    }
    return result;
}

void main() {
    vec2 center = uSize * 0.5;
    vec2 local = gl_FragCoord.xy - uLocation;
    float dist = rdist(local - center, center, radius);
    float shape = 1.0 - smoothstep(0.0, max(smoothness, 1e-4), dist);
    if (uThickness > 0.0) {
        float inner = rdist(local - center, center - uThickness, max(radius - uThickness, 0.0));
        shape *= smoothstep(0.0, max(smoothness, 1e-4), inner) ;
    }
    if (shape <= 0.0) {
        discard;
    }

    vec2 frameSize = max(uFrameSize, vec2(1.0));
    vec2 uv = clamp((gl_FragCoord.xy - uFrameLocation) / frameSize, 0.0, 1.0);

    float t;
    if (uType > 0.5) {
        vec2 q = (uv - 0.5) * 2.0;
        t = clamp(length(q), 0.0, 1.0);
    } else {
        float a = radians(uAngle);
        vec2 dir = vec2(cos(a), sin(a));
        vec2 q = uv - 0.5;
        float extent = abs(dir.x) * 0.5 + abs(dir.y) * 0.5;
        t = dot(q, dir) / max(2.0 * extent, 1e-5) + 0.5;
    }

    vec4 color = sampleGradient(t);
    vec3 rgb = color.rgb;
    float a = color.a;

    if (uChecker > 0.0) {
        vec2 cell = floor(local / uChecker);
        float check = mod(cell.x + cell.y, 2.0);
        vec3 bg = mix(vec3(0.8), vec3(0.55), check);
        rgb = mix(bg, rgb, a);
        a = 1.0;
    }

    float outA = a * shape * uOpacity;
    if (outA < 0.004) {
        discard;
    }
    fragColor = vec4(rgb, outA);
}
