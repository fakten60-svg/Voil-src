#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;

uniform mat4 projMat;
uniform mat4 viewMat;

out vec2 vPos;
out float vRadius;
out vec2 vCenter;
out vec4 vColor;

void main() {
    gl_Position = projMat * viewMat * vec4(Position.xy, 0.0, 1.0);
    vPos = Position.xy;
    vRadius = Position.z;
    vCenter = UV0;
    vColor = Color;
}
