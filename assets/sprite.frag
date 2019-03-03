#version 330 core

out vec4 FragColor;
in vec3 Normal;
in vec2 texCoord;
in vec3 FragPos;

uniform sampler2D sprite;

void main()
{
    FragColor = texture(sprite,texCoord);
}
