#version 330 core

layout (location = 0) in vec3 aPos;

uniform mat4 lightSpaceMatrix;
uniform mat4 transform;
uniform mat4 lightView;
uniform mat4 lightProj;

void main()
{
    gl_Position =  lightProj * lightView * transform * vec4(aPos, 1.0);
}