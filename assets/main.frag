#version 330 core
struct Material {
    sampler2D diffuse;
    vec3 specular;
    float shininess;
};
struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
out vec4 FragColor;
in vec3 Normal;
in vec2 texCoord;
in vec3 FragPos;
uniform Material material;
uniform vec3 objectColor;
uniform vec3 viewPos;
uniform Light light;
uniform vec3 tint;
void main()
{
    vec3 diffuseColor = vec3(texture(material.diffuse,texCoord));
    //vec3 diffuseColor = objectColor;
    vec3 ambient = light.ambient * diffuseColor;
    //diffuse
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(light.position - FragPos);
    float diff = max( dot(norm,lightDir) , 0.0 );
    vec3 diffuse = light.diffuse * diff * diffuseColor;
    //specular
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(lightDir, norm);
    float spec = pow(max(dot(-viewDir, reflectDir), 0.0), material.shininess);
    vec3 specular = light.specular * (spec * material.specular);
    vec3 result = (ambient + diffuse + specular);
    FragColor = vec4(result, 1.0);
}