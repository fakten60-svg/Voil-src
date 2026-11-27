#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;

uniform mat4 projMat;
uniform mat4 viewMat;

out vec4 vertexColor;

void main() {
    gl_Position = projMat * viewMat * vec4(Position, 1.0);
    vertexColor = Color;
}
