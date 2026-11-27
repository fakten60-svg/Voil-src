#version 330 core

layout(location = 0) in vec3 Position;

layout(std140) uniform PosOffset { vec4 posOffset[1024]; };
layout(std140) uniform PosScale { vec4 posScale[1024]; };
layout(std140) uniform Color { vec4 instColor[1024]; };
layout(std140) uniform Scissor { vec4 instScissor[1024]; };

uniform mat4 projMat;
uniform mat4 viewMat;
uniform int uBaseInstance;

out vec4 v_scissor;
out vec4 vertexColor;
flat out int glInstID;

void main() {
    int id = gl_InstanceID + uBaseInstance;
    vec3 pos = Position * vec3(posScale[id].xy, 1.0) + posOffset[id].xyz;
    gl_Position = projMat * viewMat * vec4(pos, 1.0);
    v_scissor = instScissor[id];
    vertexColor = instColor[id];
    glInstID = id;
}
