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
uniform Material material;
uniform SunLamp sunlamp;
uniform vec3 viewPos;
uniform Light light;

void main()
{

    vec3 finalColor = vec3(0,0,0);

    // Configure color based on weather or not a texture is supplied
    vec3 diffuseColor = material.diffuse + material.tint;
    if (material.use_diff_tex == 1){
        diffuseColor = vec3(texture(material.diff_tex,texCoord)) + material.tint;
    }
    vec3 specularColor = material.specular;
    if (material.use_spec_tex == 1){
        specularColor = vec3(texture(material.spec_tex,texCoord));
    }
    //ambient light
    finalColor += light.ambient * diffuseColor;

    //diffuse
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(light.position - FragPos);
    float diff = max( dot(norm,lightDir) , 0.0 );
    finalColor += light.diffuse * diff * diffuseColor;

    //specular
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(lightDir, norm);
    float spec = pow(max(dot(-viewDir, reflectDir), 0.0), material.shininess);
    finalColor += (light.specular * (spec * specularColor));

    //sunlamp
    vec3 sunDir = normalize(-sunlamp.diffuse);
    float sun = max(dot(norm,sunDir) , 0.0);
    finalColor += sunlamp.diffuse * sun * diffuseColor;

    FragColor = vec4(finalColor, 1.0);
}