#version 330 core

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

layout(std140) uniform PosOffset { vec4 posOffset[1024]; };
layout(std140) uniform PosScale { vec4 posScale[1024]; };
layout(std140) uniform Color { vec4 instColor[1024]; };
layout(std140) uniform Scissor { vec4 instScissor[1024]; };
layout(std140) uniform UvOffset { vec4 uvOffset[1024]; };
layout(std140) uniform UvScale { vec4 uvScale[1024]; };

uniform mat4 projMat;
uniform mat4 viewMat;
uniform int uBaseInstance;

out vec2 texCoord0;
out vec4 vertexColor;
out vec4 v_scissor;
flat out int glInstID;

void main() {
    int id = gl_InstanceID + uBaseInstance;
    vec3 pos = Position * vec3(posScale[id].xy, 1.0) + posOffset[id].xyz;
    gl_Position = projMat * viewMat * vec4(pos, 1.0);
    texCoord0 = UV0 * uvScale[id].xy + uvOffset[id].xy;
    vertexColor = instColor[id];
    v_scissor = instScissor[id];
    glInstID = id;
}
