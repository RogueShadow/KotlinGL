#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

out vec2 texCoord;
out vec3 Normal;
out vec3 FragPos;
out vec4 FragPosLightSpace;

uniform mat4 transform;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceMatrix;
uniform float elapsedTime;

void main()
{

    gl_Position = projection * view * transform * vec4(aPos,1);
    texCoord = aTexCoord;
    Normal = transpose(inverse(mat3((transform)))) * aNormal;
    FragPos = vec3(transform * vec4(aPos,1));
    FragPosLightSpace = (lightSpaceMatrix) * vec4(FragPos,1);
}