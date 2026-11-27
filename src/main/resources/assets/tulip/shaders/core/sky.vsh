#version 330 core

layout(location = 0) in vec2 Position;

out vec2 vNdc;

void main() {
    vNdc = Position;
    gl_Position = vec4(Position, 1.0, 1.0);
}
