#version 330 core
struct Material {
    vec3 diffuse;
    vec3 specular;
    float shininess;
    vec3 tint;
    sampler2D diff_tex;
    int use_diff_tex;
    sampler2D spec_tex;
    int use_spec_tex;
};
struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float linear;
    float constant;
    float quadratic;
};
struct SunLamp {
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
out vec4 FragColor;
in vec3 Normal;
in vec2 texCoord;
in vec3 FragPos;
uniform int engine_number_of_lights;
uniform Material material;
uniform SunLamp sunlamp;
uniform vec3 viewPos;
uniform Light[16] light;

vec3 CalculatePointLamp(Light light ,vec3 normal, vec3 viewDir);
vec3 CalculateSunLamp(vec3 normal, vec3 viewDir);
vec3 diffuseColor = material.diffuse + material.tint;
vec3 specularColor = material.specular;

void main()
{
    if (material.use_diff_tex == 1){
        diffuseColor = vec3(texture(material.diff_tex,texCoord)) + material.tint;
    }
    if (material.use_spec_tex == 1){
        specularColor = vec3(texture(material.spec_tex,texCoord));
    }

    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);

    vec3 finalColor = CalculateSunLamp(norm,viewDir);

    for (int i = 0;i<engine_number_of_lights;i++)
        finalColor += CalculatePointLamp(light[i],norm,viewDir);


    FragColor = vec4(finalColor, 1.0);
}

vec3 CalculatePointLamp(Light light ,vec3 normal, vec3 viewDir){
    float distance = length(light.position - FragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));

    vec3 result = vec3(0,0,0);

    result += light.ambient * diffuseColor * attenuation;

    vec3 lightDir = normalize(light.position - FragPos);
    float diff = max( dot(normal,lightDir), 0.0);
    result += light.diffuse * diff * diffuseColor * attenuation;

    vec3 reflectDir = reflect(lightDir, normal);
    float spec = pow( max(dot(-viewDir,reflectDir),0.0),material.shininess);
    result += (light.specular * (spec * specularColor)) * attenuation;

    return result;
}

vec3 CalculateSunLamp(vec3 normal, vec3 viewDir){
    vec3 result = vec3(0,0,0);

    vec3 sunDir = normalize(-sunlamp.direction);
    float diff = max( dot(normal,sunDir), 0.0);
    result += sunlamp.diffuse * diff * diffuseColor;

    result += sunlamp.ambient * diffuseColor * sunlamp.diffuse;

    vec3 reflectDir = reflect(sunDir, normal);
    float spec = pow(max(dot(-viewDir,reflectDir),0.0),material.shininess);
    result += sunlamp.specular * spec * specularColor;

    return result;
}