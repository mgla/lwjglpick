varying vec3 normal;
varying vec3 cameraDirection;
varying vec3 lightDirection[3];

void main()
{
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    normal = normalize(gl_NormalMatrix * gl_Normal);

    //Calculate the direction to the camera and to
    //the light for that vertex
    vec3 vertex = (gl_ModelViewMatrix * gl_Vertex).xyz;
    cameraDirection = -vertex;

 
    
    gl_LightSourceParameters lightSource[3];
    lightSource[0] = gl_LightSource[0];
    lightSource[1] = gl_LightSource[1];
    lightSource[2] = gl_LightSource[2];

    for(int i = 0; i < 3; ++i)
    {
        lightDirection[i] = lightSource[i].position.xyz - vertex;
    }
}
