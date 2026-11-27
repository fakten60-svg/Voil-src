#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

uniform mat4 projMat;
uniform mat4 viewMat;
uniform mat4 modelMat;
uniform float useViewProj;
uniform vec3 pos_offsetVec;
uniform vec3 pos_scaleVec;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    if (useViewProj > 1.0) {
        vec3 pos = pos_scaleVec == vec3(0.0) ? Position : Position * pos_scaleVec + pos_offsetVec;
        gl_Position = projMat * viewMat * vec4(pos, 1.0);
    } else {
        gl_Position = modelMat * vec4(Position.xy, 0.0, 1.0);
    }
    texCoord0 = UV0;
    vertexColor = Color;
}
