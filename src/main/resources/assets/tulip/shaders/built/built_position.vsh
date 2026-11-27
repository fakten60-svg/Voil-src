#version 330 core

layout(location = 0) in vec3 Position;

uniform mat4 projMat;
uniform mat4 viewMat;
uniform vec3 pos_offsetVec;
uniform vec3 pos_scaleVec;
uniform vec4 colorVal;

out vec4 vertexColor;

void main() {
    vec3 pos = pos_scaleVec == vec3(0.0) ? Position : Position * pos_scaleVec + pos_offsetVec;
    gl_Position = projMat * viewMat * vec4(pos, 1.0);
    vertexColor = colorVal == vec4(0.0) ? vec4(1.0) : colorVal;
}
