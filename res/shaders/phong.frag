varying vec3 normal;
varying vec3 cameraDirection;
varying vec3 lightDirection[3];


void main (void)
{
    //gl_FragColor = vec4(0.0,0.0,1.0,1.0);
    vec4 finalColor = gl_FrontMaterial.emission + gl_FrontMaterial.ambient * gl_LightModel.ambient;
    vec3 normalizedNormal = normalize(normal);


    gl_LightSourceParameters lightSource[3];
    lightSource[0] = gl_LightSource[0];
    lightSource[1] = gl_LightSource[1];
    lightSource[2] = gl_LightSource[2];

    for(int i = 0; i < 3; ++i)
    {
        vec3 normalizedLightDirection = normalize(lightDirection[i]);
        float lambertTerm =  max(dot(normalizedNormal, normalizedLightDirection), 0.0);
        finalColor += lambertTerm * lightSource[i].diffuse * gl_FrontMaterial.diffuse;
        vec3 normalizedCameraDirection = normalize(cameraDirection);
        vec3 reflectionDirection = reflect(-normalizedLightDirection, normalizedNormal);
        float specular = pow(max(dot(reflectionDirection, normalizedCameraDirection), 0.0), gl_FrontMaterial.shininess);
        finalColor +=  specular * lightSource[i].specular * gl_FrontMaterial.specular;
    }
    gl_FragColor = finalColor;
}
