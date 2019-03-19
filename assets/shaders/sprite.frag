#version 330 core

out vec4 FragColor;
in vec3 Normal;
in vec2 texCoord;
in vec3 FragPos;

uniform sampler2D sprite;

void main()
{
    float i = texture(sprite,texCoord).r;
    FragColor = vec4(i,i,i,1);
}
