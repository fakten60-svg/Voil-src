#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

uniform mat4 projMat;
uniform mat4 viewMat;
uniform vec3 pos_offsetVec;
uniform vec3 pos_scaleVec;
uniform vec2 uv_offsetVec;
uniform vec2 uv_scaleVec;
uniform vec4 colorVal;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    vec3 pos = pos_scaleVec == vec3(0.0) ? Position : Position * pos_scaleVec + pos_offsetVec;
    gl_Position = projMat * viewMat * vec4(pos, 1.0);
    texCoord0 = uv_scaleVec == vec2(0.0) ? UV0 : UV0 * uv_scaleVec + uv_offsetVec;
    vertexColor = colorVal == vec4(0.0) ? vec4(1.0) : colorVal;
}
