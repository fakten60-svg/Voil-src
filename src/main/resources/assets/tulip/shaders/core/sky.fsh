#version 330 core

uniform sampler3D uNoise;
uniform float uTime;
uniform vec2 uResolution;
uniform mat4 uInvProjection;
uniform mat4 uInvView;

uniform vec3 uHorizonColor;
uniform vec3 uZenithColor;
uniform vec3 uSkyMidColor;

uniform float uStarDensity;
uniform vec3 uStarColor;
uniform float uEnableShootingStars;
uniform vec3 uShootingStarColor;

uniform float uAuroraIntensity;
uniform vec3 uAuroraColor1;
uniform vec3 uAuroraColor2;
uniform vec3 uAuroraColor3;
uniform float uAuroraSpeed;

uniform float uNebulaIntensity;
uniform float uEnableNebula;
uniform vec3 uNebulaColor1;
uniform vec3 uNebulaColor2;

uniform float uEnableSun;
uniform vec3 uSunColor;
uniform float uSunIntensity;
uniform float uSunSize;
uniform float uEnableMoon;
uniform vec3 uMoonColor;
uniform float uMoonIntensity;
uniform float uMoonSize;

uniform float uEnableClouds;
uniform float uCloudCoverage;
uniform float uCloudDensity;
uniform float uCloudScale;
uniform float uCloudSpeed;
uniform float uCloudBottom;
uniform float uCloudTop;
uniform float uCloudAbsorption;
uniform float uCloudAmbient;
uniform int uCloudSteps;
uniform vec3 uCloudColor;
uniform vec3 uCloudShadowColor;

uniform float uVignetteIntensity;

in vec2 vNdc;

out vec4 fragColor;

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

vec3 sunDirection() {
    float a = uTime * 0.005;
    return normalize(vec3(sin(a) * 0.35, cos(a) * 0.9, sin(a) * 0.9));
}

vec3 moonDirection() {
    return -sunDirection();
}

vec3 skyGradient(vec3 dir) {
    float h = clamp(dir.y, -1.0, 1.0);
    float up = clamp(h, 0.0, 1.0);
    vec3 col = mix(uHorizonColor, uSkyMidColor, smoothstep(0.0, 0.25, up));
    col = mix(col, uZenithColor, smoothstep(0.2, 0.9, up));
    float below = clamp(-h, 0.0, 1.0);
    col = mix(col, uHorizonColor * 0.35, smoothstep(0.0, 0.3, below));
    return col;
}

vec3 stars(vec3 dir) {
    if (uStarDensity <= 0.0 || dir.y < -0.05) {
        return vec3(0.0);
    }
    vec3 p = dir * 140.0;
    vec3 cell = floor(p);
    vec3 local = fract(p) - 0.5;
    float rnd = hash13(cell);
    float threshold = 1.0 - clamp(uStarDensity, 0.0, 1.0) * 0.035;
    if (rnd < threshold) {
        return vec3(0.0);
    }
    float size = 0.08 + 0.12 * hash13(cell + 7.0);
    float d = length(local);
    float twinkle = 0.75 + 0.25 * sin(uTime * (1.0 + 4.0 * hash13(cell + 3.0)) + rnd * 40.0);
    float star = smoothstep(size, 0.0, d) * twinkle;
    float fade = smoothstep(-0.05, 0.25, dir.y);
    return uStarColor * star * fade;
}

vec3 shootingStars(vec3 dir) {
    if (uEnableShootingStars < 0.5) {
        return vec3(0.0);
    }
    vec3 result = vec3(0.0);
    for (int i = 0; i < 3; i++) {
        float period = 7.0 + float(i) * 3.3;
        float t = uTime * 0.5 + float(i) * 11.7;
        float cycle = floor(t / period);
        float local = fract(t / period) * period;
        if (local > 1.2) {
            continue;
        }
        vec2 r = hash22(vec2(cycle, float(i) + 1.0));
        float az = r.x * 6.2831853;
        vec3 start = normalize(vec3(cos(az), 0.35 + 0.5 * r.y, sin(az)));
        vec3 tangent = normalize(cross(start, vec3(0.0, 1.0, 0.0)));
        vec3 head = normalize(start + tangent * local * 0.5 - vec3(0.0, local * 0.15, 0.0));
        vec3 tail = normalize(start + tangent * max(local - 0.2, 0.0) * 0.5 - vec3(0.0, max(local - 0.2, 0.0) * 0.15, 0.0));
        vec3 seg = head - tail;
        float h = clamp(dot(dir - tail, seg) / max(dot(seg, seg), 1e-6), 0.0, 1.0);
        float d = length(dir - (tail + seg * h));
        float streak = smoothstep(0.004, 0.0, d) * h;
        float life = 1.0 - smoothstep(0.8, 1.2, local);
        result += uShootingStarColor * streak * life * 2.0;
    }
    return result;
}

vec3 aurora(vec3 dir) {
    if (uAuroraIntensity <= 0.0 || dir.y < 0.02) {
        return vec3(0.0);
    }
    float t = uTime * uAuroraSpeed * 0.02;
    vec3 acc = vec3(0.0);
    for (int i = 0; i < 12; i++) {
        float layer = float(i) / 12.0;
        float height = 0.12 + layer * 0.35;
        float tt = height / max(dir.y, 0.05);
        vec3 p = dir * tt;
        vec3 np = vec3(p.x * 0.015 + t, p.z * 0.015, layer * 0.5);
        float n = texture(uNoise, np).r;
        float n2 = texture(uNoise, np * 2.3 + vec3(0.0, t, 0.0)).g;
        float curtain = smoothstep(0.45, 0.75, n) * (0.5 + 0.5 * n2);
        vec3 c = mix(uAuroraColor1, uAuroraColor2, layer);
        c = mix(c, uAuroraColor3, smoothstep(0.5, 1.0, n2));
        acc += c * curtain * (1.0 - layer * 0.6);
    }
    float fade = smoothstep(0.02, 0.3, dir.y) * (1.0 - smoothstep(0.7, 1.0, dir.y));
    return acc * uAuroraIntensity * fade * 0.12;
}

vec3 nebula(vec3 dir) {
    if (uEnableNebula < 0.5 || uNebulaIntensity <= 0.0) {
        return vec3(0.0);
    }
    vec3 p = dir * 1.5 + vec3(uTime * 0.0005);
    float n1 = texture(uNoise, p).r;
    float n2 = texture(uNoise, p * 2.1 + 0.37).g;
    float n3 = texture(uNoise, p * 4.3 + 0.71).b;
    float density = smoothstep(0.35, 0.85, n1 * 0.6 + n2 * 0.3 + n3 * 0.1);
    vec3 c = mix(uNebulaColor1, uNebulaColor2, n2);
    float fade = smoothstep(-0.1, 0.4, dir.y);
    return c * density * uNebulaIntensity * fade;
}

vec3 celestial(vec3 dir) {
    vec3 result = vec3(0.0);
    if (uEnableSun > 0.5) {
        vec3 sd = sunDirection();
        float cosAngle = dot(dir, sd);
        float size = max(uSunSize, 0.001);
        float disc = smoothstep(cos(size), cos(size * 0.9), cosAngle);
        float halo = pow(max(cosAngle, 0.0), 24.0 / max(size * 20.0, 0.2)) * 0.5;
        result += uSunColor * (disc + halo) * uSunIntensity;
    }
    if (uEnableMoon > 0.5) {
        vec3 md = moonDirection();
        float cosAngle = dot(dir, md);
        float size = max(uMoonSize, 0.001);
        float disc = smoothstep(cos(size), cos(size * 0.92), cosAngle);
        float halo = pow(max(cosAngle, 0.0), 40.0 / max(size * 20.0, 0.2)) * 0.25;
        result += uMoonColor * (disc + halo) * uMoonIntensity;
    }
    return result;
}

float cloudDensity(vec3 p) {
    vec3 q = p * (uCloudScale * 0.0005) + vec3(uTime * uCloudSpeed * 0.0004, 0.0, uTime * uCloudSpeed * 0.0002);
    float base = texture(uNoise, q).a;
    float detail = texture(uNoise, q * 3.1).g;
    float shape = base * 0.7 + detail * 0.3;
    float threshold = 1.0 - clamp(uCloudCoverage, 0.0, 1.0);
    return max(shape - threshold, 0.0) * uCloudDensity * 4.0;
}

vec4 clouds(vec3 dir, vec3 lightDir, vec3 lightColor) {
    if (uEnableClouds < 0.5 || dir.y <= 0.01) {
        return vec4(0.0, 0.0, 0.0, 0.0);
    }
    float t0 = uCloudBottom / dir.y;
    float t1 = uCloudTop / dir.y;
    int steps = clamp(uCloudSteps, 4, 64);
    float dt = (t1 - t0) / float(steps);
    float jitter = hash13(vec3(gl_FragCoord.xy, uTime));
    float transmittance = 1.0;
    vec3 scatter = vec3(0.0);
    for (int i = 0; i < 64; i++) {
        if (i >= steps || transmittance < 0.02) {
            break;
        }
        float t = t0 + (float(i) + jitter) * dt;
        vec3 p = dir * t;
        float d = cloudDensity(p);
        if (d <= 0.0) {
            continue;
        }
        float lightDensity = cloudDensity(p + lightDir * (uCloudTop - uCloudBottom) * 0.15);
        float shadow = exp(-lightDensity * uCloudAbsorption);
        vec3 lit = mix(uCloudShadowColor, uCloudColor * lightColor, shadow);
        vec3 ambient = mix(uCloudShadowColor, uCloudColor, clamp(uCloudAmbient, 0.0, 1.0));
        vec3 col = mix(ambient, lit, 0.7);
        float absorb = exp(-d * abs(dt) * 0.01 * uCloudAbsorption);
        scatter += transmittance * (1.0 - absorb) * col;
        transmittance *= absorb;
    }
    float horizonFade = smoothstep(0.01, 0.15, dir.y);
    return vec4(scatter * horizonFade, (1.0 - transmittance) * horizonFade);
}

void main() {
    vec4 clip = vec4(vNdc, 1.0, 1.0);
    vec4 view = uInvProjection * clip;
    vec3 viewDir = normalize(view.xyz / view.w);
    vec3 dir = normalize((uInvView * vec4(viewDir, 0.0)).xyz);

    vec3 color = skyGradient(dir);

    vec3 sd = sunDirection();
    float daylight = clamp(sd.y * 2.0 + 0.5, 0.0, 1.0);
    float night = 1.0 - daylight;

    color += nebula(dir) * night;
    color += stars(dir) * night;
    color += shootingStars(dir) * night;
    color += aurora(dir) * night;
    color += celestial(dir);

    vec3 lightDir = daylight > 0.5 ? sd : moonDirection();
    vec3 lightColor = daylight > 0.5 ? uSunColor : uMoonColor;
    vec4 cl = clouds(dir, lightDir, lightColor);
    color = color * (1.0 - cl.a) + cl.rgb;

    float vignette = length(vNdc * vec2(uResolution.x / max(uResolution.y, 1.0), 1.0)) * 0.5;
    color *= 1.0 - clamp(vignette * vignette, 0.0, 1.0) * clamp(uVignetteIntensity, 0.0, 1.0);

    fragColor = vec4(max(color, vec3(0.0)), 1.0);
}
