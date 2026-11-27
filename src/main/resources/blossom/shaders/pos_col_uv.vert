#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;

uniform mat4 projMat;
uniform mat4 viewMat;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    gl_Position = projMat * viewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    vertexColor = Color;
}
